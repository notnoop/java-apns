package com.notnoop.apns.integration;

import org.junit.*;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.apns.utils.ApnsServerStub;
import com.notnoop.apns.utils.FixedCertificates;
import static com.notnoop.apns.utils.FixedCertificates.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ApnsConnectionCacheTest {

    ApnsServerStub server;
    static SimpleApnsNotification msg1 = new SimpleApnsNotification("a87d8878d878a79", "{\"aps\":{}}");
    static SimpleApnsNotification msg2 = new SimpleApnsNotification("a87d8878d878a88", "{\"aps\":{}}");
    static EnhancedApnsNotification eMsg1 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    static EnhancedApnsNotification eMsg2 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    static EnhancedApnsNotification eMsg3 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");

    @Before
    public void startup() {
    }

    @After
    public void tearDown() {
        server.stop();
        server = null;
    }


    /**
     * Test1 to make sure that after rejected notification
     * in-flight notifications are re-transmitted
     *
     * @throws InterruptedException
     */
    @Test(timeout = 5000)
    public void handleReTransmissionError5Good1Bad7Good() throws InterruptedException {

        server = new ApnsServerStub(
                FixedCertificates.serverContext().getServerSocketFactory(),
                TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
        //5 success 1 fail 7 success 7 resent
        final CountDownLatch sync = new CountDownLatch(20);
        final AtomicInteger numResent = new AtomicInteger();
        final AtomicInteger numSent = new AtomicInteger();
        int EXPECTED_RESEND_COUNT = 7;
        int EXPECTED_SEND_COUNT = 12;
        server.waitForError.acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
                .withDelegate(new ApnsDelegate() {
            public void messageSent(ApnsNotification message, boolean resent) {
                if (!resent) {
                    numSent.incrementAndGet();
                }
                sync.countDown();
            }

            public void messageSendFailed(ApnsNotification message, Throwable e) {
                numSent.decrementAndGet();
            }

            public void connectionClosed(DeliveryError e, int messageIdentifier) {
            }

            public void cacheLengthExceeded(int newCacheLength) {
            }

            public void notificationsResent(int resendCount) {
                numResent.set(resendCount);
            }
        })
                .build();
        server.stopAt(eMsg1.length() * 5 + eMsg2.length() + eMsg3.length() * 14);
        for (int i = 0; i < 5; ++i) {
            service.push(eMsg1);
        }


        service.push(eMsg2);

        for (int i = 0; i < 7; ++i) {
            service.push(eMsg3);
        }

        server.sendError(8, eMsg2.getIdentifier());

        server.waitForError.release();
        server.messages.acquire();

        sync.await();

        Assert.assertEquals(EXPECTED_RESEND_COUNT, numResent.get());
        Assert.assertEquals(EXPECTED_SEND_COUNT, numSent.get());

    }

    /**
     * Test2 to make sure that after rejected notification
     * in-flight notifications are re-transmitted
     *
     * @throws InterruptedException
     */
    @Test(timeout = 5000)
    public void handleReTransmissionError1Good1Bad2Good() throws InterruptedException {
        server = new ApnsServerStub(
                FixedCertificates.serverContext().getServerSocketFactory(),
                TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
        final CountDownLatch sync = new CountDownLatch(6);
        final AtomicInteger numResent = new AtomicInteger();
        final AtomicInteger numSent = new AtomicInteger();
        int EXPECTED_RESEND_COUNT = 2;
        int EXPECTED_SEND_COUNT = 3;
        server.waitForError.acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
                .withDelegate(new ApnsDelegate() {
            public void messageSent(ApnsNotification message, boolean resent) {
                if (!resent) {
                    numSent.incrementAndGet();
                }
                sync.countDown();
            }

            public void messageSendFailed(ApnsNotification message, Throwable e) {
                numSent.decrementAndGet();
            }

            public void connectionClosed(DeliveryError e, int messageIdentifier) {
            }

            public void cacheLengthExceeded(int newCacheLength) {
            }

            public void notificationsResent(int resendCount) {
                numResent.set(resendCount);
            }
        })
                .build();
        server.stopAt(msg1.length() * 3 + eMsg2.length() * 2);
        service.push(msg1);
        service.push(eMsg2);
        service.push(eMsg1);
        service.push(msg2);

        server.sendError(8, eMsg2.getIdentifier());
        server.waitForError.release();
        server.messages.acquire();

        sync.await();

        Assert.assertEquals(EXPECTED_RESEND_COUNT, numResent.get());
        Assert.assertEquals(EXPECTED_SEND_COUNT, numSent.get());

    }

    /**
     * Test to make sure single rejected notifications are returned
     *
     * @throws InterruptedException
     */
    @Test(timeout = 5000)
    public void handleReTransmissionError1Bad() throws InterruptedException {

        server = new ApnsServerStub(
                FixedCertificates.serverContext().getServerSocketFactory(),
                TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
        final CountDownLatch sync = new CountDownLatch(1);
        final AtomicInteger numError = new AtomicInteger();
        int EXPECTED_ERROR_COUNT = 1;
        server.waitForError.acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
                .withDelegate(new ApnsDelegate() {
            public void messageSent(ApnsNotification message, boolean resent) {
            }

            public void messageSendFailed(ApnsNotification message, Throwable e) {
                numError.incrementAndGet();
                sync.countDown();
            }

            public void connectionClosed(DeliveryError e, int messageIdentifier) {
            }

            public void cacheLengthExceeded(int newCacheLength) {
            }

            public void notificationsResent(int resendCount) {
            }
        })
                .build();
        server.stopAt(eMsg1.length());
        service.push(eMsg1);

        server.sendError(8, eMsg1.getIdentifier());
        server.waitForError.release();
        server.messages.acquire();

        sync.await();

        Assert.assertEquals(EXPECTED_ERROR_COUNT, numError.get());
    }

    /**
     * Test to make sure that after rejected notification
     * in-flight notifications are re-transmitted with a queued connection
     *
     * @throws InterruptedException
     */
    @Test(timeout = 10000)
    public void handleTransmissionErrorInQueuedConnection() throws InterruptedException {
        server = new ApnsServerStub(
                FixedCertificates.serverContext().getServerSocketFactory(),
                TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
        final AtomicInteger sync = new AtomicInteger(138);
        final AtomicInteger numResent = new AtomicInteger();
        final AtomicInteger numSent = new AtomicInteger();
        server.waitForError.acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
                .asQueued()
                .withDelegate(new ApnsDelegate() {
            public void messageSent(ApnsNotification message, boolean resent) {
                if (!resent) {
                    numSent.incrementAndGet();
                }
                sync.getAndDecrement();
            }

            public void messageSendFailed(ApnsNotification message, Throwable e) {
                numSent.decrementAndGet();
                sync.incrementAndGet();
            }

            public void connectionClosed(DeliveryError e, int messageIdentifier) {
            }

            public void cacheLengthExceeded(int newCacheLength) {
            }

            public void notificationsResent(int resendCount) {
                numResent.set(resendCount);
                sync.getAndAdd(resendCount);
            }
        })
                .build();
        server.stopAt(eMsg3.length() * 50 + msg1.length() * 3
                + eMsg2.length() * 2 + eMsg1.length() * 85);
        for (int i = 0; i < 50; ++i) {
            service.push(eMsg3);
        }
        service.push(msg1);
        service.push(eMsg2);
        service.push(eMsg1);
        service.push(msg2);
        for (int i = 0; i < 85; ++i) {
            service.push(eMsg1);
        }

        server.sendError(8, eMsg2.getIdentifier());
        server.waitForError.release();
        server.messages.acquire();

        while(sync.get() != 0) {
            Thread.yield();
        }
    }

    /**
     * Test to make sure that if the cache length is violated we get
     * a notification
     *
     * @throws InterruptedException
     */
    @Test(timeout = 5000)
    public void cacheLengthNotification() throws InterruptedException {

        server = new ApnsServerStub(
                FixedCertificates.serverContext().getServerSocketFactory(),
                TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
        final CountDownLatch sync = new CountDownLatch(1);
        int ORIGINAL_CACHE_LENGTH = 100;
        final AtomicInteger modifiedCacheLength = new AtomicInteger();
        server.waitForError.acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
                .withDelegate(new ApnsDelegate() {
            public void messageSent(ApnsNotification message, boolean resent) {

            }

            public void messageSendFailed(ApnsNotification message, Throwable e) {
            }

            public void connectionClosed(DeliveryError e, int messageIdentifier) {
            }

            public void cacheLengthExceeded(int newCacheLength) {
                modifiedCacheLength.set(newCacheLength);
                sync.countDown();
            }

            public void notificationsResent(int resendCount) {
            }
        })
                .build();
        server.stopAt(eMsg1.length() * 5 + eMsg2.length() + eMsg3.length() * 14);
        for (int i = 0; i < 5; ++i) {
            service.push(eMsg1);
        }


        service.push(eMsg2);

        for (int i = 0; i < 101; ++i) {
            service.push(eMsg3);
        }

        server.sendError(8, eMsg2.getIdentifier());

        server.waitForError.release();
        server.messages.acquire();

        sync.await();


        Assert.assertTrue(ORIGINAL_CACHE_LENGTH < modifiedCacheLength.get());
    }

}
