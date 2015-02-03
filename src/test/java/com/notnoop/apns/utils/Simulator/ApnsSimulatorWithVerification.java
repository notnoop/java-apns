package com.notnoop.apns.utils.Simulator;

import com.google.common.base.Strings;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.internal.Utilities;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Provides a simulator that receives connection over TCP as per a real APNS server.  This class allows verification
 * and prior configuration of responses in a manner similar to a mocking framework.
 */
public class ApnsSimulatorWithVerification extends ApnsServerSimulator {

    private List<Notification> receivedNotifications;
    private Queue<ApnsNotificationWithAction> expectedWithResponses;
    private List<Notification> unexpected;

    public ApnsSimulatorWithVerification(ServerSocketFactory sslFactory) {
        super(sslFactory);
        receivedNotifications = new ArrayList<Notification>();
        expectedWithResponses = new ConcurrentLinkedQueue<ApnsNotificationWithAction>();
        unexpected = new ArrayList<Notification>();
    }

    public void reset() {
        receivedNotifications.clear();
        expectedWithResponses.clear();
        unexpected.clear();
    }

    public List<Notification> getReceivedNotifications() {
        return Collections.unmodifiableList(receivedNotifications);
    }

    private void addExpected(ApnsNotificationWithAction notificationWithAction) {
        expectedWithResponses.add(notificationWithAction);
    }

    protected void onNotification(final Notification notification, final InputOutputSocket inOutSocket)
            throws IOException {
        receivedNotifications.add(notification);
        pollExpectedResponses(notification, inOutSocket);
    }

    protected void pollExpectedResponses(Notification notification, InputOutputSocket inOutSocket) throws IOException {
        final ApnsNotificationWithAction withAction = expectedWithResponses.poll();
        if (withAction == null) {
            unexpected.add(notification);
        } else if (!matchNotificationWithExpected(withAction, notification)) {
            unexpected.add(notification);
        } else {
            handleNotificationWithAction(withAction, inOutSocket);
        }
    }

    protected void handleNotificationWithAction(ApnsNotificationWithAction notificationWithAction,
                                                InputOutputSocket inOutSocket) throws IOException {
        final ApnsResponse response = notificationWithAction.getResponse();
        if (!response.isDoNothing()) {
            if (response.getAction() == Action.RETURN_ERROR_AND_SHUTDOWN) {
                // have to stop first before sending out the error
                stop();
                sendError(response, inOutSocket);
            } else {
                sendError(response, inOutSocket);
            }
        }
    }

    private boolean matchNotificationWithExpected(ApnsNotificationWithAction withAction, Notification found) {
        if (withAction.getId() != found.getIdentifier()) {
            return false;
        }
        return matchDeviceToken(withAction.getNotification().getDeviceToken(), found.getDeviceToken());
    }

    private boolean matchDeviceToken(byte[] expected, byte[] found) {
        return Arrays.equals(expected, found);
    }

    protected void sendError(ApnsResponse response, InputOutputSocket inOutSocket) throws IOException {
        final byte status = (byte) response.getError().code();
        fail(status, response.getErrorId(), inOutSocket);
    }

    public DoResponse when(Notification notification) {
        return new DoResponse(notification);
    }

    public DoResponse when(EnhancedApnsNotification notification) {
        return new DoResponse(buildNotification(notification));
    }

    public void verify() {
        final int size = expectedWithResponses.size();
        if (size > 0) {
            final String error = String.format("[%d] Expected notification(s) were not received, first id was: [%d] ",
                    size, expectedWithResponses.poll().getId());
            throw new IllegalStateException(error);
        }
        verifyUnexpected();
    }

    public void verifyAndWait(int waitSecs) {
        verifyUnexpected();

        long timeRemaining = TimeUnit.SECONDS.toMillis(waitSecs);
        final long sleepForMs = 250;
        while (!expectedWithResponses.isEmpty() && timeRemaining > 0) {
            sleep(sleepForMs < timeRemaining ? sleepForMs : timeRemaining);
            timeRemaining -= sleepForMs;
        }
        verify();
    }

    private void verifyUnexpected() {
        if (!unexpected.isEmpty()) {
            final Notification firstUnexpected = this.unexpected.get(0);
            throw new IllegalStateException(String.format("Unexpected notifications received, count: [%d].  First" +
                            " notification is for id: [%d], deviceToken: [%s]", unexpected.size(),
                    firstUnexpected.getIdentifier(), Utilities.encodeHex(firstUnexpected.getDeviceToken())));
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Notification buildNotification(EnhancedApnsNotification notification) {
        return new Notification(1, notification.getIdentifier(), notification.getExpiry(),
                notification.getDeviceToken(), notification.getPayload());
    }

    public class DoResponse {
        private final Notification expected;

        public DoResponse(Notification notification) {
            this.expected = notification;
        }

        public ApnsSimulatorWithVerification thenRespond(ApnsResponse response) {
            addExpected(new ApnsNotificationWithAction(expected, response));
            return ApnsSimulatorWithVerification.this;
        }

        public ApnsSimulatorWithVerification thenDoNothing() {
            addExpected(new ApnsNotificationWithAction(expected, ApnsResponse.doNothing()));
            return ApnsSimulatorWithVerification.this;
        }
    }

    public static String deviceTokenForId(int id)
    {
        final String right = Integer.toHexString(id).toUpperCase();
        final int zeroedLength = 64 - right.length();
        return Strings.repeat("0", zeroedLength) + right;
    }
}
