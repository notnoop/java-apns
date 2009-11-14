package com.notnoop.apns;

public interface ApnsService {

    boolean push(String deviceToken, String message);

    void start();

    void stop();

}
