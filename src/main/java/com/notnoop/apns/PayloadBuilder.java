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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notnoop.apns.internal.Utilities;

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
        root = new HashMap<String, Object>();
        aps = new HashMap<String, Object>();
        customAlert = new HashMap<String, Object>();
    }

    /**
     * Sets the alert body text, the text the appears to the user,
     * to the passed value
     *
     * @param alert the text to appear to the user
     * @return  this
     */
    public PayloadBuilder alertBody(final String alert) {
        customAlert.put("body", alert);
        return this;
    }

    /**
     * Sets the alert title text, the text the appears to the user,
     * to the passed value.
     *
     * Used on iOS 8.2, iWatch and also Safari
     *
     * @param title the text to appear to the user
     * @return  this
     */
    public PayloadBuilder alertTitle(final String title) {
        customAlert.put("title", title);
        return this;
    }

    /**
     * The key to a title string in the Localizable.strings file for the current localization.
     *
     * @param key  the localizable message title key
     * @return  this
     */
    public PayloadBuilder localizedTitleKey(final String key) {
        customAlert.put("title-loc-key", key);
        return this;
    }

    /**
     * Sets the arguments for the localizable title key.
     *
     * @param arguments the arguments to the localized alert message
     * @return  this
     */
    public PayloadBuilder localizedTitleArguments(final Collection<String> arguments) {
        customAlert.put("title-loc-args", arguments);
        return this;
    }

    /**
     * Sets the arguments for the localizable title key.
     *
     * @param arguments the arguments to the localized alert message
     * @return  this
     */
    public PayloadBuilder localizedTitleArguments(final String... arguments) {
        return localizedTitleArguments(Arrays.asList(arguments));
    }

    /**
     * Sets the alert action text
     *
     * @param action The label of the action button
     * @return  this
     */
    public PayloadBuilder alertAction(final String action) {
        customAlert.put("action", action);
        return this;
    }

    /**
     * Sets the "url-args" key that are paired with the placeholders
     * inside the urlFormatString value of your website.json file.
     * The order of the placeholders in the URL format string determines
     * the order of the values supplied by the url-args array.
     *
     * @param urlArgs the values to be paired with the placeholders inside
     *                the urlFormatString value of your website.json file.
     * @return  this
     */
    public PayloadBuilder urlArgs(final String... urlArgs){
        aps.put("url-args", urlArgs);
        return this;
    }

    /**
     * Sets the alert sound to be played.
     *
     * Passing {@code null} disables the notification sound.
     *
     * @param sound the file name or song name to be played
     *              when receiving the notification
     * @return  this
     */
    public PayloadBuilder sound(final String sound) {
        if (sound != null) {
            aps.put("sound", sound);
        } else {
            aps.remove("sound");
        }
        return this;
    }

    /**
     * Sets the category of the notification for iOS8 notification
     * actions.  See 13 minutes into "What's new in iOS Notifications"
     *
     * Passing {@code null} removes the category.
     *
     * @param category the name of the category supplied to the app
     *              when receiving the notification
     * @return  this
     */
    public PayloadBuilder category(final String category) {
        if (category != null) {
            aps.put("category", category);
        } else {
            aps.remove("category");
        }
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
    public PayloadBuilder badge(final int badge) {
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
    public PayloadBuilder actionKey(final String actionKey) {
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
     * Sets the notification type to be a 'newstand' notification.
     *
     * A Newstand Notification targets the Newstands app so that the app
     * updates the subscription info and content.
     *
     * @return this
     */
    public PayloadBuilder forNewsstand() {
        aps.put("content-available", 1);
        return this;
    }

    /**
     * With iOS7 it is possible to have the application wake up before the user opens the app.
     * 
     * The same key-word can also be used to send 'silent' notifications. With these 'silent' notification 
     * a different app delegate is being invoked, allowing the app to perform background tasks.
     *
     * @return this
     */
    public PayloadBuilder instantDeliveryOrSilentNotification() {
        aps.put("content-available", 1);
        return this;
    }

    /**
     * Set the notification localized key for the alert body
     * message.
     *
     * @param key   the localizable message body key
     * @return  this
     */
    public PayloadBuilder localizedKey(final String key) {
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
    public PayloadBuilder localizedArguments(final Collection<String> arguments) {
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
    public PayloadBuilder localizedArguments(final String... arguments) {
        return localizedArguments(Arrays.asList(arguments));
    }

    /**
     * Sets the launch image file for the push notification
     *
     * @param launchImage   the filename of the image file in the
     *      application bundle.
     * @return  this
     */
    public PayloadBuilder launchImage(final String launchImage) {
        customAlert.put("launch-image", launchImage);
        return this;
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
    public PayloadBuilder customField(final String key, final Object value) {
        root.put(key, value);
        return this;
    }

    public PayloadBuilder mdm(final String s) {
        return customField("mdm", s);
    }

    /**
     * Set any application-specific custom fields.  These values
     * are presented to the application and the iPhone doesn't
     * display them automatically.
     *
     * This method *adds* the custom fields in the map to the
     * payload, and subsequent calls add but doesn't reset the
     * custom fields.
     *
     * @param values   the custom map
     * @return  this
     */
    public PayloadBuilder customFields(final Map<String, ?> values) {
        root.putAll(values);
        return this;
    }

    /**
     * Returns the length of payload bytes once marshaled to bytes
     *
     * @return the length of the payload
     */
    public int length() {
        return copy().buildBytes().length;
    }

    /**
     * Returns true if the payload built so far is larger than
     * the size permitted by Apple (which is 2048 bytes).
     *
     * @return true if the result payload is too long
     */
    public boolean isTooLong() {
        return length() > Utilities.MAX_PAYLOAD_LENGTH;
    }

    /**
     * Shrinks the alert message body so that the resulting payload
     * message fits within the passed expected payload length.
     *
     * This method performs best-effort approach, and its behavior
     * is unspecified when handling alerts where the payload
     * without body is already longer than the permitted size, or
     * if the break occurs within word.
     *
     * @param payloadLength the expected max size of the payload
     * @return  this
     */
    public PayloadBuilder resizeAlertBody(final int payloadLength) {
        return resizeAlertBody(payloadLength, "");
    }

    /**
     * Shrinks the alert message body so that the resulting payload
     * message fits within the passed expected payload length.
     *
     * This method performs best-effort approach, and its behavior
     * is unspecified when handling alerts where the payload
     * without body is already longer than the permitted size, or
     * if the break occurs within word.
     *
     * @param payloadLength the expected max size of the payload
     * @param postfix for the truncated body, e.g. "..."
     * @return  this
     */
    public PayloadBuilder resizeAlertBody(final int payloadLength, final String postfix) {
        int currLength = length();
        if (currLength <= payloadLength) {
            return this;
        }

        // now we are sure that truncation is required
        String body = (String)customAlert.get("body");

        final int acceptableSize = Utilities.toUTF8Bytes(body).length
                - (currLength - payloadLength
                        + Utilities.toUTF8Bytes(postfix).length);
        body = Utilities.truncateWhenUTF8(body, acceptableSize) + postfix;

        // set it back
        customAlert.put("body", body);

        // calculate the length again
        currLength = length();

        if(currLength > payloadLength) {
            // string is still too long, just remove the body as the body is
            // anyway not the cause OR the postfix might be too long
            customAlert.remove("body");
        }

        return this;
    }

    /**
     * Shrinks the alert message body so that the resulting payload
     * message fits within require Apple specification (2048 bytes).
     *
     * This method performs best-effort approach, and its behavior
     * is unspecified when handling alerts where the payload
     * without body is already longer than the permitted size, or
     * if the break occurs within word.
     *
     * @return  this
     */
    public PayloadBuilder shrinkBody() {
        return shrinkBody("");
    }

    /**
     * Shrinks the alert message body so that the resulting payload
     * message fits within require Apple specification (2048 bytes).
     *
     * This method performs best-effort approach, and its behavior
     * is unspecified when handling alerts where the payload
     * without body is already longer than the permitted size, or
     * if the break occurs within word.
     *
     * @param postfix for the truncated body, e.g. "..."
     *
     * @return  this
     */
    public PayloadBuilder shrinkBody(final String postfix) {
        return resizeAlertBody(Utilities.MAX_PAYLOAD_LENGTH, postfix);
    }

    /**
     * Returns the JSON String representation of the payload
     * according to Apple APNS specification
     *
     * @return  the String representation as expected by Apple
     */
    public String build() {
        if (!root.containsKey("mdm")) {
            insertCustomAlert();
            root.put("aps", aps);
        }
        try {
            return mapper.writeValueAsString(root);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void insertCustomAlert() {
        switch (customAlert.size()) {
            case 0:
                aps.remove("alert");
                break;
            case 1:
                if (customAlert.containsKey("body")) {
                    aps.put("alert", customAlert.get("body"));
                    break;
                }
                // else follow through
                //$FALL-THROUGH$
            default:
                aps.put("alert", customAlert);
        }
    }

    /**
     * Returns the bytes representation of the payload according to
     * Apple APNS specification
     *
     * @return the bytes as expected by Apple
     */
    public byte[] buildBytes() {
        return Utilities.toUTF8Bytes(build());
    }

    @Override
    public String toString() {
        return build();
    }

    private PayloadBuilder(final Map<String, Object> root,
            final Map<String, Object> aps,
            final Map<String, Object> customAlert) {
        this.root = new HashMap<String, Object>(root);
        this.aps = new HashMap<String, Object>(aps);
        this.customAlert = new HashMap<String, Object>(customAlert);
    }

    /**
     * Returns a copy of this builder
     *
     * @return a copy of this builder
     */
    public PayloadBuilder copy() {
        return new PayloadBuilder(root, aps, customAlert);
    }

    /**
     * @return a new instance of Payload Builder
     */
    public static PayloadBuilder newPayload() {
        return new PayloadBuilder();
    }
}
