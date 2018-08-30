package com.payline.payment.p24.test.integration;

import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.PaymentService;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.junit.jupiter.api.Assertions;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

/**
 * Integration test to verify that a full payment works.
 */
public abstract class AbstractPaymentIntegration {

    public static final String SUCCESS_URL = "https://succesurl.com/";
    public static final String CANCEL_URL = "http://localhost/cancelurl.com/";
    public static final String NOTIFICATION_URL = "http://google.com/";

    /**
     * Generate map with all contract parameters. Will be used to connect to the partner's API.
     * <p>
     * Must be implemented by all payment method.
     *
     * @return contract parameters
     */
    protected abstract Map<String, ContractProperty> generateParameterContract();

    protected abstract Map<String, Serializable> generatePaymentFormData();

    /**
     * Do the payment on partner's website.
     * Use the url in parameter to go the partner's website where the payment will be initialized.
     * <p>
     * Must be implemented by all payment method.
     *
     * @param partnerUrl
     * @return redirection url returned by the partner
     */
    protected abstract String payOnPartnerWebsite(String partnerUrl);

    protected abstract String cancelOnPartnerWebsite(String partnerUrl);

    /**
     * Allows to test a full payment (redirection) :
     * - ask the partner for the redirection url
     * - do the payment on the partner website
     * - finalize payment on payline
     * <p>
     * Must be call by payment method test.
     *
     * @param paymentRequest
     */
    public void fullRedirectionPayment(final PaymentRequest paymentRequest, final PaymentService paymentService, final PaymentWithRedirectionService paymentWithRedirectionService) {
        // Step 1 : Partner’s url generation
        final PaymentResponse paymentResponse1 = paymentService.paymentRequest(paymentRequest);
        checkPaymentResponseIsNotFailure(paymentResponse1);
        checkPaymentResponseIsRightClass("paymentRequest", paymentResponse1, PaymentResponseRedirect.class);

        // Step 2 : Display of partner’s web page
        final String partnerUrl = ((PaymentResponseRedirect) paymentResponse1).getRedirectionRequest().getUrl().toString();
        final String redirectionUrl = this.payOnPartnerWebsite(partnerUrl);
        Assertions.assertEquals(SUCCESS_URL, redirectionUrl);

        // Step 3 : End of payment
        final String transactionId = ((PaymentResponseRedirect) paymentResponse1).getTransactionIdentifier();
        final PaymentResponse paymentResponse2 = handlePartnerResponse(paymentWithRedirectionService, transactionId);
        checkPaymentResponseIsNotFailure(paymentResponse2);
        checkPaymentResponseIsRightClass("redirectionPaymentRequest", paymentResponse2, PaymentResponseSuccess.class);

        // Validate final response
        final PaymentResponseSuccess paymentResponseSuccess = (PaymentResponseSuccess) paymentResponse2;
        Assertions.assertNotNull(paymentResponseSuccess.getTransactionDetails());
        Assertions.assertEquals(transactionId, paymentResponseSuccess.getTransactionIdentifier());
    }

    /**
     * Allows to test that the cancellation on the partner website ends with a PaymentResponseFailure (CANCEL)
     * - ask the partner for the redirection url
     * - cancel the payment on the partner website
     *
     * @param paymentRequest
     * @param paymentService
     * @param paymentWithRedirectionService
     */
    public void fullRedirectionPaymentKO(final PaymentRequest paymentRequest, final PaymentService paymentService, final PaymentWithRedirectionService paymentWithRedirectionService) {
        // Step 1 : Partner’s url generation
        final PaymentResponse paymentResponse1 = paymentService.paymentRequest(paymentRequest);
        checkPaymentResponseIsNotFailure(paymentResponse1);
        checkPaymentResponseIsRightClass("paymentRequest", paymentResponse1, PaymentResponseRedirect.class);

        // Step 2 : Display of partner’s web page
        final String partnerUrl = ((PaymentResponseRedirect) paymentResponse1).getRedirectionRequest().getUrl().toString();
        final String redirectionUrl = this.cancelOnPartnerWebsite(partnerUrl);

        // 2 possibilities : partner redirect on cancel url or partner redirect on success url with error code
        if (!CANCEL_URL.equals(redirectionUrl)) {
            // Step 3 : End of payment
            final String transactionId = ((PaymentResponseRedirect) paymentResponse1).getTransactionIdentifier();
            final PaymentResponse paymentResponse2 = handlePartnerResponse(paymentWithRedirectionService, transactionId);
            Assertions.assertTrue(paymentResponse2 instanceof PaymentResponseFailure, "PaymentResponse should be a failure since the payment has been cancelled");
            Assertions.assertEquals(FailureCause.CANCEL, ((PaymentResponseFailure) paymentResponse2).getFailureCause(), "PaymentResponseFailure should be a CANCEL FailureCause");
        }
    }

    /**
     * Create a paymentRequest with default parameters.
     *
     * @return paymentRequest created
     */
    public PaymentRequest createDefaultPaymentRequest() {
        final Amount amount = new Amount(BigInteger.valueOf(1500), Currency.getInstance("EUR"));
        final ContractConfiguration contractConfiguration = new ContractConfiguration("", this.generateParameterContract());
        final Map<String, Serializable> paymentFormData = this.generatePaymentFormData();
        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = "transactionID";
        final Order order = Order.OrderBuilder.anOrder().withReference(transactionID).build();
        final String softDescriptor = "softDescriptor";

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaymentFormData(paymentFormData)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .build();
    }

    /**
     * Finalize payment on payline.
     * Retrieve transaction details.
     *
     * @param transactionId
     * @return paymentResponse with transaction details or PaymentResponseFailure if an error occurred
     */
    public PaymentResponse handlePartnerResponse(final PaymentWithRedirectionService paymentWithRedirectionService, final String transactionId) {
        final ContractConfiguration contractConfiguration = new ContractConfiguration("", this.generateParameterContract());
        final RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withRedirectionContext(transactionId)
                .withContractConfiguration(contractConfiguration)
                .withPaymentFormData(this.generatePaymentFormData())
                .build();

        return paymentWithRedirectionService.finalizeRedirectionPayment(redirectionPaymentRequest);
    }

    /**
     * Check that paymentResponse is not a Failure. If it is the test will fail and the error details outputted.
     *
     * @param paymentResponse : paymentResponse to check
     */
    public void checkPaymentResponseIsNotFailure(final PaymentResponse paymentResponse) {
        Assertions.assertFalse(paymentResponse instanceof PaymentResponseFailure, () -> "paymentRequest returned PaymentResponseFailure (Failure cause = "
                + ((PaymentResponseFailure) paymentResponse).getFailureCause() + ", errorCode = " + ((PaymentResponseFailure) paymentResponse).getErrorCode());
    }

    /**
     * Check that paymentResponse is an instance of the class in parameter. If not the test will fail.
     *
     * @param requestName     : name of the request which provides the paymentResponse
     * @param paymentResponse : paymentResponse to check
     * @param clazz           : class required
     */
    public void checkPaymentResponseIsRightClass(final String requestName, final PaymentResponse paymentResponse, final Class clazz) {
        Assertions.assertTrue(paymentResponse.getClass().isAssignableFrom(clazz), () -> requestName + " did not return a " + clazz.getSimpleName() + " (" + paymentResponse.toString() + ")");
    }
}
