package com.notnoop.apns;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Silly Tests
 */
public class APNSTest {

    @Test
    public void testInstances() {
        assertThat(APNS.newPayload(), isA(PayloadBuilder.class));
        assertThat(APNS.newService(), isA(ApnsServiceBuilder.class));
    }

    @Test
    public void payloadShouldGetNewInstances() {
        assertNotSame(APNS.newPayload(), APNS.newPayload());
    }

    @Test
    public void newServiceGetNewInstances() {
        assertNotSame(APNS.newService(), APNS.newService());
    }
}
