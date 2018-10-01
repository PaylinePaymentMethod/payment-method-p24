package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.RequestUtils;
import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;

import java.util.Map;

public abstract class P24Request implements Request {

    private final ContractConfiguration contractConfiguration;
    private final PaylineEnvironment paylineEnvironment;
    private String merchantId;
    private String posId;
    private String key;

    private RequestUtils requestUtils = new RequestUtils();


    public P24Request(PaymentRequest paymentRequest) throws P24ValidationException {
        this.contractConfiguration = paymentRequest.getContractConfiguration();
        this.paylineEnvironment = paymentRequest.getPaylineEnvironment();
        this.merchantId = requestUtils.getContractValue(paymentRequest, P24Constants.MERCHANT_ID);
        this.posId = requestUtils.getContractValue(paymentRequest, P24Constants.POS_ID);
        if (posId == null || posId.length() == 0) {
            posId = this.getMerchantId();
        }
        this.key = requestUtils.getContractValue(paymentRequest, P24Constants.MERCHANT_KEY);
    }

    public P24Request(ContractParametersCheckRequest contractParametersCheckRequest) {
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

    public P24Request(TransactionStatusRequest transactionStatusRequest) {
        this.contractConfiguration = transactionStatusRequest.getContractConfiguration();
        this.paylineEnvironment = transactionStatusRequest.getPaylineEnvironment();

        // get all fields to check
        final ContractConfiguration configuration = transactionStatusRequest.getContractConfiguration();

        this.merchantId = configuration.getProperty(P24Constants.MERCHANT_ID).getValue();
        this.posId = configuration.getProperty(P24Constants.POS_ID).getValue();
        if (posId == null || posId.length() == 0) {
            posId = this.getMerchantId();
        }
        this.key = configuration.getProperty(P24Constants.MERCHANT_KEY).getValue();
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

    protected RequestUtils getRequestUtils() {
        return requestUtils;
    }
}
