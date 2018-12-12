package com.payline.payment.p24.test.integration;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.bean.rest.P24CheckConnectionRequest;
import com.payline.payment.p24.bean.soap.P24CheckAccessRequest;
import com.payline.payment.p24.service.ConfigurationServiceImpl;
import com.payline.payment.p24.service.PaymentServiceImpl;
import com.payline.payment.p24.service.PaymentWithRedirectionServiceImpl;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.PaymentFormContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.integration.AbstractPaymentIntegration;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class P24PaymentTestIT extends AbstractPaymentIntegration {

    private static final Logger logger = LogManager.getLogger("AbstractPaymentTest");

    private final Environment environment =
            new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);

    private PaymentService paymentService = new PaymentServiceImpl();
    private ConfigurationServiceImpl configurationServiceImpl = new ConfigurationServiceImpl();
    private PaymentWithRedirectionServiceImpl paymentWithRedirectionService = new PaymentWithRedirectionServiceImpl();

    @Test
    public void fullPaymentTest() {
        Map<String, String> errors = new HashMap<>();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, TestUtils.MERCHANT_ID);
        bodyMap.put(P24Constants.POS_ID, TestUtils.POS_ID);
        bodyMap.put(P24Constants.MERCHANT_KEY, TestUtils.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(TestUtils.createContractConfiguration())
                        .withEnvironment(environment)
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
        P24CheckAccessRequest p24TestAccessRequest = new P24CheckAccessRequest().login(TestUtils.MERCHANT_ID).pass(TestUtils.MERCHANT_PASSWORD);
        configurationServiceImpl.checkSoapConnection(p24TestAccessRequest, true, errors, Locale.FRENCH);
        Assert.assertTrue(errors.isEmpty());

        this.fullRedirectionPayment(this.createDefaultPaymentRequest(), paymentService, paymentWithRedirectionService);

        // Refund
    }

    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        return TestUtils.generateParameterContract();
    }

    @Override
    protected PaymentFormContext generatePaymentFormContext() {
        return null;
    }

    /**
     * The GoogleDriver has to be in path in order to pass this test.
     *
     * @param partnerUrl the partner url
     * @return current url
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
            return driver.getCurrentUrl();

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
        return TestUtils.createCompletePaymentRequest();
    }


}


