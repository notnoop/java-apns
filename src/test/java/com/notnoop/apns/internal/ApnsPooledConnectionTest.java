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

import com.notnoop.apns.ApnsNotification;
import com.notnoop.exceptions.NetworkIOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

public class ApnsPooledConnectionTest {

    private ApnsConnection errorPrototype;
    private ApnsConnection prototype;

    private ExecutorService executorService;

    @Before
    public void setup() {
        errorPrototype = mock(ApnsConnection.class);
        when(errorPrototype.copy()).thenReturn(errorPrototype);
        doThrow(NetworkIOException.class).when(errorPrototype).sendMessage(any(ApnsNotification.class));

        prototype = mock(ApnsConnection.class);
        when(prototype.copy()).thenReturn(prototype);
    }

    @After
    public void cleanup() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Ignore
    @Test(expected = NetworkIOException.class)
    public void testSendMessage() throws Exception {
        ApnsPooledConnection conn = new ApnsPooledConnection(errorPrototype, 1, getSingleThreadExecutor());
        conn.sendMessage(mock(ApnsNotification.class));
    }

    @Test
    public void testCopyCalls() throws Exception {
        ApnsPooledConnection conn = new ApnsPooledConnection(prototype, 1, getSingleThreadExecutor());
        for (int i = 0; i < 10; i++) {
            conn.sendMessage(mock(ApnsNotification.class));
        }
        verify(prototype, times(1)).copy();
    }

    @Test
    public void testCloseCalls() throws Exception {
        ApnsPooledConnection conn = new ApnsPooledConnection(prototype, 1, getSingleThreadExecutor());
        conn.sendMessage(mock(ApnsNotification.class));
        conn.close();
        // should be closed twice because of the thread local copy
        verify(prototype, times(2)).close();
    }

    private ExecutorService getSingleThreadExecutor() {
        executorService = Executors.newSingleThreadExecutor();
        return executorService;
    }
}