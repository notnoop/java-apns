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

import com.notnoop.apns.utils.Simulator.ApnsServerSimulator.Notification;

public class ApnsNotificationWithAction {
    private final Notification notification;
    private final ApnsResponse response;

    public ApnsNotificationWithAction(Notification notification) {
        this(notification, ApnsResponse.doNothing());
    }

    public ApnsNotificationWithAction(Notification notification, ApnsResponse response) {
        if (notification == null)
        {
            throw new NullPointerException("notification cannot be null");
        }
        this.notification = notification;
        if (response == null)
        {
            throw new NullPointerException("response cannot be null");
        }
        this.response = response;
    }

    public Notification getNotification() {
        return notification;
    }

    public int getId() {
        return notification.getIdentifier();
    }

    public ApnsResponse getResponse() {
        return response;
    }

}
