package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class ApnsMessage {

    private final static byte COMMAND = 0;
    private final byte[] deviceToken;
    private final byte[] message;

    public ApnsMessage(String dtoken, String message) {
        this.deviceToken = decodeHex(dtoken);
        try {
            this.message = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unsupported!");
        }
    }

    public byte[] marshell() {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(boas);

        try {
            dos.writeByte(COMMAND);
            dos.writeShort(deviceToken.length);
            dos.write(deviceToken);
            dos.writeShort(message.length);
            dos.write(message);
            return boas.toByteArray();
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    private static final Pattern pattern = Pattern.compile("[ -]");
    private static byte[] decodeHex(String deviceToken) {
        String hex = pattern.matcher(deviceToken).replaceAll("");

        byte[] bts = new byte[hex.length() / 2];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return bts;
    }
}
