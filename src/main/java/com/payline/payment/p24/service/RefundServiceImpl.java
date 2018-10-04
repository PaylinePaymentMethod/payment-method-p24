package com.payline.payment.p24.service;


import com.payline.payment.p24.bean.soap.P24TrnBySessionIdRequest;
import com.payline.payment.p24.bean.soap.P24TrnRefundRequest;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.*;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.RefundService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.soap.SOAPMessage;
import java.util.Calendar;

public class RefundServiceImpl implements RefundService {

    private static final Logger LOG = LogManager.getLogger(RefundServiceImpl.class);

    private RequestUtils requestUtils;

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
    private SoapErrorCodeEnum getErrorCode(SOAPMessage soapResponseMessage) {


        if (soapResponseMessage == null) {
            return SoapErrorCodeEnum.UNKNOWN_ERROR;
        }


        SoapErrorCodeEnum errorCode = SoapErrorCodeEnum.fromP24CodeValue(
                soapHelper.getErrorCodeFromSoapResponseMessage(soapResponseMessage));

        if (errorCode == null) {
            return SoapErrorCodeEnum.UNKNOWN_ERROR;
        }

        return errorCode;

    }

    @Override
    public RefundResponse refundRequest(RefundRequest refundRequest) {
        int batch = (int) Calendar.getInstance().getTimeInMillis();
        try {
            SOAPMessage soapResponseMessage = null;
            SoapErrorCodeEnum errorCode = null;
            boolean isSandbox = requestUtils.isSandbox(refundRequest);

            // get all needed infos
            String merchantId = requestUtils.getContractValue(refundRequest, P24Constants.MERCHANT_ID);
            String password = requestUtils.getContractValue(refundRequest, P24Constants.MERCHANT_MDP);

            validateRequest(refundRequest);
            String sessionId = refundRequest.getOrder().getReference();
            int amount = refundRequest.getOrder().getAmount().getAmountInSmallestUnit().intValue();

            // Call P24.trnBySessionId and get the orderId from response
            P24TrnBySessionIdRequest trnBySessionIdRequest =
                    new P24TrnBySessionIdRequest().login(merchantId).pass(password).sessionId(sessionId);
            soapResponseMessage = soapHelper.sendSoapMessage(
                    trnBySessionIdRequest.buildSoapMessage(isSandbox), P24Url.SOAP_ENDPOINT.getUrl(isSandbox));

            errorCode = getErrorCode(soapResponseMessage);

            // ... continue if last ws errorCode = 0
            if (SoapErrorCodeEnum.OK != errorCode) {
                return getRefundResponseFailure(errorCode.getP24ErrorCode(), FailureCause.INVALID_DATA, refundRequest.getTransactionId());
            }


            String trnBySessionIdOrderIdValue =
                    soapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.ORDER_ID);

            if (requestUtils.isNotNumeric(trnBySessionIdOrderIdValue)) {
                LOG.error("Invalid data : trnBySessionIdOrderIdValue is not numeric");
                return getRefundResponseFailure(null, FailureCause.INVALID_DATA, refundRequest.getTransactionId());
            }

            // Call P24.trnRefund
            P24TrnRefundRequest p24TrnRefundRequest = new P24TrnRefundRequest()
                    .login(merchantId)
                    .pass(password)
                    .batch(batch)
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
                        .withTransactionId(String.valueOf(batch))
                        .build();

            } else {
                return getRefundResponseFailure(errorCode.getP24ErrorCode(), FailureCause.INTERNAL_ERROR, refundRequest.getTransactionId());
            }

        } catch (P24ValidationException e) {
            return getRefundResponseFailure(null, FailureCause.INVALID_DATA, refundRequest.getTransactionId());
        }
    }

    @Override
    public boolean canMultiple() {
        return true;
    }

    @Override
    public boolean canPartial() {
        return true;
    }


    private RefundResponseFailure getRefundResponseFailure(String errorCode, final FailureCause failureCause, String transactionId) {
        return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                .withErrorCode(errorCode)
                .withFailureCause(failureCause)
                .withTransactionId(transactionId)
                .build();
    }

    private void validateRequest(RefundRequest refundRequest) throws P24ValidationException {
        Order order = refundRequest.getOrder();
        if (order == null) {
            String err = "Invalid data : refundRequest Order is mandatory";
            LOG.error(err);
            throw new P24ValidationException(err);

        }
        Amount amount = order.getAmount();
        if (amount == null || amount.getAmountInSmallestUnit() == null) {
            String err = "Invalid data : amount is mandatory";
            LOG.error(err);
            throw new P24ValidationException(err);

        }
    }
}