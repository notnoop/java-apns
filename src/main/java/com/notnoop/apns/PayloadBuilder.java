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

import net.sf.json.JSONObject;

public final class PayloadBuilder {
	private JSONObject root;
    private JSONObject aps;
    private CustomAlertBuilder customAlert;

    PayloadBuilder() {
        this.root = new JSONObject();
        this.aps = new JSONObject();
    }

    public PayloadBuilder alert(String alert) {
        aps.put("alert", alert);
        return this;
    }

    public PayloadBuilder sound(String sound) {
        aps.put("sound", sound);
        return this;
    }

    public PayloadBuilder badge(int badge) {
        aps.put("badge", badge);
        return this;
    }

    public CustomAlertBuilder customAlert() {
    	customAlert = new CustomAlertBuilder();
    	return customAlert;
    }

    public PayloadBuilder customField(String key, Object value) {
    	root.put(key, value);
    	return this;
    }

    public String build() {
    	if (customAlert != null)
    		aps.put("alert", customAlert.build());
    	root.put("aps", aps);
        return root.toString();
    }

    @Override
    public String toString() {
        return this.build();
    }

    public static class CustomAlertBuilder {
    	private JSONObject aps;

    	public CustomAlertBuilder() {
    		this.aps = new JSONObject();
    	}

    	public CustomAlertBuilder body(String body) {
    		aps.put("body", body);
    		return this;
    	}

    	public CustomAlertBuilder actionKey(String actionKey) {
    		if (actionKey == null)
    			actionKey = "null";
    		aps.put("action-loc-key", actionKey);
    		return this;
    	}

    	public CustomAlertBuilder localizedKey(String key) {
    		aps.put("loc-key", key);
    		return this;
    	}

    	public CustomAlertBuilder localizedArguments(Collection<String> arguments) {
    		aps.put("loc-args", arguments);
    		return this;
    	}

    	public CustomAlertBuilder localizedArguments(String[] arguments) {
    		return localizedArguments(Arrays.asList(arguments));
    	}

    	public String build() {
    		return aps.toString();
    	}

    	public String toString() {
    		return build();
    	}
    }
}
