/*
 * Copyright 2009, Mahmood Ali.
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
package com.notnoop.apns;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.notnoop.apns.internal.Utilities;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Represents a builder for constructing Payload requests, as
 * specified by Apple Push Notification Programming Guide.
 */
public final class PayloadBuilder {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, Object> root;
    private final Map<String, Object> aps;
    private final Map<String, Object> customAlert;

    /**
     * Constructs a new instance of {@code PayloadBuilder}
     */
    PayloadBuilder() {
        this.root = new HashMap<String, Object>();
        this.aps = new HashMap<String, Object>();
        this.customAlert = new HashMap<String, Object>();
    }

    /**
     * Sets the alert body text, the text the appears to the user,
     * to the passed value
     *
     * @param alert the text to appear to the user
     * @return  this
     */
    public PayloadBuilder alertBody(String alert) {
        aps.put("alert", alert);
        return this;
    }

    /**
     * Sets the alert sound to be played.
     *
     * @param sound the file name or song name to be played
     *              when receiving the notification
     * @return  this
     */
    public PayloadBuilder sound(String sound) {
        aps.put("sound", sound);
        return this;
    }

    /**
     * Sets the notification badge to be displayed next to the
     * application icon.
     *
     * The passed value is the value that should be displayed
     * (it will be added to the previous badge number), and
     * a badge of 0 clears the badge indicator.
     *
     * @param badge the badge number to be displayed
     * @return  this
     */
    public PayloadBuilder badge(int badge) {
        aps.put("badge", badge);
        return this;
    }

    /**
     * Requests clearing of the badge number next to the application
     * icon.
     *
     * This is an alias to {@code badge(0)}.
     *
     * @return this
     */
    public PayloadBuilder clearBadge() {
        return badge(0);
    }

    /**
     * Sets the value of action button (the right button to be
     * displayed).  The default value is "View".
     *
     * The value can be either the simple String to be displayed or
     * a localizable key, and the iPhone will show the appropriate
     * localized message.
     *
     * A {@code null} actionKey indicates no additional button
     * is displayed, just the Cancel button.
     *
     * @param actionKey the title of the additional button
     * @return  this
     */
    public PayloadBuilder actionKey(String actionKey) {
        customAlert.put("action-loc-key", actionKey);
        return this;
    }

    /**
     * Set the notification view to display an action button.
     *
     * This is an alias to {@code actionKey(null)}
     *
     * @return this
     */
    public PayloadBuilder noActionButton() {
        return actionKey(null);
    }

    /**
     * Set the notification localized key for the alert body
     * message.
     *
     * @param key   the localizable message body key
     * @return  this
     */
    public PayloadBuilder localizedKey(String key) {
        customAlert.put("loc-key", key);
        return this;
    }

    /**
     * Sets the arguments for the alert message localizable message.
     *
     * The iPhone doesn't localize the arguments.
     *
     * @param arguments the arguments to the localized alert message
     * @return  this
     */
    public PayloadBuilder localizedArguments(Collection<String> arguments) {
        customAlert.put("loc-args", arguments);
        return this;
    }

    /**
     * Sets the arguments for the alert message localizable message.
     *
     * The iPhone doesn't localize the arguments.
     *
     * @param arguments the arguments to the localized alert message
     * @return  this
     */
    public PayloadBuilder localizedArguments(String... arguments) {
        return localizedArguments(Arrays.asList(arguments));
    }

    /**
     * Sets any application-specific custom fields.  The values
     * are presented to the application and the iPhone doesn't
     * display them automatically.
     *
     * This can be used to pass specific values (urls, ids, etc) to
     * the application in addition to the notification message
     * itself.
     *
     * @param key   the custom field name
     * @param value the custom field value
     * @return  this
     */
    public PayloadBuilder customField(String key, Object value) {
        root.put(key, value);
        return this;
    }

    public int length() {
        String str = this.copy().toString();
        int payloadLength = Utilities.toUTF8Bytes(str).length;
        return payloadLength;
    }

    public boolean isTooLong() {
        return this.length() > Utilities.MAX_PAYLOAD_LENGTH;
    }

    public PayloadBuilder resizeAlertBody(int payloadLength) {
        int currLength = length();
        if (currLength < payloadLength)
            return this;

        int d = currLength - payloadLength;
        String body = (String)aps.get("alert");

        if (body.length() < d)
            aps.remove("alert");
        else
            aps.put("alert", body.subSequence(0, body.length() - d));

        return this;
    }

    public PayloadBuilder shrinkBody() {
        return resizeAlertBody(Utilities.MAX_PAYLOAD_LENGTH);
    }

    /**
     * Returns the JSON String representation of the payload
     * according to Apple APNS specification
     *
     * @return  the String representation as expected by Apple
     */
    public String build() {
        if (!(customAlert.isEmpty()
                || customAlert.equals(aps.get("alert")))) {
            if (aps.containsKey("alert")) {
                String alertBody = (String)aps.get("alert");
                customAlert.put("body", alertBody);
            }
            aps.put("alert", customAlert);
        }
        root.put("aps", aps);
        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return this.build();
    }

    private PayloadBuilder(Map<String, Object> root, Map<String, Object> aps, Map<String, Object> customAlert) {
        this.root = new HashMap<String, Object>(root);
        this.aps = new HashMap<String, Object>(aps);
        this.customAlert = new HashMap<String, Object>(customAlert);
    }

    public PayloadBuilder copy() {
        return new PayloadBuilder(this.root, this.aps, this.customAlert);
    }

    /**
     * @return a new instance of Payload Builder
     */
    public static PayloadBuilder newPayload() {
        return new PayloadBuilder();
    }
}
