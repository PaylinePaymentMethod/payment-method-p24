package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.LocalizationImpl;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static com.payline.payment.p24.bean.TestUtils.CANCEL_URL;
import static com.payline.payment.p24.bean.TestUtils.SUCCESS_URL;

public class P24RegisterRequestTest {

    private static final PaylineEnvironment paylineEnvironment =
            new PaylineEnvironment(null, SUCCESS_URL, CANCEL_URL, true);

    private static final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();

    @Mock
    LocalizationImpl localizationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test(expected = P24ValidationException.class)
    public void ConstructorInvocationWithoutBuyer() throws P24ValidationException {
        PaymentRequest request = TestUtils.createDefaultPaymentRequest();
        new P24RegisterRequest(request);
    }

    @Test(expected = P24ValidationException.class)
    public void ConstructorInvocationWithoutAddresses() throws P24ValidationException {
        PaymentRequest request = createPaymentRequestWithoutAddressRequest();
        new P24RegisterRequest(request);
    }

    @Test(expected = P24ValidationException.class)
    public void ConstructorInvocationWithWrongCurrency() throws P24ValidationException {
        PaymentRequest request = createInvalidCurrencyPaymentRequest();
        new P24RegisterRequest(request);
    }

    @Test
    public void createBodyMap() throws P24ValidationException {
        P24RegisterRequest request = new P24RegisterRequest(createPaymentRequestMandatory());
        Map<String, String> map = request.createBodyMap();

        System.out.println(
                Arrays.toString(map.entrySet().toArray())

        );
        Assert.assertNotNull(map);
    }


    private static PaymentRequest createInvalidCurrencyPaymentRequest() {

        final Amount amount = TestUtils.createAmount("JPY");

        final String transactionID = "transactionID" + Calendar.getInstance().getTimeInMillis();

        final Order order = TestUtils.createOrder(transactionID);
        final String softDescriptor = "softDescriptor";
        final Locale locale = new Locale("FR");

        Buyer buyer = TestUtils.createDefaultBuyer();

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", locale))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .build();
    }

    private static PaymentRequest createPaymentRequestWithoutAddressRequest() {
        final Amount amount = TestUtils.createAmount("EUR");

        final String transactionID = "transactionID" + Calendar.getInstance().getTimeInMillis();

        final Order order = TestUtils.createOrder(transactionID);
        final String softDescriptor = "softDescriptor";
        final Locale locale = new Locale("FR");

        Buyer buyer = TestUtils.createBuyer(TestUtils.createDefaultPhoneNumbers(), TestUtils.createAddresses(null), TestUtils.createFullName());

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", locale))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .build();
    }

    private static PaymentRequest createPaymentRequestMandatory() {
        final Amount amount = TestUtils.createAmount("EUR");


        final String transactionID = "transactionID" + Calendar.getInstance().getTimeInMillis();
        final Order order = TestUtils.createOrder(transactionID);

        final Buyer.Address address = TestUtils.createAddress(null, null, null);
        Map<Buyer.AddressType, Buyer.Address> addresses = TestUtils.createAddresses(address);

        final Buyer buyer = TestUtils.createBuyer(null, addresses, null);

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(null)
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(null)
                .withTransactionId(transactionID)
                .withBuyer(buyer)
                .build();
    }


    @Test
    public void isNotNumeric() {
        Mockito.when(localizationService.getSafeLocalizedString(Mockito.anyString(), Mockito.any())).thenReturn("erreur");


        P24CheckConnectionRequest p24CheckConnectionRequest = getP24CheckConnectionRequest(null);
        Map<String, String> errors = p24CheckConnectionRequest.validateRequest(localizationService, Locale.FRANCE);
        Assert.assertEquals(1, errors.size());

        p24CheckConnectionRequest = getP24CheckConnectionRequest(null);
        errors = p24CheckConnectionRequest.validateRequest(localizationService, Locale.FRANCE);
        Assert.assertEquals(1, errors.size());

        p24CheckConnectionRequest = getP24CheckConnectionRequest("foo");
        errors = p24CheckConnectionRequest.validateRequest(localizationService, Locale.FRANCE);
        Assert.assertEquals(1, errors.size());

        p24CheckConnectionRequest = getP24CheckConnectionRequest("1");
        errors = p24CheckConnectionRequest.validateRequest(localizationService, Locale.FRANCE);
        Assert.assertEquals(0, errors.size());

    }

    private P24CheckConnectionRequest getP24CheckConnectionRequest(String toCheck) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, toCheck);
        bodyMap.put(P24Constants.POS_ID, "5");
        bodyMap.put(P24Constants.MERCHANT_KEY, P24Constants.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withPaylineEnvironment(paylineEnvironment)
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE)
                        .build();
        return new P24CheckConnectionRequest(contractParametersCheckRequest);
    }

}
