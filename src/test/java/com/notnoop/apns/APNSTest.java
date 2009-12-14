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
        assertThat(APNS.newPayload(), is(PayloadBuilder.class));
        assertThat(APNS.newService(), is(ApnsServiceBuilder.class));
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
