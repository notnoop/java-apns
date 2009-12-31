package com.notnoop.apns.internal;

import java.io.Closeable;

import com.notnoop.apns.ApnsNotification;

public interface ApnsConnection extends Closeable {
    void sendMessage(ApnsNotification m);
    ApnsConnection copy();
}
