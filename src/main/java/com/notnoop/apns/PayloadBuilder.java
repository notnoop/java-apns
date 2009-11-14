package com.notnoop.apns;

import net.sf.json.JSONObject;

public final class PayloadBuilder {
    private JSONObject aps;

    PayloadBuilder() {
        this.aps = new JSONObject();
    }

    public PayloadBuilder alert(String alert) {
        aps.put("alert", alert);
        return this;
    }

    public PayloadBuilder sound(String sound) {
        aps.put("sound", sound);
        return this;
    }

    public PayloadBuilder badge(int badge) {
        aps.put("badge", badge);
        return this;
    }

    public String build() {
        JSONObject root = new JSONObject();
        root.put("aps", aps);
        return root.toString();
    }

    @Override
    public String toString() {
        return this.build();
    }

}
