package com.payline.payment.p24;


import com.payline.payment.p24.bean.soap.P24TrnBySessionIdRequest;
import com.payline.payment.p24.bean.soap.P24TrnRefundRequest;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SoapErrorCodeEnum;
import com.payline.payment.p24.utils.SoapHelper;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.RefundService;

import javax.xml.soap.SOAPMessage;

public class RefundServiceImpl implements RefundService {
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
        SOAPMessage soapResponseMessage = null;
        SoapErrorCodeEnum errorCode = null;

        // get all needed infos
        String merchantId = refundRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_ID).getValue();
        String password = refundRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_PASSWORD).getValue();
        String sessionId = refundRequest.getOrder().getReference();
        String transactionId = refundRequest.getTransactionId();
        int amount = refundRequest.getOrder().getAmount().getAmountInSmallestUnit().intValue();

        // Call P24.trnBySessionId and get the orderId from response
        P24TrnBySessionIdRequest trnBySessionIdRequest = new P24TrnBySessionIdRequest().login(merchantId).pass(password).sessionId(sessionId);
        soapResponseMessage = SoapHelper.sendSoapMessage(
                trnBySessionIdRequest.buildSoapMessage(),
                P24Constants.URL_ENDPOINT
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

            soapResponseMessage = SoapHelper.sendSoapMessage(
                    p24TrnRefundRequest.buildSoapMessage(),
                    P24Constants.URL_ENDPOINT
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
