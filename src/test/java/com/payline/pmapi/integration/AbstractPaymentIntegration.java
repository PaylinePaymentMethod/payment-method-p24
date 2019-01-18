//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.payline.pmapi.integration;

import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.Order.OrderBuilder;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.PaymentService;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.junit.jupiter.api.Assertions;

import java.math.BigInteger;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractPaymentIntegration {
    public static final String SUCCESS_URL = "https://succesurl.com/";
    public static final String CANCEL_URL = "http://localhost/cancelurl.com/";
    public static final String NOTIFICATION_URL = "http://google.com/";

    public AbstractPaymentIntegration() {
    }

    protected abstract Map<String, ContractProperty> generateParameterContract();

    protected abstract PaymentFormContext generatePaymentFormContext();

    protected abstract String payOnPartnerWebsite(String var1);

    protected abstract String cancelOnPartnerWebsite(String var1);

    public PaymentResponseSuccess fullRedirectionPayment(PaymentRequest paymentRequest, PaymentService paymentService, PaymentWithRedirectionService paymentWithRedirectionService) {
        PaymentResponse paymentResponseFromPaymentRequest = paymentService.paymentRequest(paymentRequest);
        this.checkPaymentResponseIsNotFailure(paymentResponseFromPaymentRequest);
        this.checkPaymentResponseIsRightClass("paymentRequest", paymentResponseFromPaymentRequest, PaymentResponseRedirect.class);
        PaymentResponseRedirect paymentResponseRedirect = (PaymentResponseRedirect) paymentResponseFromPaymentRequest;
        String partnerUrl = paymentResponseRedirect.getRedirectionRequest().getUrl().toString();
        String redirectionUrl = this.payOnPartnerWebsite(partnerUrl);
        Assertions.assertEquals("https://succesurl.com/", redirectionUrl);
        String partnerTransactionId = paymentResponseRedirect.getPartnerTransactionId();
        PaymentResponse paymentResponseFromFinalize = this.handlePartnerResponse(paymentWithRedirectionService, paymentRequest, paymentResponseRedirect);
        this.checkPaymentResponseIsNotFailure(paymentResponseFromFinalize);
        this.checkPaymentResponseIsRightClass("redirectionPaymentRequest", paymentResponseFromFinalize, PaymentResponseSuccess.class);
        PaymentResponseSuccess paymentResponseSuccess = (PaymentResponseSuccess) paymentResponseFromFinalize;
        Assertions.assertNotNull(paymentResponseSuccess.getTransactionDetails());
        Assertions.assertEquals(partnerTransactionId, paymentResponseSuccess.getPartnerTransactionId());
       return paymentResponseSuccess;
    }

    public void fullRedirectionPaymentKO(PaymentRequest paymentRequest, PaymentService paymentService, PaymentWithRedirectionService paymentWithRedirectionService) {
        PaymentResponse paymentResponseFromPaymentRequest = paymentService.paymentRequest(paymentRequest);
        this.checkPaymentResponseIsNotFailure(paymentResponseFromPaymentRequest);
        this.checkPaymentResponseIsRightClass("paymentRequest", paymentResponseFromPaymentRequest, PaymentResponseRedirect.class);
        PaymentResponseRedirect paymentResponseRedirectKO = (PaymentResponseRedirect) paymentResponseFromPaymentRequest;
        String partnerUrl = paymentResponseRedirectKO.getRedirectionRequest().getUrl().toString();
        String redirectionUrl = this.cancelOnPartnerWebsite(partnerUrl);
        if (!"http://localhost/cancelurl.com/".equals(redirectionUrl)) {
            PaymentResponse paymentResponse2 = this.handlePartnerResponse(paymentWithRedirectionService, paymentRequest, paymentResponseRedirectKO);
            Assertions.assertTrue(paymentResponse2 instanceof PaymentResponseFailure, "PaymentResponse should be a failure since the payment has been cancelled");
            Assertions.assertEquals(FailureCause.CANCEL, ((PaymentResponseFailure) paymentResponse2).getFailureCause(), "PaymentResponseFailure should be a CANCEL FailureCause");
        }

    }

    protected PaymentRequest createDefaultPaymentRequest() {
        Amount amount = new Amount(BigInteger.valueOf(1500L), Currency.getInstance("EUR"));
        ContractConfiguration contractConfiguration = new ContractConfiguration("", this.generateParameterContract());
        PaymentFormContext paymentFormContext = this.generatePaymentFormContext();
        Environment environment = new Environment("http://google.com/", "https://succesurl.com/", "http://localhost/cancelurl.com/", true);
        Order order = OrderBuilder.anOrder().withReference("transactionID").build();
        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaymentFormContext(paymentFormContext)
                .withEnvironment(environment)
                .withOrder(order)
                .withTransactionId("transactionID")
                .withSoftDescriptor("softDescriptor").build();
    }

    private PaymentResponse handlePartnerResponse(PaymentWithRedirectionService paymentWithRedirectionService, PaymentRequest paymentRequest, PaymentResponseRedirect paymentResponseRedirect) {
        ContractConfiguration contractConfiguration = new ContractConfiguration("", this.generateParameterContract());
        Environment environment = new Environment("http://google.com/", "https://succesurl.com/", "http://localhost/cancelurl.com/", true);
        RedirectionPaymentRequest redirectionPaymentRequest = (RedirectionPaymentRequest)
                RedirectionPaymentRequest.builder()
                        .withContractConfiguration(contractConfiguration)
                        .withPaymentFormContext(this.generatePaymentFormContext())
                        .withEnvironment(environment)
                        .withTransactionId(paymentRequest.getTransactionId())
                        .withRequestContext(paymentResponseRedirect.getRequestContext())
                        .withAmount(paymentRequest.getAmount())
                        .withOrder(paymentRequest.getOrder())
                        .withBuyer(paymentRequest.getBuyer())
                        .withBrowser(paymentRequest.getBrowser())
                        .withPartnerConfiguration(paymentRequest.getPartnerConfiguration())
                        .withLocale(Locale.FRANCE)
                        .build();
        return paymentWithRedirectionService.finalizeRedirectionPayment(redirectionPaymentRequest);
    }

    private void checkPaymentResponseIsNotFailure(PaymentResponse paymentResponse) {
        Assertions.assertFalse(paymentResponse instanceof PaymentResponseFailure, () -> {
            return "paymentRequest returned PaymentResponseFailure (Failure cause = " + ((PaymentResponseFailure) paymentResponse).getFailureCause() + ", errorCode = " + ((PaymentResponseFailure) paymentResponse).getErrorCode();
        });
    }

    private void checkPaymentResponseIsRightClass(String requestName, PaymentResponse paymentResponse, Class clazz) {
        Assertions.assertTrue(paymentResponse.getClass().isAssignableFrom(clazz), () -> {
            return requestName + " did not return a " + clazz.getSimpleName() + " (" + paymentResponse.toString() + ")";
        });
    }
}
