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
package com.notnoop.apns.internal;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SillyTests {

    @Test
    public void testFeedbackCalls() {
        Map<String, Date> map = Collections.singletonMap("Test", new Date());
        ApnsFeedbackConnection feed = mock(ApnsFeedbackConnection.class);
        when(feed.getInactiveDevices()).thenReturn(map);

        ApnsServiceImpl service = new ApnsServiceImpl(null, feed);
        assertEquals(map, service.getInactiveDevices());

        // The feedback should be called once at most
        // Otherwise, we need to verify that the results of both
        // calls are accounted for
        verify(feed, times(1)).getInactiveDevices();
    }

    @Test
    public void testQueuedFeedbackCalls() {
        Map<String, Date> map = Collections.singletonMap("Test", new Date());
        ApnsFeedbackConnection feed = mock(ApnsFeedbackConnection.class);
        when(feed.getInactiveDevices()).thenReturn(map);

        ApnsServiceImpl service = new ApnsServiceImpl(null, feed);
        QueuedApnsService queued = new QueuedApnsService(service);
        assertEquals(map, queued.getInactiveDevices());

        // The feedback should be called once at most
        // Otherwise, we need to verify that the results of both
        // calls are accounted for
        verify(feed, times(1)).getInactiveDevices();
    }

}
