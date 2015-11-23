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
package com.notnoop.apns.utils.Simulator;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.DeliveryError;

public class ApnsResponse {

    private final Action action;
    private final DeliveryError error;
    private final int errorId;

    private ApnsResponse(Action action, DeliveryError error, int errorId) {
        this.action = action;
        this.error = error;
        this.errorId = errorId;
    }

    public boolean isDoNothing() {
        return action == Action.DO_NOTHING;
    }

    public Action getAction() {
        return action;
    }

    public DeliveryError getError() {
        return error;
    }

    public int getErrorId() {
        return errorId;
    }

    public static ApnsResponse doNothing() {
        return new ApnsResponse(Action.DO_NOTHING, null, 0);
    }

    public static ApnsResponse returnError(DeliveryError error, int errorId) {
        return new ApnsResponse(Action.RETURN_ERROR, error, errorId);
    }

    public static ApnsResponse returnErrorAndShutdown(DeliveryError error, int errorId) {
        return new ApnsResponse(Action.RETURN_ERROR_AND_SHUTDOWN, error, errorId);
    }

    public static ApnsResponse returnErrorAndShutdown(DeliveryError error, ApnsNotification notification) {
        return new ApnsResponse(Action.RETURN_ERROR_AND_SHUTDOWN, error, notification.getIdentifier());
    }

}
