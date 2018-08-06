package com.payline.payment.p24.service;


import com.payline.payment.p24.bean.soap.P24TrnBySessionIdRequest;
import com.payline.payment.p24.bean.soap.P24TrnRefundRequest;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.*;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.RefundService;

import javax.xml.soap.SOAPMessage;

public class RefundServiceImpl implements RefundService {

    private RequestUtils requestUtils = new RequestUtils();

    private SoapHelper soapHelper;

    public RefundServiceImpl() {
        requestUtils = new RequestUtils();
        soapHelper = new SoapHelper();
    }


    /**
     * Get the SOAP response message error code
     *
     * @param soapResponseMessage
     * @return SoapErrorCodeEnum : the error code
     */
    private static SoapErrorCodeEnum getErrorCode(SOAPMessage soapResponseMessage) {

        SoapErrorCodeEnum errorCode;

        if (soapResponseMessage != null) {

            errorCode = SoapErrorCodeEnum.fromP24CodeValue(
                    SoapHelper.getErrorCodeFromSoapResponseMessage(soapResponseMessage));

            if (errorCode == null) {
                errorCode = SoapErrorCodeEnum.UNKNOWN_ERROR;
            }

        } else {
            errorCode = SoapErrorCodeEnum.UNKNOWN_ERROR;
        }

        return errorCode;

    }

    @Override
    public RefundResponse refundRequest(RefundRequest refundRequest) {
        String transactionId = refundRequest.getTransactionId();
        try {
            SOAPMessage soapResponseMessage = null;
            SoapErrorCodeEnum errorCode = null;
            boolean isSandbox = requestUtils.isSandbox(refundRequest);

            // get all needed infos
            String merchantId = requestUtils.getContractValue(refundRequest, P24Constants.MERCHANT_ID);
            String password = requestUtils.getContractValue(refundRequest, P24Constants.MERCHANT_MDP);
            String sessionId = refundRequest.getOrder().getReference();
            // FIXME NPA + transactionId ... number format exception
            int amount = refundRequest.getOrder().getAmount().getAmountInSmallestUnit().intValue();

            // Call P24.trnBySessionId and get the orderId from response
            P24TrnBySessionIdRequest trnBySessionIdRequest = new P24TrnBySessionIdRequest().login(merchantId).pass(password).sessionId(sessionId);
            soapResponseMessage = soapHelper.sendSoapMessage(
                    trnBySessionIdRequest.buildSoapMessage(isSandbox),
                    P24Url.SOAP_ENDPOINT.getUrl(isSandbox)
            );

            errorCode = getErrorCode(soapResponseMessage);

            // ... continue if last ws errorCode = 0
            if (SoapErrorCodeEnum.OK == errorCode) {

                String trnBySessionIdOrderIdValue = SoapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.ORDER_ID);

                // Call P24.trnRefund
                P24TrnRefundRequest p24TrnRefundRequest = new P24TrnRefundRequest()
                        .login(merchantId)
                        .pass(password)
                        .batch(Integer.valueOf(transactionId))
                        .orderId(Integer.valueOf(trnBySessionIdOrderIdValue))
                        .sessionId(sessionId)
                        .amount(amount);

                soapResponseMessage = soapHelper.sendSoapMessage(
                        p24TrnRefundRequest.buildSoapMessage(isSandbox),
                        P24Url.SOAP_ENDPOINT.getUrl(isSandbox)
                );

                errorCode = getErrorCode(soapResponseMessage);

                // ... continue if last ws errorCode = 0
                if (SoapErrorCodeEnum.OK == errorCode) {
                    return RefundResponseSuccess.RefundResponseSuccessBuilder.aRefundResponseSuccess()
                            .withStatusCode("0")
                            .withTransactionId(transactionId)
                            .build();

                } else {
                    return getRefundResponseFailure(errorCode.getP24ErrorCode(), FailureCause.INTERNAL_ERROR, transactionId);
                }

            } else {
                return getRefundResponseFailure(errorCode.getP24ErrorCode(), FailureCause.INVALID_DATA, transactionId);
            }
        } catch (P24ValidationException e) {
            return getRefundResponseFailure(null, FailureCause.INVALID_DATA, transactionId);
        }
    }

    @Override
    public boolean canMultiple() {
        return false;
    }

    @Override
    public boolean canPartial() {
        return false;
    }


    private RefundResponseFailure getRefundResponseFailure(String errorCode, final FailureCause failureCause, String transactionId) {
        return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                .withErrorCode(errorCode)
                .withFailureCause(failureCause)
                .withTransactionId(transactionId)
                .build();
    }

}
