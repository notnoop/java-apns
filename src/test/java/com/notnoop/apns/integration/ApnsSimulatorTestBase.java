package com.notnoop.apns.integration;

import com.notnoop.apns.*;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.apns.utils.FixedCertificates;
import com.notnoop.apns.utils.Simulator.ApnsServerSimulator;
import com.notnoop.apns.utils.Simulator.FailingApnsServerSimulator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.mockito.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.notnoop.apns.utils.FixedCertificates.LOCALHOST;
import static com.notnoop.apns.utils.FixedCertificates.clientContext;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ApnsSimulatorTestBase {
    final Logger logger = LoggerFactory.getLogger(ApnsSimulatorTestBase.class);

    private static final String payload = "{\"aps\":{}}";
    @Rule
    public TestName name = new TestName();
    protected FailingApnsServerSimulator server;
    protected ApnsDelegate delegate;
    private ApnsService service;
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

    protected Matcher<? super LoggingEvent> eventContains(final String subString) {
        return new BaseMatcher<LoggingEvent>() {
            @Override
            public boolean matches(final Object item) {
                final String message = ((LoggingEvent) item).getMessage();
                return message.contains(subString);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("substring [" + subString + "]");
            }
        };
    }

    protected void send(final int... codes) {
        for (int code : codes) {
            send(code);
        }
    }

    protected void send(final int code) {

        ApnsNotification notification = makeNotification(code);
        service.push(notification);

    }

    /**
     * Create an APNS notification that creates specified "error" behaviour in the
     * {@link com.notnoop.apns.utils.Simulator.FailingApnsServerSimulator}
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

    protected void sendCount(final int count, final int code) {
        for (int i = 0; i < count; ++i) {
            send(code);
        }
    }

    protected void assertNumberReceived(final int count) throws InterruptedException {
        logger.debug("assertNumberReceived {}", count);
        int i = 0;
        try {
            for (; i < count; ++i) {
                ApnsServerSimulator.Notification notification = server.getQueue().poll(5, TimeUnit.SECONDS);
                logger.debug("Polled notification {}", notification);
                if (notification == null)
                    throw new RuntimeException("poll timed out");
            }
        } catch (RuntimeException re) {
            logger.error("Exception in assertNumberReceived, took {}", i);
            throw re;
        }
        logger.debug("assertNumberReceived - successfully took {}", count);
        assertIdle();
    }

    protected void assertIdle() throws InterruptedException {
        logger.info("assertIdle");
        Thread.sleep(1000);
        assertThat(server.getQueue().size(), equalTo(0));
    }

    protected void assertDelegateSentCount(final int count) {
        logger.info("assertDelegateSentCount {}", count);
        verify(delegate, times(count)).messageSent(Matchers.any(ApnsNotification.class), Matchers.anyBoolean());
    }
}
