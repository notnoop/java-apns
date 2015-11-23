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
