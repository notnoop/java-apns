package com.notnoop.apns;

/**
 * A delegate that also gets notified just before a notification is being delivered to the
 * Apple Server.
 */
public interface StartSendingApnsDelegate extends ApnsDelegate {

    /**
     * Called when message is about to be sent to the Apple servers.
     *
     * @param message the notification that is about to be sent
     * @param resent whether the notification is being resent after an error
     */
    public void startSending(ApnsNotification message, boolean resent);

}
