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
