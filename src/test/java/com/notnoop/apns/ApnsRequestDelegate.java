package com.notnoop.apns;

/**
 * A delegate that gets notified of the delievery of messages.
 */
public interface ApnsRequestDelegate {

    /**
     * Called when message was successfully received
     *
     * @param message the notification that was received
     */
    void messageReceived(ApnsRequest message);
}