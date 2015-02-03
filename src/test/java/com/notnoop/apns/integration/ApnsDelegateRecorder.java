package com.notnoop.apns.integration;

import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.DeliveryError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ApnsDelegateRecorder implements ApnsDelegate {

    private List<MessageSentRecord> sent = new ArrayList<MessageSentRecord>();
    private List<MessageSentFailedRecord> failed = new ArrayList<MessageSentFailedRecord>();

    @Override
    public void messageSent(ApnsNotification message, boolean resent) {
        sent.add(new MessageSentRecord(message, resent));
    }

    @Override
    public void messageSendFailed(ApnsNotification message, Throwable e) {
        failed.add(new MessageSentFailedRecord(message, e));
    }

    @Override
    public void connectionClosed(DeliveryError e, int messageIdentifier) {
        // not stubbed
    }

    @Override
    public void cacheLengthExceeded(int newCacheLength) {
        // not stubbed
    }

    @Override
    public void notificationsResent(int resendCount) {
        // not stubbed
    }

    public List<MessageSentRecord> getSent() {
        return Collections.unmodifiableList(sent);
    }

    public List<MessageSentFailedRecord> getFailed() {
        return Collections.unmodifiableList(failed);
    }

    public static class MessageSentRecord {
        private final ApnsNotification notification;
        private final boolean resent;

        public MessageSentRecord(ApnsNotification notification, boolean resent) {
            this.notification = notification;
            this.resent = resent;
        }

        public ApnsNotification getNotification() {
            return notification;
        }

        public boolean isResent() {
            return resent;
        }
    }

    public static class MessageSentFailedRecord {
        private final ApnsNotification notification;
        private final Throwable ex;

        public MessageSentFailedRecord(ApnsNotification notification, Throwable ex) {
            this.notification = notification;
            this.ex = ex;
        }

        public ApnsNotification getNotification() {
            return notification;
        }

        @SuppressWarnings("unchecked")
        public <T> T getException() {
            return (T) ex;
        }

        public void assertRecord(ApnsNotification notification, Throwable ex) {
            assertEquals(notification, getNotification());
            assertEquals(ex.getClass(), this.ex.getClass());
        }
    }

}
