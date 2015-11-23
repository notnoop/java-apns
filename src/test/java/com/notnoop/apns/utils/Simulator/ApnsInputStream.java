/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
