package com.notnoop.apns;

public class ApnsRequest {
    private final int identifier;
    private final int expiry;
    private final String deviceToken;
    private final String payload;

    public ApnsRequest(int identifier, int expiry, String deviceToken, String payload) {
        this.identifier = identifier;
        this.expiry = expiry;
        this.deviceToken = deviceToken.toUpperCase();
        this.payload = payload;
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getExpiry() {
        return expiry;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "[identifier=" + identifier + ", expiry=" + expiry + ", deviceToken=" + deviceToken + ", payload=" + payload + "]";
    }
}