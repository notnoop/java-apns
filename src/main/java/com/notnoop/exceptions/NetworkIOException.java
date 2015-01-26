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

package com.notnoop.exceptions;

import java.io.IOException;

/**
 * Thrown to indicate that that a network operation has failed:
 * (e.g. connectivity problems, domain cannot be found, network
 * dropped).
 */
public class NetworkIOException extends ApnsException {
    private static final long serialVersionUID = 3353516625486306533L;

    private boolean resend;

    public NetworkIOException()                      { super(); }
    public NetworkIOException(String message)        { super(message); }
    public NetworkIOException(IOException cause)       { super(cause); }
    public NetworkIOException(String m, IOException c) { super(m, c); }
    public NetworkIOException(IOException cause, boolean resend) {
        super(cause);
        this.resend = resend;
    }

    /**
     * Identifies whether an exception was thrown during a resend of a
     * message or not.  In this case a resend refers to whether the
     * message is being resent from the buffer of messages internal.
     * This would occur if we sent 5 messages quickly to APNS:
     * 1,2,3,4,5 and the 3 message was rejected.  We would
     * then need to resend 4 and 5.  If a network exception was
     * triggered when doing this, then the resend flag will be
     * {@code true}.
     * @return {@code true} for an exception trigger during a resend, otherwise {@code false}.
     */
    public boolean isResend() {
        return resend;
    }

}
