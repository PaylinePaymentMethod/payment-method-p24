package com.payline.payment.p24;

import com.payline.payment.p24.bean.rest.P24VerifyRequest;
import com.payline.payment.p24.bean.soap.P24TrnBySessionIdRequest;
import com.payline.payment.p24.utils.*;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.PaymentResponseSuccess;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.soap.SOAPMessage;
import java.io.IOException;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {


    private static final Logger LOG = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);

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
                    SoapHelper.getErrorCodeFromSoapResponseMessage(soapResponseMessage)
            );

            if (errorCode == null) {
                errorCode = SoapErrorCodeEnum.UNKNOWN_ERROR;
            }

        } else {
            errorCode = SoapErrorCodeEnum.UNKNOWN_ERROR;
        }

        return errorCode;

    }

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        try {
            // get needed infos for SOAP request
            String merchantId = redirectionPaymentRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_ID).getValue();
            String password = redirectionPaymentRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_PASSWORD).getValue();
            String sessionId = redirectionPaymentRequest.getOrder().getReference();

            // call /trnBySessionId
            P24TrnBySessionIdRequest sessionIdRequest = new P24TrnBySessionIdRequest().login(merchantId).pass(password).sessionId(sessionId);
            SOAPMessage soapResponseMessage = SoapHelper.sendSoapMessage(sessionIdRequest.buildSoapMessage(), P24Constants.URL_ENDPOINT);

            // parse the response
            if (SoapErrorCodeEnum.OK.equals(getErrorCode(soapResponseMessage))) {


                // get needed info for REST request
                String orderId = SoapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.ORDER_ID);
                String email = SoapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.EMAIL);

                // call trnVerify
                P24VerifyRequest verifyRequest = new P24VerifyRequest(redirectionPaymentRequest, orderId);
                Response response = HttpClient.doPost(P24Path.VERIFY.toString(), verifyRequest.createBodyMap());

                // parse the response
                if (response.code() == 200) {
                    String responseMessage = response.body().string();

                    if ("error=0".equalsIgnoreCase(responseMessage)) {
                        // SUCCESS!
                        return PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                                .withStatusCode("0")
                                .withTransactionIdentifier(orderId)
                                .withTransactionDetails(Email.EmailBuilder.anEmail().withEmail(email).build())
                                .build();

                    } else {
                        // parse the response
                        String errorCode = getVerifyError(responseMessage);
                        return getPaymentResponseFailure(errorCode, FailureCause.INVALID_DATA);

                    }
                } else {
                    return getPaymentResponseFailure("invalid request", FailureCause.COMMUNICATION_ERROR);
                }

            } else {
                // get the SOAP error and return it
                return getPaymentResponseFailure("invalid soap data", FailureCause.INVALID_DATA);
            }

        } catch (IOException e) {
            return getPaymentResponseFailure(e.getMessage(), FailureCause.INTERNAL_ERROR);
        }
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        return getPaymentResponseFailure("timeout", FailureCause.SESSION_EXPIRED);
    }

    private PaymentResponseFailure getPaymentResponseFailure(String errorCode, final FailureCause failureCause) {
        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode).build();
    }


    private String getVerifyError(String responseMessage) {
        String[] s = responseMessage.split("\n");
        LOG.debug("Message : {0} \n 1: {1}; 2: {2}; 3: {3}; 4: {3}", responseMessage, s[0], s[1], s[2], s[3]);

        return s[2];
    }

}
