package com.notnoop.apns.utils.Simulator;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ApnsInputStream extends DataInputStream {
    public ApnsInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    byte[] readBlob() throws IOException {
        int length = readUnsignedShort();
        byte[] blob = new byte[length];
        readFully(blob);
        return blob;
    }

    ApnsInputStream readFrame() throws IOException {
        int length = readInt();
        byte[] buffer = new byte[length];
        readFully(buffer);
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        return new ApnsInputStream(byteArrayInputStream);
    }

    public Item readItem() throws IOException {
        byte itemId = readByte();
        byte[] blob = readBlob();
        return new Item(itemId, blob);
    }

    public static class Item {
        public final static byte ID_DEVICE_TOKEN = 1;
        public final static byte ID_PAYLOAD = 2;
        public final static byte ID_NOTIFICATION_IDENTIFIER = 3;
        public final static byte ID_EXPIRATION_DATE = 4;
        public final static byte ID_PRIORITY = 5;
        public final static Item DEFAULT = new Item((byte)0, new byte[0]);

        private final byte itemId;
        private final byte[] blob;

        public Item(final byte itemId, final byte[] blob) {

            this.itemId = itemId;
            this.blob = blob;
        }

        public byte getItemId() {
            return itemId;
        }

        public byte[] getBlob() {
            return blob.clone();
        }

        public int getInt() { return blob.length < 4 ? 0 : ByteBuffer.wrap(blob).getInt(); }

        public byte getByte() { return blob.length < 1 ? 0 : blob[0]; }
    }
}
