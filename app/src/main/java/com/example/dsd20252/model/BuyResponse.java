package com.example.dsd20252.model;

import java.io.Serializable;

/**
 * Response object for purchase operations
 */
public class BuyResponse implements Serializable {
    private boolean success;
    private String message;

    public BuyResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}