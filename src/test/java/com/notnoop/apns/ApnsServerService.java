package com.notnoop.apns;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * A delegate that gets notified of the delivery of messages.
 */
public interface ApnsServerService {


    /**
     * Called when message was successfully received
     *
     * @param message the notification that was received.
     * @throws Exception
     */
    void messageReceived(ApnsNotification message) throws Exception;


    /**
     * Called to determine if any of the devices is judged to be inactive.
     * 
     * @return a map of inactive devices.
     */
    Map<byte[], Date> getInactiveDevices();
    
    public static final ApnsServerService EMPTY = new ApnsServerService() {
		@Override
		public void messageReceived(ApnsNotification message) throws Exception {
		}
		
		@Override
		public Map<byte[], Date> getInactiveDevices() {
			return Collections.emptyMap();
		}
	};
}