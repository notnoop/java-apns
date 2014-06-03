/*
 * Copyright 2010, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import com.notnoop.apns.ReconnectPolicy;

public final class ReconnectPolicies {

    public static class Never implements ReconnectPolicy {

        public boolean shouldReconnect() { return false; }
        public void reconnected() { }
        public Never copy() { return this; }
    }

    public static class Always implements ReconnectPolicy {
        public boolean shouldReconnect() { return true; }
        public void reconnected() { }
        public Always copy() { return this; }
    }

    public static class EveryHalfHour implements ReconnectPolicy {
        private static final long PERIOD = 30 * 60 * 1000;

        private long lastRunning = System.currentTimeMillis();

        public boolean shouldReconnect() {
            return System.currentTimeMillis() - lastRunning > PERIOD;
        }

        public void reconnected() {
            lastRunning = System.currentTimeMillis();
        }

        public EveryHalfHour copy() {
            return new EveryHalfHour();
        }
    }
}
