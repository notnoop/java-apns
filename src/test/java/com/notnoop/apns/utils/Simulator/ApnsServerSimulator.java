package com.notnoop.apns.utils.Simulator;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.net.ServerSocketFactory;
import com.notnoop.apns.internal.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ApnsServerSimulator {

    private static final Logger logger = LoggerFactory.getLogger(ApnsServerSimulator.class);

    private final Semaphore startUp = new Semaphore(0);
    private final ServerSocketFactory sslFactory;
    private int effectiveGatewayPort;
    private int effectiveFeedbackPort;

    public ApnsServerSimulator(ServerSocketFactory sslFactory) {
        this.sslFactory = sslFactory;
    }

    Thread gatewayThread;
    Thread feedbackThread;
    ServerSocket gatewaySocket;
    ServerSocket feedbackSocket;

    public void start() {
        logger.debug("Starting APNSServerSimulator");
        gatewayThread = new GatewayListener();
        feedbackThread = new FeedbackRunner();
        gatewayThread.start();
        feedbackThread.start();
        startUp.acquireUninterruptibly(2);
    }

    public void stop() {
        logger.debug("Stopping APNSServerSimulator");
        try {
            if (gatewaySocket != null) {
                gatewaySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (feedbackSocket != null) {
                feedbackSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (gatewayThread != null) {
            gatewayThread.interrupt();
        }

        if (feedbackThread != null) {
            feedbackThread.interrupt();
        }

    }

    public int getEffectiveGatewayPort() {
        return effectiveGatewayPort;
    }

    public int getEffectiveFeedbackPort() {
        return effectiveFeedbackPort;
    }

    private class GatewayListener extends Thread {

        public void run() {
            logger.debug("Running GatewayListener");

            try {
                gatewaySocket = sslFactory.createServerSocket(0);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            effectiveGatewayPort = gatewaySocket.getLocalPort();

            // Listen for connections
            startUp.release();

            while (!isInterrupted()) {
                try {
                    handleGatewayConnection(new InputOutputSocket(gatewaySocket.accept()));
                } catch (SocketException ex) {
                    interrupt();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        private void handleGatewayConnection(final InputOutputSocket inputOutputSocket) throws IOException {
            Thread gatewayConnectionTread = new Thread() {
                @Override
                public void run() {
                    try {
                        parseNotifications(inputOutputSocket);
                    } finally {
                        inputOutputSocket.close();
                    }
                }
            };
            gatewayConnectionTread.start();
        }

        private void parseNotifications(final InputOutputSocket inputOutputSocket) {
            logger.debug("Runnin parseNotifications {}", inputOutputSocket.getSocket());
            while (!Thread.interrupted()) {
                try {
                    final ApnsInputStream inputStream = inputOutputSocket.getInputStream();
                    byte notificationType = inputStream.readByte();
                    logger.debug("Received Notification (type {})", notificationType);
                    switch (notificationType) {
                        case 0:
                            readLegacyNotification(inputOutputSocket);
                            break;
                        case 1:
                            readEnhancedNotification(inputOutputSocket);
                            break;
                        case 2:
                            readFramedNotifications(inputOutputSocket);
                            break;
                    }
                } catch (IOException ioe) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void readFramedNotifications(final InputOutputSocket inputOutputSocket) throws IOException {

            Map<Byte, ApnsInputStream.Item> map = new HashMap<Byte, ApnsInputStream.Item>();

            ApnsInputStream frameStream = inputOutputSocket.getInputStream().readFrame();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final ApnsInputStream.Item item = frameStream.readItem();
                    map.put(item.getItemId(), item);
                }
            } catch (EOFException eof) {
                // Done reading.
            }

            byte[] deviceToken = get(map, ApnsInputStream.Item.ID_DEVICETOKEN).getBlob();
            byte[] payload = get(map, ApnsInputStream.Item.ID_PAYLOAD).getBlob();
            int identifier = get(map, ApnsInputStream.Item.ID_NOTIFICATIONIDENTIFIER).getInt();
            int expiry = get(map, ApnsInputStream.Item.ID_EXPIRATIONDATE).getInt();
            byte priority = get(map, ApnsInputStream.Item.ID_PRIORITY).getByte();

            final Notification notification = new Notification(2, identifier, expiry, deviceToken, payload, priority);
            logger.debug("Read framed notification {}", notification);
            onNotification(notification, inputOutputSocket);

        }

        private ApnsInputStream.Item get(final Map<Byte, ApnsInputStream.Item> map, final byte idDevicetoken) {
            ApnsInputStream.Item item = map.get(idDevicetoken);
            if (item == null) {
                item = ApnsInputStream.Item.DEFAULT;
            }
            return item;
        }

        private void readEnhancedNotification(final InputOutputSocket inputOutputSocket) throws IOException {
            ApnsInputStream inputStream = inputOutputSocket.getInputStream();

            int identifier = inputStream.readInt();
            int expiry = inputStream.readInt();
            final byte[] deviceToken = inputStream.readBlob();
            final byte[] payload = inputStream.readBlob();
            final Notification notification = new Notification(1, identifier, expiry, deviceToken, payload);
            logger.debug("Read enhanced notification {}", notification);
            onNotification(notification, inputOutputSocket);
        }

        private void readLegacyNotification(final InputOutputSocket inputOutputSocket) throws IOException {
            ApnsInputStream inputStream = inputOutputSocket.getInputStream();

            final byte[] deviceToken = inputStream.readBlob();
            final byte[] payload = inputStream.readBlob();
            final Notification notification = new Notification(0, deviceToken, payload);
            logger.debug("Read legacy notification {}", notification);
            onNotification(notification, inputOutputSocket);

        }

        @Override
        public void interrupt() {
            logger.debug("Interrupt, closing socket");
            super.interrupt();
            try {
                gatewaySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void fail(final byte status, final int identifier, final InputOutputSocket inputOutputSocket) throws IOException {
        logger.debug("FAIL {} {}", status, identifier);
        final DataOutputStream outputStream = inputOutputSocket.getOutputStream();
        outputStream.writeByte(8);
        outputStream.writeByte(status);
        outputStream.writeInt(identifier);
        outputStream.flush();
        inputOutputSocket.close();
    }

    private class FeedbackRunner extends Thread {

        public void run() {
            logger.debug("Launched FeedbackRunner");
            try {
                feedbackSocket = sslFactory.createServerSocket(0);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            effectiveFeedbackPort = feedbackSocket.getLocalPort();

            startUp.release();


            while (!isInterrupted()) {
                try {
                    handleFeedbackConnection(new InputOutputSocket(feedbackSocket.accept()));
                } catch (SocketException ex) {
                    interrupt();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

        }

        private void handleFeedbackConnection(final InputOutputSocket inputOutputSocket) {
            Thread feedbackConnectionTread = new Thread() {
                @Override
                public void run() {
                    try {
                        logger.debug("Feedback connection sending feedback");
                        sendFeedback(inputOutputSocket);
                    } catch (IOException ioe) {
                        // An exception is unexpected here. Close the current connection and bail out.
                        ioe.printStackTrace();
                    } finally {
                        inputOutputSocket.close();
                    }

                }
            };
            feedbackConnectionTread.start();
        }

        private void sendFeedback(final InputOutputSocket inputOutputSocket) throws IOException {
            List<byte[]> badTokens = getBadTokens();

            for (byte[] token : badTokens) {
                writeFeedback(inputOutputSocket, token);
            }
        }

        private void writeFeedback(final InputOutputSocket inputOutputSocket, final byte[] token) throws IOException {
            DataOutputStream dos = inputOutputSocket.getOutputStream();
            final int unixtime = (int) (new Date().getTime() / 1000);
            dos.write(unixtime);
            dos.write((short)token.length);
            dos.write(token);
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    protected class Notification {
        private final int type;
        private final int identifier;
        private final int expiry;
        private final byte[] deviceToken;
        private final byte[] payload;
        private final byte priority;

        public Notification(final int type, final byte[] deviceToken, final byte[] payload) {
            this(type, 0, 0, deviceToken, payload);
        }

        public Notification(final int type, final int identifier, final int expiry, final byte[] deviceToken, final byte[] payload) {
            this(type, identifier, expiry, deviceToken, payload, (byte) 10);

        }

        public Notification(final int type, final int identifier, final int expiry, final byte[] deviceToken, final byte[] payload,
                            final byte priority) {
            this.priority = priority;
            this.type = type;
            this.identifier = identifier;
            this.expiry = expiry;
            this.deviceToken = deviceToken;
            this.payload = payload;
        }

        public byte[] getPayload() {
            return payload.clone();
        }

        public byte[] getDeviceToken() {
            return deviceToken.clone();
        }

        public int getType() {
            return type;
        }

        public int getExpiry() {
            return expiry;
        }

        public int getIdentifier() {
            return identifier;
        }

        public byte getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Notification{" +
                    "type=" + type +
                    ", identifier=" + identifier +
                    ", expiry=" + expiry +
                    ", deviceToken=" + Utilities.encodeHex(deviceToken) +
                    //", payload=" + Utilities.encodeHex(payload) +
                    ", priority=" + priority +
                    '}';
        }
    }

    protected void onNotification(final Notification notification, final InputOutputSocket inputOutputSocket) throws IOException {}


    protected List<byte[]> getBadTokens() {
        return new ArrayList<byte[]>();
    }
}
