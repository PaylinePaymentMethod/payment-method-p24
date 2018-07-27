package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.BodyMapKeys;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

public class P24CheckConnectionRequest extends P24Request {


    public P24CheckConnectionRequest(String merchantId, String posId, String key) {
        super(merchantId, posId, key);
    }

    public P24CheckConnectionRequest(PaymentRequest paymentRequest) {
        super(paymentRequest);
    }

    @Override
    public Map<String, String> createBodyMap() {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(BodyMapKeys.MERCHAND_ID.getKey(), getMerchantId());
        bodyMap.put(BodyMapKeys.POS_ID.getKey(), getPosId());
        bodyMap.put(BodyMapKeys.SIGN.getKey(), createSignature());
        return bodyMap;
    }

    @Override
    public String createSignature() {
        return (new SecurityManager()).hash(getPosId(), getKey());
    }

}
