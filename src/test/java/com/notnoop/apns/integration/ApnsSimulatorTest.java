package com.notnoop.apns.integration;

import java.util.Random;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.apns.utils.FixedCertificates;
import com.notnoop.apns.utils.Simulator.FailingApnsServerSimulator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Matchers;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import static com.notnoop.apns.utils.FixedCertificates.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("deprecation")
public class ApnsSimulatorTest {

    //@Rule
    public Timeout globalTimeout = new Timeout(5000);


    private static final String payload = "{\"aps\":{}}";
    private ApnsService service;
    private FailingApnsServerSimulator server;
    private ApnsDelegate delegate;
    private Random random;

    @Before
    public void startup() {
        // http://projects.lidalia.org.uk/slf4j-test/
        TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG);
        TestLoggerFactory.clearAll();



        server = new FailingApnsServerSimulator(FixedCertificates.serverContext().getServerSocketFactory());
        server.start();
        delegate = ApnsDelegate.EMPTY;
        delegate = mock(ApnsDelegate.class);
        service = APNS.newService()
                .withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
                .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
                .withDelegate(delegate).build();
        random = new Random();
    }

    @After
    public void tearDown() {
        server.stop();
        server = null;
    }

    @Test
    public void sendOne() throws InterruptedException {
        send(0);
        server.getQueue().take();
        assertIdle();
        assertDelegateSentCount(1);
    }

    @Test
    public void sendThousand() throws InterruptedException {
        sendCount(1000, 0);
        assertNumberReceived(1000);
        assertDelegateSentCount(1000);
    }


    @Test
    public void sendDelay() throws InterruptedException {
        send(-3);
        server.getQueue().take();
        assertIdle();
        assertDelegateSentCount(1);
    }

    @Ignore("Failing - getting 2 callbacks on connectionclosed")
    @Test
    public void testConnectionClose() throws InterruptedException {
        send(8);
        assertNumberReceived(1);
        Thread.sleep(3000);
        assertDelegateSentCount(1);
        verify(delegate, times(1)).connectionClosed(Matchers.any(DeliveryError.class), Matchers.anyInt());
    }

    @Test
    public void handleRetransmissionWithSeveralOutstandingMessages() throws InterruptedException {
        send(0, 0, -1, -1, -1, 8, -1, -1, -1, -1, 0, 0, 0);
        assertNumberReceived(13);
        assertDelegateSentCount(13);
        verify(delegate, times(1)).connectionClosed(Matchers.any(DeliveryError.class), Matchers.anyInt());

    }

    private void send(final int... codes) {
        for (int code : codes) {
            send(code);
        }
    }

    private void send(final int code) {

        ApnsNotification notification = makeNotification(code);
        service.push(notification);

    }

    /**
     * Create an APNS notification that creates specified "error" behaviour in the
     * {@link FailingApnsServerSimulator}
     *
     * @param code A code specifying the FailingApnsServer's behaviour.
     *            <ul>
     *                  <li>-100: Drop connection</li>
     *                  <li>below zero: wait (-code) number times a tenth of a second</li>
     *                  <li>above zero: send code as APNS error message then drop connection</li>
     *            </ul>
     * @return an APNS notification
     */
    private EnhancedApnsNotification makeNotification(final int code) {
        byte[] deviceToken = new byte[32];
        random.nextBytes(deviceToken);
        if (code == 0) {
            deviceToken[0] = 42;
        } else {
            deviceToken[0] = (byte) 0xff;
            deviceToken[1] = (byte) 0xff;

            if (code < -100) {
                // Drop connection
                deviceToken[2] = (byte) 2;
            } else if (code < 0) {
                // Sleep
                deviceToken[2] = (byte) 1;
                deviceToken[3] = (byte) -code;
            } else {
                // Send APNS error response then drop connection
                assert code > 0;
                deviceToken[2] = (byte) 0;
                deviceToken[3] = (byte) code;

            }

        }
        return new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(), 1, deviceToken, Utilities.toUTF8Bytes(payload));
    }

    private void sendCount(final int count, final int code) {
        for (int i = 0; i< count; ++i) {
            send(code);
        }
    }

    private void assertNumberReceived(final int count) throws InterruptedException {
        for (int i = 0; i< count; ++i) {
            server.getQueue().take();
        }
        assertIdle();
    }

    private void assertIdle() throws InterruptedException {
        Thread.sleep(50);
        Assert.assertThat(server.getQueue().size(), equalTo(0));
    }

    private void assertDelegateSentCount(final int count) {
        verify(delegate, times(count)).messageSent(Matchers.any(ApnsNotification.class), Matchers.anyBoolean() );
    }
}
