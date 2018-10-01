package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;

import java.util.HashMap;
import java.util.Map;

public class P24VerifyRequest extends P24Request {
    private String sessionId;
    private String amount;
    private String currency;
    private String orderId;
    private String signature;

    /**
     * @param redirectionPaymentRequest
     * @param orderId
     * @throws P24ValidationException
     */
    public P24VerifyRequest(RedirectionPaymentRequest redirectionPaymentRequest, String orderId)
            throws P24ValidationException {
        super(redirectionPaymentRequest);
        validate(redirectionPaymentRequest);
        this.sessionId = redirectionPaymentRequest.getTransactionId();
        this.amount = redirectionPaymentRequest.getAmount().getAmountInSmallestUnit().toString();
        this.currency = redirectionPaymentRequest.getAmount().getCurrency().getCurrencyCode();
        this.orderId = orderId;
        this.signature = createSignature();

    }

    public P24VerifyRequest(TransactionStatusRequest transactionStatusRequest, String orderId) throws P24ValidationException {
        super(transactionStatusRequest);
        validate(transactionStatusRequest);
        this.sessionId = transactionStatusRequest.getTransactionIdentifier();
        this.amount = transactionStatusRequest.getAmount().getAmountInSmallestUnit().toString();
        this.currency = transactionStatusRequest.getAmount().getCurrency().getCurrencyCode();
        this.orderId = orderId;
        this.signature = createSignature();
    }

    @Override
    public Map<String, String> createBodyMap() {
        Map<String, String> bodyMap = new HashMap<>();

        bodyMap.put(BodyMapKeys.MERCHAND_ID.getKey(), getMerchantId());
        bodyMap.put(BodyMapKeys.POS_ID.getKey(), getPosId());
        bodyMap.put(BodyMapKeys.SESSION_ID.getKey(), sessionId);
        bodyMap.put(BodyMapKeys.AMOUNT.getKey(), amount);
        bodyMap.put(BodyMapKeys.CURRECNCY.getKey(), currency);
        bodyMap.put(BodyMapKeys.ORDER_ID.getKey(), orderId);
        bodyMap.put(BodyMapKeys.SIGN.getKey(), signature);
        return bodyMap;
    }

    @Override
    public String createSignature() {
        return (new SecurityManager()).hash(sessionId, orderId, amount, currency, getKey());
    }

    private void validate(RedirectionPaymentRequest redirectionPaymentRequest) throws P24ValidationException {
        /*if (redirectionPaymentRequest.getOrder() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_ORDER);
        }
*/
        if (redirectionPaymentRequest.getAmount() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_AMOUNT);
        }
    }

    private void validate(TransactionStatusRequest transactionStatusRequest) throws P24ValidationException {
        if (transactionStatusRequest.getOrder() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_ORDER);
        }

        if (transactionStatusRequest.getAmount() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_AMOUNT);
        }
    }
}
