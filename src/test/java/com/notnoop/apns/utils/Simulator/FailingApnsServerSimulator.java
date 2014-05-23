package com.notnoop.apns.utils.Simulator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ServerSocketFactory;
import com.notnoop.apns.utils.FixedCertificates;

/**
 * A Server simulator that can simulate some failure modes.
 */
public class FailingApnsServerSimulator extends ApnsServerSimulator {

    private BlockingQueue<Notification> queue = new LinkedBlockingQueue<Notification>();

    /**
     * Create an ApnsServerSimulator. This tries to behave more like the real APNS server by being fully asnyc.
     *
     * @return the server stub. Use getEffectiveGatewayPort() and getEffectiveFeedbackPort() to ask for ports.
     */
    public static FailingApnsServerSimulator prepareAndStartServer() {
        FailingApnsServerSimulator server = new FailingApnsServerSimulator(FixedCertificates.serverContext().getServerSocketFactory());
        server.start();
        return server;
    }

    public FailingApnsServerSimulator(final ServerSocketFactory sslFactory) {
        super(sslFactory);
    }

    @Override
    protected void onNotification(final ApnsServerSimulator.Notification notification, final InputOutputSocket inputOutputSocket)
            throws IOException {
        queue.add(notification);
        final byte[] token = notification.getDeviceToken();
        if (token.length == 32 && token[0] == (byte)0xff && token[1] == (byte)0xff) {
            switch (token[2]) {
                case 0:
                    fail(token[3], notification.getIdentifier(), inputOutputSocket);
                    break;

                case 1:
                    try {
                        final int millis = token[3] * 100;
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                    break;

                case 2:
                default:
                    inputOutputSocket.close();
                    break;
            }

        }
    }

    protected List<byte[]> getBadTokens() {
        return super.getBadTokens();
    }

    public BlockingQueue<Notification> getQueue() {
        return queue;
    }

}
