package com.notnoop.apns.utils.Simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Server simulator that can simulate some failure modes.
 */
public class FailingApnsServerSimulator extends ApnsServerSimulator {

    private static final Logger logger = LoggerFactory.getLogger(FailingApnsServerSimulator.class);


    private BlockingQueue<Notification> queue = new LinkedBlockingQueue<Notification>();

    public FailingApnsServerSimulator(final ServerSocketFactory sslFactory) {
        super(sslFactory);
    }

    @Override
    protected void onNotification(final ApnsServerSimulator.Notification notification, final InputOutputSocket inputOutputSocket)
            throws IOException {
        logger.debug("Queueing notification " + notification);
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
