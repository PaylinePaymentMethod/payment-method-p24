package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.Map;

public abstract class P24Request implements Request {

    private final ContractConfiguration contractConfiguration;
    private final PaylineEnvironment paylineEnvironment;
    private String merchantId;
    private String posId;
    private String key;


    public P24Request(PaymentRequest paymentRequest) {
        // FIXME verifs
        this.contractConfiguration = paymentRequest.getContractConfiguration();
        this.paylineEnvironment = paymentRequest.getPaylineEnvironment();
        this.merchantId = paymentRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_ID).getValue();
        this.posId = paymentRequest.getContractConfiguration().getProperty(P24Constants.POS_ID).getValue();
        this.key = paymentRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_KEY).getValue();
    }

    public P24Request(ContractParametersCheckRequest contractParametersCheckRequest) {
        // FIXME verifs
        this.contractConfiguration = contractParametersCheckRequest.getContractConfiguration();
        this.paylineEnvironment = contractParametersCheckRequest.getPaylineEnvironment();

        // get all fields to check
        final Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
        this.merchantId = accountInfo.get(P24Constants.MERCHANT_ID);
        this.posId = accountInfo.get(P24Constants.POS_ID);
        if (posId == null || posId.length() == 0) {
            posId = this.getMerchantId();
        }
        this.key = accountInfo.get(P24Constants.MERCHANT_KEY);
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getPosId() {
        return posId;
    }

    public String getKey() {
        return key;
    }

    @Override
    public PaylineEnvironment getPaylineEnvironment() {
        return paylineEnvironment;
    }

    @Override
    public ContractConfiguration getContractConfiguration() {
        return contractConfiguration;
    }

    public abstract Map<String, String> createBodyMap();

    public abstract String createSignature();
}
