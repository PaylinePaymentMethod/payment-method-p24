package com.payline.payment.p24.errors;

public class P24ValidationException extends Exception {


    private final String param;


    public P24ValidationException(String message) {
        super(message);
        param = "";
    }

    public P24ValidationException(String message, String param) {
        super(message);
        this.param = param;
    }


    public String getParam() {
        return param;
    }
}
