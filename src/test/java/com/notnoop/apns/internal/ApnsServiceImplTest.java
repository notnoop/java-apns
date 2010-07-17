package com.notnoop.apns.internal;

import org.junit.Test;
import static org.mockito.Mockito.*;

import com.notnoop.apns.ApnsService;
import com.notnoop.apns.SimpleApnsNotification;

public class ApnsServiceImplTest {

    SimpleApnsNotification notification = new SimpleApnsNotification("2342", "{}");

    @Test
    public void pushEvantually() {
        ApnsConnection connection = mock(ApnsConnection.class);
        ApnsService service = newService(connection, null);

        service.push(notification);

        verify(connection, times(1)).sendMessage(notification);
    }

    @Test
    public void pushEvantuallySample() {
        ApnsConnection connection = mock(ApnsConnection.class);
        ApnsService service = newService(connection, null);

        service.push("2342", "{}");

        verify(connection, times(1)).sendMessage(notification);
    }

    protected ApnsService newService(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        return new ApnsServiceImpl(connection, null);
    }
}
