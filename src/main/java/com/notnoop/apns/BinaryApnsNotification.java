package com.notnoop.apns;

import com.notnoop.apns.internal.Utilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Represents an APNS notification to be sent to Apple service using <a href="https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW4">Binary Interface</a>
 */
public class BinaryApnsNotification implements ApnsNotification {

    private static final byte COMMAND = 2;
    private static final int ITEM_ID_DEVICE_TOKEN = 1;
    private static final int ITEM_ID_PAYLOAD = 2;
    private static final int ITEM_ID_IDENTIFIER = 3;
    private static final int ITEM_ID_EXPIRATION = 4;
    private static final int ITEM_ID_PRIORITY = 5;

    private static final int ITEM_IDENTIFIER_LENGTH = 4;
    private static final int ITEM_EXPIRATION_LENGTH = 4;
    private static final int ITEM_PRIORITY_LENGTH = 1;
    private static final int ITEM_COMMAND_LENGTH = 1;
    private static final int ITEM_LENGTH_LENGTH = 4;

    private final int identifier;
    private final int expiry;
    private final byte[] deviceToken;
    private final byte[] payload;


    /**
     * Constructs an instance of {@code ApnsNotification}.
     * <p/>
     * The message encodes the payload with a {@code UTF-8} encoding.
     *
     * @param deviceToken The Hex of the device token of the destination phone
     * @param payload     The payload message to be sent
     */
    public BinaryApnsNotification(
            int identifier, int expiryTime,
            String deviceToken, String payload) {
        this.identifier = identifier;
        this.expiry = expiryTime;
        this.deviceToken = Utilities.decodeHex(deviceToken);
        this.payload = Utilities.toUTF8Bytes(payload);
    }

    /**
     * Returns the binary representation of the device token.
     */
    public byte[] getDeviceToken() {
        return Utilities.copyOf(deviceToken);
    }

    /**
     * Returns the binary representation of the payload.
     */
    public byte[] getPayload() {
        return Utilities.copyOf(payload);
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getExpiry() {
        return expiry;
    }

    private byte[] marshall = null;

    /**
     * Returns the binary representation of the message as expected by the
     * APNS server.
     * <p/>
     * The returned array can be used to sent directly to the APNS server
     * (on the wire/socket) without any modification.
     */
    public byte[] marshall() {

        try {

            if (marshall == null) {
                byte[] itemBytes = marshalItemData();

                final ByteArrayOutputStream boas = new ByteArrayOutputStream(itemBytes.length + ITEM_COMMAND_LENGTH + ITEM_LENGTH_LENGTH);
                final DataOutputStream dos = new DataOutputStream(boas);

                dos.writeByte(COMMAND);
                dos.writeInt(itemBytes.length);
                dos.write(itemBytes);

                marshall = boas.toByteArray();
            }
            return marshall;
        } catch (final IOException e) {
            throw new AssertionError();
        }
    }

    public byte[] marshalItemData() throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(payload.length + deviceToken.length + ITEM_IDENTIFIER_LENGTH + ITEM_EXPIRATION_LENGTH + ITEM_PRIORITY_LENGTH);
        final DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(ITEM_ID_DEVICE_TOKEN);
        dos.writeShort(deviceToken.length);
        dos.write(deviceToken);

        dos.writeByte(ITEM_ID_PAYLOAD);
        dos.writeShort(payload.length);
        dos.write(payload);

        dos.writeByte(ITEM_ID_IDENTIFIER);
        dos.writeShort(ITEM_IDENTIFIER_LENGTH);
        dos.writeInt(identifier);

        dos.writeByte(ITEM_ID_EXPIRATION);
        dos.writeShort(ITEM_EXPIRATION_LENGTH);
        dos.writeInt(expiry);

        dos.writeByte(ITEM_ID_PRIORITY);
        dos.writeShort(ITEM_PRIORITY_LENGTH);
        dos.writeByte(10);
        dos.flush();

        return baos.toByteArray();
    }

    @Override
    public int hashCode() {
        return (21
                + 31 * identifier
                + 31 * expiry
                + 31 * Arrays.hashCode(deviceToken)
                + 31 * Arrays.hashCode(payload));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryApnsNotification)) {
            return false;
        }
        BinaryApnsNotification o = (BinaryApnsNotification) obj;
        return (identifier == o.identifier
                && expiry == o.expiry
                && Arrays.equals(this.deviceToken, o.deviceToken)
                && Arrays.equals(this.payload, o.payload));
    }

    @Override
    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    public String toString() {
        String payloadString;
        try {
            payloadString = new String(payload, "UTF-8");
        } catch (Exception ex) {
            payloadString = "???";
        }
        return "Message (Id=" + identifier + "; Token=" + Utilities.encodeHex(deviceToken) + "; Payload=" + payloadString + ")";
    }
}
