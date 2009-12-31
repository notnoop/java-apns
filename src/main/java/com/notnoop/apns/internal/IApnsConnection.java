package com.notnoop.apns.internal;

import java.io.Closeable;

import com.notnoop.apns.ApnsNotification;

public interface IApnsConnection extends Closeable {
    void sendMessage(ApnsNotification m);
    IApnsConnection copy();
}
