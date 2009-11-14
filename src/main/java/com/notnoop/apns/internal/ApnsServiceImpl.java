package com.notnoop.apns.internal;

import com.notnoop.apns.ApnsService;

public class ApnsServiceImpl implements ApnsService {
    private ApnsConnection connection;

    public ApnsServiceImpl(ApnsConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean push(String deviceToken, String message) {
        ApnsMessage msg = new ApnsMessage(deviceToken, message);
        connection.sendMessage(msg);
        return true;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

}
