package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.rest.P24VerifyRequest;
import com.payline.payment.p24.bean.soap.P24TrnBySessionIdRequest;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.*;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.net.URISyntaxException;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {

    private P24HttpClient p24HttpClient;

    private RequestUtils requestUtils;


    private SoapHelper soapHelper;

    public PaymentWithRedirectionServiceImpl() {
        this.p24HttpClient = new P24HttpClient();
        this.requestUtils = new RequestUtils();
        this.soapHelper = new SoapHelper();
    }

    /**
     * Get the SOAP response message error code
     *
     * @param soapResponseMessage
     * @return SoapErrorCodeEnum : the error code
     */
    private SoapErrorCodeEnum getErrorCode(SOAPMessage soapResponseMessage) {

        SoapErrorCodeEnum errorCode;

        if (soapResponseMessage != null) {

            String toto = soapHelper.getErrorCodeFromSoapResponseMessage(soapResponseMessage);
            errorCode = SoapErrorCodeEnum.fromP24CodeValue(toto);

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

            String merchantId = requestUtils.getContractValue(redirectionPaymentRequest, P24Constants.MERCHANT_ID);
            String password = requestUtils.getContractValue(redirectionPaymentRequest, P24Constants.MERCHANT_MDP);
            String sessionId = redirectionPaymentRequest.getTransactionId();
            boolean isSandbox = requestUtils.isSandbox(redirectionPaymentRequest);

            // call /trnBySessionId
            P24TrnBySessionIdRequest sessionIdRequest = new P24TrnBySessionIdRequest().login(merchantId).pass(password).sessionId(sessionId);
            SOAPMessage soapResponseMessage = soapHelper.sendSoapMessage(sessionIdRequest.buildSoapMessage(isSandbox), P24Url.SOAP_ENDPOINT.getUrl(isSandbox));

            // parse the response
            if (SoapErrorCodeEnum.OK == getErrorCode(soapResponseMessage)) {

                // get needed info for REST request
                String orderId = soapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.ORDER_ID);
                String email = soapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.EMAIL);

                // call trnVerify
                P24VerifyRequest verifyRequest = new P24VerifyRequest(redirectionPaymentRequest, orderId);

                String host = P24Url.REST_HOST.getUrl(isSandbox);
                HttpResponse response = p24HttpClient.doPost(host, P24Path.VERIFY, verifyRequest.createBodyMap());

                // parse the response
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

                    if ("error=0".equalsIgnoreCase(responseMessage)) {
                        // SUCCESS!
                        return PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                                .withStatusCode("0")
                                .withTransactionIdentifier(sessionId)
                                .withTransactionDetails(Email.EmailBuilder.anEmail().withEmail(email).build())
                                .build();

                    } else {
                        // parse the response
                        return getPaymentResponseFailure(getVerifyError(responseMessage), FailureCause.INVALID_DATA);

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

        } catch (P24ValidationException | URISyntaxException e) {
            return getPaymentResponseFailure(e.getMessage(), FailureCause.INVALID_DATA);
        }
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        try {

            String merchantId = transactionStatusRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_ID).getValue();
            String password = transactionStatusRequest.getContractConfiguration().getProperty(P24Constants.MERCHANT_MDP).getValue();
            String sessionId = transactionStatusRequest.getOrder().getReference();

            boolean isSandbox = transactionStatusRequest.getPaylineEnvironment().isSandbox();

            // call /trnBySessionId
            P24TrnBySessionIdRequest sessionIdRequest = new P24TrnBySessionIdRequest().login(merchantId).pass(password).sessionId(sessionId);
            SOAPMessage soapResponseMessage = soapHelper.sendSoapMessage(sessionIdRequest.buildSoapMessage(isSandbox), P24Url.SOAP_ENDPOINT.getUrl(isSandbox));

            // parse the response
            if (SoapErrorCodeEnum.OK == getErrorCode(soapResponseMessage)) {

                // get needed info for REST request
                String orderId = soapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.ORDER_ID);
                String email = soapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.EMAIL);

                // call trnVerify
                P24VerifyRequest verifyRequest = new P24VerifyRequest(transactionStatusRequest, orderId);

                String host = P24Url.REST_HOST.getUrl(isSandbox);
                HttpResponse response = p24HttpClient.doPost(host, P24Path.VERIFY, verifyRequest.createBodyMap());

                // parse the response
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");

                    if ("error=0".equalsIgnoreCase(responseMessage)) {
                        // SUCCESS!
                        return PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                                .withStatusCode("0")
                                .withTransactionIdentifier(orderId)
                                .withTransactionDetails(Email.EmailBuilder.anEmail().withEmail(email).build())
                                .build();

                    } else {
                        // parse the response
                        return getPaymentResponseFailure(getVerifyError(responseMessage), FailureCause.INVALID_DATA);

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

        } catch (P24ValidationException | URISyntaxException e) {
            return getPaymentResponseFailure(e.getMessage(), FailureCause.INVALID_DATA);
        }
























    }

    private PaymentResponseFailure getPaymentResponseFailure(String errorCode, final FailureCause failureCause) {
        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode).build();
    }


    public String getVerifyError(String responseMessage) {
        try {
            return responseMessage.substring(6, 11);
        } catch (IndexOutOfBoundsException e) {
            return "unknown error";
        }

    }

}
