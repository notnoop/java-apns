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
