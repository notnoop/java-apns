package com.notnoop.apns.integration;

import com.notnoop.apns.DeliveryError;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Matchers;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("deprecation")
public class ApnsSimulatorTest extends ApnsSimulatorTestBase {

    // final Logger logger = LoggerFactory.getLogger(ApnsSimulatorTest.class);

    //@Rule
    //public DumpThreadsOnErrorRule dump = new DumpThreadsOnErrorRule();

    @Rule
    public Timeout timeout = new Timeout(5000);


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
    public void RaceCondition() {
        // TODO implement test & decide if fix is necessary afterwards.
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

    @Test
    public void firstTokenBad_issue145() throws InterruptedException {
        // Test for Issue #145
        send(8, 0);
        assertNumberReceived(2);
    }
}
