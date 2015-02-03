package com.notnoop.apns.utils.Simulator;

import com.notnoop.apns.utils.Simulator.ApnsServerSimulator.Notification;

public class ApnsNotificationWithAction {
    private final Notification notification;
    private final ApnsResponse response;

    public ApnsNotificationWithAction(Notification notification) {
        this(notification, ApnsResponse.doNothing());
    }

    public ApnsNotificationWithAction(Notification notification, ApnsResponse response) {
        if (notification == null)
        {
            throw new NullPointerException("notification cannot be null");
        }
        this.notification = notification;
        if (response == null)
        {
            throw new NullPointerException("response cannot be null");
        }
        this.response = response;
    }

    public Notification getNotification() {
        return notification;
    }

    public int getId() {
        return notification.getIdentifier();
    }

    public ApnsResponse getResponse() {
        return response;
    }

}
