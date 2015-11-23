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
package com.notnoop.apns.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.StartSendingApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.apns.utils.ApnsServerStub;
import com.notnoop.apns.utils.FixedCertificates;
import org.junit.*;

import static com.notnoop.apns.utils.FixedCertificates.*;

@SuppressWarnings("deprecation")
public class ApnsConnectionCacheTest {

    private ApnsServerStub server;
    private static SimpleApnsNotification msg1 = new SimpleApnsNotification("a87d8878d878a79", "{\"aps\":{}}");
    private static SimpleApnsNotification msg2 = new SimpleApnsNotification("a87d8878d878a88", "{\"aps\":{}}");
    private static EnhancedApnsNotification eMsg1 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    private static EnhancedApnsNotification eMsg2 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    private static EnhancedApnsNotification eMsg3 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
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

        server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        //5 success 1 fail 7 success 7 resent
        final CountDownLatch sync = new CountDownLatch(20);
        final AtomicInteger numResent = new AtomicInteger();
        final AtomicInteger numSent = new AtomicInteger();
        final AtomicInteger numStartSend = new AtomicInteger();
        int EXPECTED_RESEND_COUNT = 7;
        int EXPECTED_SEND_COUNT = 12;
        server.getWaitForError().acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
                .withDelegate(new StartSendingApnsDelegate() {

            public void startSending(final ApnsNotification message, final boolean resent) {
                if (!resent) {
                    numStartSend.incrementAndGet();
                }
            }

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

        server.getWaitForError().release();
        server.getMessages().acquire();

        sync.await();

        Assert.assertEquals(EXPECTED_RESEND_COUNT, numResent.get());
        Assert.assertEquals(EXPECTED_SEND_COUNT, numSent.get());
        Assert.assertEquals(EXPECTED_SEND_COUNT + 1, numStartSend.get());

    }

    /**
     * Test2 to make sure that after rejected notification
     * in-flight notifications are re-transmitted
     *
     * @throws InterruptedException
     */
    @Test(timeout = 5000)
    public void handleReTransmissionError1Good1Bad2Good() throws InterruptedException {
        server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        final CountDownLatch sync = new CountDownLatch(6);
        final AtomicInteger numResent = new AtomicInteger();
        final AtomicInteger numSent = new AtomicInteger();
        final AtomicInteger numStartSend = new AtomicInteger();
        int EXPECTED_RESEND_COUNT = 2;
        int EXPECTED_SEND_COUNT = 3;
        server.getWaitForError().acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
                .withDelegate(new StartSendingApnsDelegate() {

            public void startSending(final ApnsNotification message, final boolean resent) {
                if (!resent) {
                    numStartSend.incrementAndGet();
                }
            }

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
        server.getWaitForError().release();
        server.getMessages().acquire();

        sync.await();

        Assert.assertEquals(EXPECTED_RESEND_COUNT, numResent.get());
        Assert.assertEquals(EXPECTED_SEND_COUNT, numSent.get());
        Assert.assertEquals(EXPECTED_SEND_COUNT + 1, numStartSend.get());

    }

    /**
     * Test to make sure single rejected notifications are returned
     *
     * @throws InterruptedException
     */
    @Test(timeout = 5000)
    public void handleReTransmissionError1Bad() throws InterruptedException {

        server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        final CountDownLatch sync = new CountDownLatch(1);
        final AtomicInteger numError = new AtomicInteger();
        final AtomicInteger numStartSend = new AtomicInteger();
        int EXPECTED_ERROR_COUNT = 1;
        server.getWaitForError().acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
                .withDelegate(new StartSendingApnsDelegate() {

            public void startSending(final ApnsNotification message, final boolean resent) {
                if (!resent) {
                    numStartSend.incrementAndGet();
                }
            }

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
        server.getWaitForError().release();
        server.getMessages().acquire();

        sync.await();

        Assert.assertEquals(EXPECTED_ERROR_COUNT, numError.get());
        Assert.assertEquals(EXPECTED_ERROR_COUNT, numStartSend.get());
    }

    /**
     * Test to make sure that after rejected notification
     * in-flight notifications are re-transmitted with a queued connection
     *
     * @throws InterruptedException
     */
    @Ignore("Fails because old ApnsServerStub does not accept() on the connection socket for all the time.")
    @Test(timeout = 10000)
    public void handleTransmissionErrorInQueuedConnection() throws InterruptedException {
        server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        final AtomicInteger sync = new AtomicInteger(138);
        final AtomicInteger numResent = new AtomicInteger();
        final AtomicInteger numSent = new AtomicInteger();
        server.getWaitForError().acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
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
        server.getWaitForError().release();
        server.getMessages().acquire();

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

        server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        final CountDownLatch sync = new CountDownLatch(1);
        int ORIGINAL_CACHE_LENGTH = 100;
        final AtomicInteger modifiedCacheLength = new AtomicInteger();
        server.getWaitForError().acquire();
        server.start();
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
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

        server.getWaitForError().release();
        server.getMessages().acquire();

        sync.await();


        Assert.assertTrue(ORIGINAL_CACHE_LENGTH < modifiedCacheLength.get());
    }

}
