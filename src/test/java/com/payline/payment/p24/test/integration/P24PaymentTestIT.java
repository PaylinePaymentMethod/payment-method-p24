package com.payline.payment.p24.test.integration;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.bean.rest.P24CheckConnectionRequest;
import com.payline.payment.p24.bean.soap.P24CheckAccessRequest;
import com.payline.payment.p24.service.ConfigurationServiceImpl;
import com.payline.payment.p24.service.PaymentServiceImpl;
import com.payline.payment.p24.service.PaymentWithRedirectionServiceImpl;
import com.payline.payment.p24.service.RefundServiceImpl;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.PaymentResponseRedirect;
import com.payline.pmapi.bean.payment.response.PaymentResponseSuccess;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.PaymentService;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import com.payline.pmapi.service.RefundService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class P24PaymentTestIT extends AbstractPaymentIntegration {

    private static final Logger logger = LogManager.getLogger("AbstractPaymentTest");

    private final PaylineEnvironment paylineEnvironment =
            new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);

    private PaymentService paymentService = new PaymentServiceImpl();

    private ConfigurationServiceImpl configurationServiceImpl = new ConfigurationServiceImpl();

    private PaymentWithRedirectionServiceImpl paymentWithRedirectionService = new PaymentWithRedirectionServiceImpl();

    private RefundService refundService = new RefundServiceImpl();

    private static final String SUCCESS_URL = "http://www.citronrose.com/Payment_Return.aspx";

    private String merchantId = "65840";
    private String posId = "65840";
    private String key = "0f67a7fec13ff180";
    private String password = "76feca7a92aee7d069e32a66b7e8cef4";
    private Order order;
    private Order refundOrder;
    private Amount amount;
    private String transactionID;

    public P24PaymentTestIT() throws GeneralSecurityException {
    }

    @Test
    public void fullPaymentTest() {

        final ContractConfiguration contractConfiguration = new ContractConfiguration("", generateParameterContract());
        Map<String, String> errors = new HashMap<>();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, merchantId);
        bodyMap.put(P24Constants.POS_ID, posId);
        bodyMap.put(P24Constants.MERCHANT_KEY, key);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withPaylineEnvironment(paylineEnvironment)
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE).build();
        P24CheckConnectionRequest p24CheckConnectionRequest = new P24CheckConnectionRequest(contractParametersCheckRequest);
        logger.info(" P24CheckConnectionRequest : {}", p24CheckConnectionRequest);

        // check HTTP connection
        logger.info(" check Http Connection ");
        configurationServiceImpl.checkHttpConnection(true, p24CheckConnectionRequest, errors, Locale.FRENCH);
        Assert.assertTrue(errors.isEmpty());

        // check SOAP connection
        logger.info(" check SOAP Connection ");
        P24CheckAccessRequest p24TestAccessRequest = new P24CheckAccessRequest().login(merchantId).pass(password);
        configurationServiceImpl.checkSoapConnection(p24TestAccessRequest, true, errors, Locale.FRENCH);
        Assert.assertTrue(errors.isEmpty());

        this.fullRedirectionPayment(this.createDefaultPaymentRequest(), paymentService, paymentWithRedirectionService);

        // Refund
    }

    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        final Map<String, ContractProperty> propertyMap = new HashMap<>();
        propertyMap.put(P24Constants.MERCHANT_ID, new ContractProperty(merchantId));
        propertyMap.put(P24Constants.MERCHANT_MDP, new ContractProperty(password));
        propertyMap.put(P24Constants.POS_ID, new ContractProperty(posId));
        propertyMap.put(P24Constants.MERCHANT_KEY, new ContractProperty(key));
        propertyMap.put(P24Constants.TIME_LIMIT, new ContractProperty("15"));
        propertyMap.put(P24Constants.WAIT_FOR_RESULT, new ContractProperty("1"));
        propertyMap.put(P24Constants.SHIPPING, new ContractProperty("0"));
        return propertyMap;
    }

    @Override
    protected Map<String, Serializable> generatePaymentFormData() {
        return null;
    }

    /**
     * The GoogleDriver has to be in path in order to pass this test.
     *
     * @param partnerUrl
     * @return
     */
    @Override
    protected String payOnPartnerWebsite(final String partnerUrl) {
        // Start browser
        WebDriver driver = new ChromeDriver();
        try {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            // Go to partner's website
            driver.get(partnerUrl);

            // Select Orange bank
            driver.findElement(By.xpath("//span[contains(@class, 'bank-logo-146')]")).click();

            // login
            driver.findElement(By.xpath(".//form//button")).click();

            // Pay
            driver.findElement(By.xpath("//button[contains(@class, 'btn-success')]")).click();

            // Wait for redirection to success or cancel url
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.or(ExpectedConditions.urlToBe(SUCCESS_URL), ExpectedConditions.urlToBe(CANCEL_URL)));
            String currentUrl = driver.getCurrentUrl();

            return currentUrl;
        } finally {

            // Stop browser
            driver.quit();

        }
    }

    @Override
    protected String cancelOnPartnerWebsite(String partnerUrl) {
        return null;
    }

    @Override
    public PaymentRequest createDefaultPaymentRequest() {

        final ContractConfiguration contractConfiguration = new ContractConfiguration("", generateParameterContract());
        amount = TestUtils.createAmount("PLN");
        String numaberId = ("" + Calendar.getInstance().getTimeInMillis());
        transactionID = numaberId.substring(numaberId.length() - 7, numaberId.length() - 1);
        order = Order.OrderBuilder.anOrder().withReference(transactionID).build();
        final String softDescriptor = "softDescriptor";
        final Buyer buyer = TestUtils.createDefaultBuyer();

        final PaymentRequest paymentRequest = PaymentRequest.builder()
                .withAmount(amount)
                .withBuyer(buyer)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withLocale(new Locale("FR"))
                .build();

        return paymentRequest;

    }

    @Override
    public void fullRedirectionPayment(PaymentRequest paymentRequest, PaymentService paymentService, PaymentWithRedirectionService paymentWithRedirectionService) {
        // Step 1 : Partner’s url generation
        logger.info(" trnRegister : PaymentService#paymentRequest ");
        PaymentResponse paymentResponse1 = paymentService.paymentRequest(paymentRequest);
        super.checkPaymentResponseIsNotFailure(paymentResponse1);
        super.checkPaymentResponseIsRightClass("paymentRequest", paymentResponse1, PaymentResponseRedirect.class);

        logger.info(" PaymentService#paymentRequest ");

        // Step 2 : Display of partner’s web page
        String partnerUrl = ((PaymentResponseRedirect) paymentResponse1).getRedirectionRequest().getUrl().toString();
        String redirectionUrl = this.payOnPartnerWebsite(partnerUrl);
        Assertions.assertEquals(SUCCESS_URL, redirectionUrl);

        // Step 3 : End of payment
//        String transactionId = ((PaymentResponseRedirect) paymentResponse1).getTransactionIdentifier();
        PaymentResponse paymentResponse2 = this.handlePartnerResponse(paymentWithRedirectionService, transactionID);
        super.checkPaymentResponseIsNotFailure(paymentResponse2);
        super.checkPaymentResponseIsRightClass("redirectionPaymentRequest", paymentResponse2, PaymentResponseSuccess.class);

        // Validate final response
        PaymentResponseSuccess paymentResponseSuccess = (PaymentResponseSuccess) paymentResponse2;
        Assertions.assertNotNull(paymentResponseSuccess.getTransactionDetails());
        Assertions.assertEquals("0", paymentResponseSuccess.getStatusCode());
        final String p24transactionId = paymentResponseSuccess.getTransactionIdentifier();
        Assertions.assertNotNull(p24transactionId);


        final ContractConfiguration contractConfiguration = new ContractConfiguration("", generateParameterContract());
        final Buyer buyer = TestUtils.createDefaultBuyer();

        refundOrder = Order.OrderBuilder.anOrder().withReference(transactionID).withAmount(amount).build();
        RefundRequest refundRequest = RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withTransactionId(transactionID)
                .withPartnerTransactionId(transactionID)
                .withPaylineEnvironment(paylineEnvironment)
                .withContractConfiguration(contractConfiguration)
                .withPartnerConfiguration(new PartnerConfiguration(null, null))
                .withBuyer(buyer)
                .withOrder(refundOrder)
                .build();

        // Step 4 : refund
        RefundResponse refundResponse = refundService.refundRequest(refundRequest);
        Assertions.assertEquals(RefundResponseSuccess.class, refundResponse.getClass());

    }

    @Override
    public PaymentResponse handlePartnerResponse(PaymentWithRedirectionService paymentWithRedirectionService, String transactionId) {

        final ContractConfiguration contractConfiguration = new ContractConfiguration("", generateParameterContract());
        final RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withRedirectionContext(transactionID)
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withAmount(amount)
                .build();

        return paymentWithRedirectionService.finalizeRedirectionPayment(redirectionPaymentRequest);
    }

}


