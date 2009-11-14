package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ApnsMessage {

    private final static byte COMMAND = 0;
    private final byte[] deviceToken;
    private final byte[] message;

    public ApnsMessage(String dtoken, String message) {
        this.deviceToken = Utilities.decodeHex(dtoken);
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
}
