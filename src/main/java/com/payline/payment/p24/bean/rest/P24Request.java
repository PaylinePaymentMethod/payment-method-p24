package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.Map;

public abstract class P24Request {

    private String merchantId;
    private String posId;
    private String key;

    public P24Request(String merchantId, String posId, String key) {
        this.merchantId = merchantId;
        this.posId = posId;
        this.key = key;
    }

    public P24Request(PaymentRequest paymentRequest) {
        this.merchantId = paymentRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_ID).getValue();
        this.posId = paymentRequest.getContractConfiguration().getProperty(P24Constants.POS_ID).getValue();
        this.key = paymentRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_KEY).getValue();
    }

    public String getMerchantId() {
        return merchantId;
    }

//    public void setMerchantId(String merchantId) {
//        this.merchantId = merchantId;
//    }

    public String getPosId() {
        return posId;
    }

//    public void setPosId(String posId) {
//        this.posId = posId;
//    }

    public String getKey() {
        return key;
    }

//    public void setKey(String key) {
//        this.key = key;
//    }

    public abstract Map<String, String> createBodyMap();

    public abstract String createSignature();
}
