package com.payline.payment.p24.utils;

public enum P24Path {
    CHECK("testConnection"),
    REGISTER("trnRegister"),
    REQUEST("/trnRequest/"),
    VERIFY("trnVerify");

    private String path;

    P24Path(String name) {
        this.path = name;
    }

    public String getPath() {
        return path;
    }
}