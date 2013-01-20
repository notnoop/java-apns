/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.notnoop.exceptions;

import com.notnoop.apns.DeliveryError;

/**
 *
 * @author kkirch
 */
public class ApnsDeliveryErrorException extends ApnsException {

    private final DeliveryError deliveryError;

    public ApnsDeliveryErrorException(DeliveryError error) {
        this.deliveryError = error;
    }

    @Override
    public String getMessage() {
        return "Failed to deliver notification with error code " + deliveryError.code();
    }

    public DeliveryError getDeliveryError() {
        return deliveryError;
    }
    
    
}
