/*
 * Copyright 2010, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns;

/**
 * A delegate that gets notified of the status of notification delivery to the
 * Apple Server.
 *
 * The delegate doesn't get notified when the notification actually arrives at
 * the phone.
 */
public interface ApnsDelegate {

    /**
     * Called when message was successfully sent to the Apple servers
     *
     * @param message the notification that was sent
     * @param resent whether the notification was resent after an error
     */
    public void messageSent(ApnsNotification message, boolean resent);

    /**
     * Called when the delivery of the message failed for any reason
     *
     * If message is null, then your notification has been rejected by Apple but
     * it has been removed from the cache so it is not possible to identify
     * which notification caused the error. In this case subsequent
     * notifications may be lost. If this happens you should consider increasing
     * your cacheLength value to prevent data loss.
     *
     * @param message the notification that was attempted to be sent
     * @param e the cause and description of the failure
     */
    public void messageSendFailed(ApnsNotification message, Throwable e);

    /**
     * The connection was closed and/or an error packet was received while
     * monitoring was turned on.
     *
     * @param e the delivery error
     * @param messageIdentifier  id of the message that failed
     */
    public void connectionClosed(DeliveryError e, int messageIdentifier);

    /**
     * The resend cache needed a bigger size (while resending messages)
     *
     * @param newCacheLength new size of the resend cache.
     */
    public void cacheLengthExceeded(int newCacheLength);

    /**
     * A number of notifications has been queued for resending due to a error-response
     * packet being received.
     *
     * @param resendCount the number of messages being queued for resend
     */
    public void notificationsResent(int resendCount);
    
    /**
     * A no operation delegate that does nothing!
     */
    public final static ApnsDelegate EMPTY = new ApnsDelegateAdapter();
}
