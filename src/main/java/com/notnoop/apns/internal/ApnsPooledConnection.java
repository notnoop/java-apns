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

import java.util.concurrent.*;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.exceptions.NetworkIOException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApnsPooledConnection implements ApnsConnection {
    private static final Logger logger = LoggerFactory.getLogger(ApnsPooledConnection.class);

    private final ApnsConnection prototype;
    private final int max;

    private final ExecutorService executors;
    private final ConcurrentLinkedQueue<ApnsConnection> prototypes;

    public ApnsPooledConnection(ApnsConnection prototype, int max) {
        this(prototype, max, Executors.newFixedThreadPool(max));
    }

    public ApnsPooledConnection(ApnsConnection prototype, int max, ExecutorService executors) {
        this.prototype = prototype;
        this.max = max;

        this.executors = executors;
        this.prototypes = new ConcurrentLinkedQueue<ApnsConnection>();
    }

    private final ThreadLocal<ApnsConnection> uniquePrototype =
        new ThreadLocal<ApnsConnection>() {
        protected ApnsConnection initialValue() {
            ApnsConnection newCopy = prototype.copy();
            prototypes.add(newCopy);
            return newCopy;
        }
    };

    public void sendMessage(final ApnsNotification m) throws NetworkIOException {
        Future<Void> future = executors.submit(new Callable<Void>() {
            public Void call() throws Exception {
                uniquePrototype.get().sendMessage(m);
                return null;
            }
        });
        try {
            future.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ee) {
            if (ee.getCause() instanceof NetworkIOException) {
                throw (NetworkIOException) ee.getCause();
            }
        }
    }

    public ApnsConnection copy() {
        // TODO: Should copy executor properly.... What should copy do
        // really?!
        return new ApnsPooledConnection(prototype, max);
    }

    public void close() {
        executors.shutdown();
        try {
            executors.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("pool termination interrupted", e);
        }
        for (ApnsConnection conn : prototypes) {
            Utilities.close(conn);
        }
        Utilities.close(prototype);
    }

    public void testConnection() {
        prototype.testConnection();
    }

    public synchronized void setCacheLength(int cacheLength) {  
        for (ApnsConnection conn : prototypes) {
            conn.setCacheLength(cacheLength);
        }
    }

    @SuppressFBWarnings(value = "UG_SYNC_SET_UNSYNC_GET", justification = "prototypes is a MT-safe container")
    public int getCacheLength() {
        return prototypes.peek().getCacheLength();
    }
}
