package com.notnoop.apns.integration;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.integration.ApnsDelegateRecorder.MessageSentFailedRecord;
import com.notnoop.apns.utils.FixedCertificates;
import com.notnoop.apns.utils.Simulator.ApnsResponse;
import com.notnoop.apns.utils.Simulator.ApnsSimulatorWithVerification;
import com.notnoop.exceptions.ApnsDeliveryErrorException;
import com.notnoop.exceptions.NetworkIOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.notnoop.apns.utils.FixedCertificates.LOCALHOST;
import static com.notnoop.apns.utils.FixedCertificates.clientContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApnsConnectionResendTest {

    private static EnhancedApnsNotification NOTIFICATION_0 = buildNotification(0);
    private static EnhancedApnsNotification NOTIFICATION_1 = buildNotification(1);
    private static EnhancedApnsNotification NOTIFICATION_2 = buildNotification(2);
    private static ApnsSimulatorWithVerification apnsSim;

    private ApnsDelegateRecorder delegateRecorder;
    private ApnsService testee;

    @Before
    public void setUp() {
        if (apnsSim == null) {
            apnsSim = new ApnsSimulatorWithVerification(FixedCertificates.serverContext().getServerSocketFactory());
            apnsSim.start();
        }
        apnsSim.reset();
        delegateRecorder = new ApnsDelegateRecorder();
        testee = build(delegateRecorder);
    }

    @AfterClass
    public static void tearDownClass() {
        if (apnsSim != null) {
            apnsSim.stop();
            apnsSim = null;
        }
    }

    /*
     * Test when we submit 3 messages to APNS 0, 1, 2.  0 is an error but we don't see the error response back until
     * 1,2 have already been submitted.  Then at this point the network connection to APNS cannot be made, so that
     * when retrying the submissions we have to notify the client that delivery failed for 1 and 2.
     */
    @Test
    public void testGivenFailedSubmissionDueToErrorThenApnsDownWithNotificationsInBufferEnsureClientNotified()
            throws Exception {

        final DeliveryError deliveryError = DeliveryError.INVALID_PAYLOAD_SIZE;

        apnsSim.when(NOTIFICATION_0).thenDoNothing();
        apnsSim.when(NOTIFICATION_1).thenDoNothing();
        apnsSim.when(NOTIFICATION_2).thenRespond(ApnsResponse.returnErrorAndShutdown(deliveryError, NOTIFICATION_0));

        testee.push(NOTIFICATION_0);
        testee.push(NOTIFICATION_1);
        testee.push(NOTIFICATION_2);

        // Give some time for connection failure to take place
        Thread.sleep(5000);
        // Verify received expected notifications
        apnsSim.verify();

        // verify delegate calls
        assertEquals(3, delegateRecorder.getSent().size());
        final List<MessageSentFailedRecord> failed = delegateRecorder.getFailed();
        assertEquals(3, failed.size());
        // first is failed delivery due to payload size
        failed.get(0).assertRecord(NOTIFICATION_0, new ApnsDeliveryErrorException(deliveryError));
        // second and third are due to not being able to connect to APNS
        assertNetworkIoExForRedelivery(NOTIFICATION_1, failed.get(1));
        assertNetworkIoExForRedelivery(NOTIFICATION_2, failed.get(2));
    }

    private void assertNetworkIoExForRedelivery(ApnsNotification notification, MessageSentFailedRecord failed) {
        failed.assertRecord(notification, new NetworkIOException());
        final NetworkIOException found = failed.getException();
        assertTrue(found.isResend());
    }


    private ApnsService build(ApnsDelegate delegate) {
        return APNS.newService()
                .withConnectTimeout(1000)
                .withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, apnsSim.getEffectiveGatewayPort())
                .withFeedbackDestination(LOCALHOST, apnsSim.getEffectiveFeedbackPort())
                .withDelegate(delegate).build();
    }

    private static EnhancedApnsNotification buildNotification(int id) {
        final String deviceToken = ApnsSimulatorWithVerification.deviceTokenForId(id);
        return new EnhancedApnsNotification(id, 1, deviceToken, "{\"aps\":{}}");
    }

}
