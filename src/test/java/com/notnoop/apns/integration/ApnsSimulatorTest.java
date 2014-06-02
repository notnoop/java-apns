package com.notnoop.apns.integration;

import com.notnoop.apns.*;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.apns.utils.FixedCertificates;
import com.notnoop.apns.utils.Simulator.FailingApnsServerSimulator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.mockito.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.notnoop.apns.utils.FixedCertificates.LOCALHOST;
import static com.notnoop.apns.utils.FixedCertificates.clientContext;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("deprecation")
public class ApnsSimulatorTest {

    final Logger logger = LoggerFactory.getLogger(ApnsSimulatorTest.class);

    //@Rule
    //public DumpThreadsOnErrorRule dump = new DumpThreadsOnErrorRule();

    @Rule
    public TestName name = new TestName();

    @Rule
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

        logger.info("\n\n\n\n\n");
        logger.info("********* Test: {}", name.getMethodName());

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
        server.getQueue().poll(5, TimeUnit.SECONDS);
        assertIdle();
        assertDelegateSentCount(1);
    }

    @Test
    public void sendThree() throws InterruptedException {
        sendCount(3, 0);
        assertNumberReceived(3);
        assertDelegateSentCount(3);
    }

    @Test
    public void sendThousand() throws InterruptedException {
        TestLoggerFactory.getInstance().setPrintLevel(Level.INFO);
        sendCount(1000, 0);
        assertNumberReceived(1000);
        assertDelegateSentCount(1000);
    }


    @Test
    public void sendDelay() throws InterruptedException {
        send(-3);
        server.getQueue().poll(5, TimeUnit.SECONDS);
        assertIdle();
        assertDelegateSentCount(1);
    }

    @Test
    public void testConnectionClose() throws InterruptedException {
        send(8);
        assertNumberReceived(1);
        assertDelegateSentCount(1);
        verify(delegate, times(1)).connectionClosed(Matchers.any(DeliveryError.class), Matchers.anyInt());
    }

    @Test
    public void handleRetransmissionWithSeveralOutstandingMessages() throws InterruptedException {
        send(-1, -1, -1, -1, -1, 8, -1, -1, -1, -1, -1, -1, -1);
        assertNumberReceived(13);
        assertDelegateSentCount(13 + 7); // Initially sending all 13 notifications, then resend the last 7 ones
        verify(delegate, times(1)).connectionClosed(Matchers.any(DeliveryError.class), Matchers.anyInt());
    }


    @Test
    public void testClientDoesNotResendMessagesWhenServerClosesSocketWithoutErrorPacket() throws InterruptedException {
        send(-1, -1, -1, -1, -1, -100, -1, -1, -1, -1, -1, -1, -1);
        assertNumberReceived(6);
    }

    @Ignore
    @Test
    public void Racecondition() {
        // TODO implement test & decide if fix is neccessary afterwards.
        Assert.fail("Assumption: monitoring thread crashes in read() when the sender thread closes the connection first.");
        // Thus the last feedback message gets lost, thus we lose messages.
    }

    @Test
    public void abortNoWait() throws InterruptedException {
        send(8, 0);
        assertNumberReceived(2);
    }

    @Test
    public void doNotSpamLogWhenConnectionClosesBetweenFeedbackPackets() throws InterruptedException {
        // Don't spam a lot of information into the log when the socket closes at a "legal" location. (Just before
        // or after a feedback packet)
        send(-1, 8, -1);
        assertNumberReceived(3);
        final List<LoggingEvent> allLoggingEvents = TestLoggerFactory.getAllLoggingEvents();
        assertThat(allLoggingEvents, not(hasItem(eventContains("Exception while waiting for error code"))));
    }

    @Ignore("fails")
    @Test
    public void firstTokenBad_issue145() throws InterruptedException {
        // Test for Issue #145
        send(8, -1);
        assertNumberReceived(2);
    }

    @Ignore("fails")
    @Test
    public void multipleTokensBad_issue145() throws InterruptedException {
        send(8, 0, 8, 0, 8, 0 ,8, 0);
        assertNumberReceived(8);

    }

    private Matcher<? super LoggingEvent> eventContains(final String substr) {
        return new BaseMatcher<LoggingEvent>() {
            @Override
            public boolean matches(final Object item) {
                final String message = ((LoggingEvent) item).getMessage();
                return message.contains(substr);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("substring [" + substr + "]");
            }
        };
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
     *             <ul>
     *             <li>-100: Drop connection</li>
     *             <li>below zero: wait (-code) number times a tenth of a second</li>
     *             <li>above zero: send code as APNS error message then drop connection</li>
     *             </ul>
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
        for (int i = 0; i < count; ++i) {
            send(code);
        }
    }

    private void assertNumberReceived(final int count) throws InterruptedException {
        logger.debug("assertNumberReceived {}", count);
        int i = 0;
        try {
            for (; i < count; ++i) {
                server.getQueue().poll(5, TimeUnit.SECONDS);
            }
        } catch (RuntimeException re) {
            logger.error("Exception in assertNumberReceived, took {}", i);
            throw re;
        }
        logger.debug("assertNumberReceived - successfully took {}", count);
        assertIdle();
    }

    private void assertIdle() throws InterruptedException {
        logger.info("assertIdle");
        Thread.sleep(1000);
        assertThat(server.getQueue().size(), equalTo(0));
    }

    private void assertDelegateSentCount(final int count) {
        logger.info("assertDelegateSentCount {}", count);
        verify(delegate, times(count)).messageSent(Matchers.any(ApnsNotification.class), Matchers.anyBoolean());
    }
}
