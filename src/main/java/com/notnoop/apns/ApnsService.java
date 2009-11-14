package com.notnoop.apns;

import com.notnoop.apns.internal.ApnsMessage;

public interface ApnsService {

    void push(String deviceToken, String message);

    void push(ApnsMessage message);

    void start();

    void stop();

}
