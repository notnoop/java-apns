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
    private JSONObject customAlert;

    PayloadBuilder() {
        this.root = new JSONObject();
        this.aps = new JSONObject();
        this.customAlert = new JSONObject();
    }

    public PayloadBuilder alertBody(String alert) {
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

	public PayloadBuilder actionKey(String actionKey) {
		if (actionKey == null)
			actionKey = "null";
		customAlert.put("action-loc-key", actionKey);
		return this;
	}

	public PayloadBuilder noActionButton() {
		return actionKey(null);
	}

	public PayloadBuilder localizedKey(String key) {
		customAlert.put("loc-key", key);
		return this;
	}

	public PayloadBuilder localizedArguments(Collection<String> arguments) {
		customAlert.put("loc-args", arguments);
		return this;
	}

	public PayloadBuilder localizedArguments(String... arguments) {
		return localizedArguments(Arrays.asList(arguments));
	}

    public PayloadBuilder customField(String key, Object value) {
    	root.put(key, value);
    	return this;
    }

    public String build() {
    	if (!customAlert.isEmpty()) {
    		if (aps.containsKey("alert")) {
    			String alertBody = aps.getString("alert");
    			customAlert.put("body", alertBody);
    		}
    		aps.put("alert", customAlert);
    	}
    	root.put("aps", aps);
        return root.toString();
    }

    @Override
    public String toString() {
        return this.build();
    }
}
