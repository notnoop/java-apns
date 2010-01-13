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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Signals that the the provided SSL context settings (e.g.
 * keystore path, password, encryption type, etc) are invalid
 *
 * This Exception can be caused by any of the following:
 *
 * <ol>
 * <li>{@link KeyStoreException}</li>
 * <li>{@link NoSuchAlgorithmException}</li>
 * <li>{@link CertificateException}</li>
 * <li>{@link IOException}</li>
 * <li>{@link UnrecoverableKeyException}</li>
 * <li>{@link KeyManagementException}</li>
 * </ol>
 *
 */
public class InvalidSSLConfig extends ApnsException {
    private static final long serialVersionUID = -7283168775864517167L;

    public InvalidSSLConfig()                      { super(); }
    public InvalidSSLConfig(String message)        { super(message); }
    public InvalidSSLConfig(Throwable cause)       { super(cause); }
    public InvalidSSLConfig(String m, Throwable c) { super(m, c); }

}
