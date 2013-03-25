package com.notnoop.apns;

/**
 * A delegate that gets notified of failures.
 */
public interface ApnsServerExceptionDelegate {
	
	void handleRequestFailed(Throwable thr);
}