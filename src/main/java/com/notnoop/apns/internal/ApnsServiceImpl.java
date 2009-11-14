package com.notnoop.apns.internal;

import com.notnoop.apns.ApnsService;

public class ApnsServiceImpl implements ApnsService {
    private ApnsConnection connection;

    public ApnsServiceImpl(ApnsConnection connection) {
        this.connection = connection;
    }

    @Override
    public void push(String deviceToken, String message) {
        push(new ApnsMessage(deviceToken, message));
    }

    @Override
    public void push(ApnsMessage msg) {
        connection.sendMessage(msg);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

}
