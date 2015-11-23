/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
