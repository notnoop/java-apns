package com.notnoop.apns;

public class APNS {

    public static PayloadBuilder alert(String alert) {
        return new PayloadBuilder().alert(alert);
    }

    public static PayloadBuilder sound(String sound) {
        return new PayloadBuilder().sound(sound);
    }

    public static PayloadBuilder badge(int badge) {
        return new PayloadBuilder().badge(badge);
    }

    public static ApnsServiceBuilder newService() {
        return new ApnsServiceBuilder();
    }
}
