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

import java.io.IOException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLContext;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.utils.ApnsServerStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.notnoop.apns.internal.ApnsFeedbackParsingUtils.*;
import static com.notnoop.apns.utils.FixedCertificates.*;
import static org.junit.Assert.*;

public class FeedbackTest {

    ApnsServerStub server;
    SSLContext clientContext = clientContext();


    @Before
    public void startup() {
        server = ApnsServerStub.prepareAndStartServer();
    }

    @After
    public void tearDown() {
        server.stop();
        server = null;
    }

    @Test
    public void simpleFeedback() throws IOException {
        server.getToSend().write(simple);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }
    
    @Test
    public void simpleFeedbackWithoutTimeout() throws IOException {
        server.getToSend().write(simple);
        server.getToWaitBeforeSend().set(2000);
        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .withReadTimeout(3000)
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }

    @Test()
    public void simpleFeedbackWithTimeout() throws IOException {
        server.getToSend().write(simple);
        server.getToWaitBeforeSend().set(5000);
        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .withReadTimeout(1000)
            .build();
        try {
            service.getInactiveDevices();
            fail("RuntimeException expected");
        }
        catch(RuntimeException e) {
            assertEquals("Socket timeout exception expected", 
                    SocketTimeoutException.class, e.getCause().getClass() );
        }
    }

    @Test
    public void threeFeedback() throws IOException {
        server.getToSend().write(three);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .build();

        checkParsedThree(service.getInactiveDevices());
    }

    @Test
    public void simpleQueuedFeedback() throws IOException {
        server.getToSend().write(simple);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .asQueued()
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }

    @Test
    public void threeQueuedFeedback() throws IOException {
        server.getToSend().write(three);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .asQueued()
            .build();

        checkParsedThree(service.getInactiveDevices());
    }

}
