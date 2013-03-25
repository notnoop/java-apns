package com.notnoop.apns;

import java.util.Date;
import java.util.Map;

/**
 * A delegate that gets notified of the delivery of messages.
 */
public interface ApnsRequestDelegate {

    /**
     * Called when message was successfully received
     *
     * @param message the notification that was received.
     */
    void messageReceived(ApnsNotification message);


    /**
     * Called to determine if any of the devices is judged to be inactive.
     * 
     * @return a map of inactive devices.
     */
    Map<byte[], Date> getInactiveDevices();
}