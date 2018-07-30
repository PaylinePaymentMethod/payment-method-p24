package com.payline.payment.p24.utils;

public enum P24Path {
    TEST("testConnection"),
    REGISTER("trnRegister"),
    REQUEST("/trnRequest/"),
    VERIFY("trnVerify");

    private String path;

    P24Path(String name) {
        this.path = name;
    }

    @Override
    public String toString() {
        return path;
    }
}