package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.P24InvalidRequestException;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.p24.bean.TestUtils.*;

public class P24RegisterRequestTest {

    @Test(expected = P24InvalidRequestException.class)
    public void ConstructorInvocationWithoutBuyer() throws P24InvalidRequestException {
        PaymentRequest request = TestUtils.createDefaultPaymentRequest();
        new P24RegisterRequest(request);
    }

    @Test(expected = P24InvalidRequestException.class)
    public void ConstructorInvocationWithoutAddresses() throws P24InvalidRequestException {
        PaymentRequest request = createPaymentRequestWithoutAddressRequest();
        new P24RegisterRequest(request);
    }

    @Test(expected = P24InvalidRequestException.class)
    public void ConstructorInvocationWithWrongCurrency() throws P24InvalidRequestException {
        PaymentRequest request = createInvalidCurrencyPaymentRequest();
        new P24RegisterRequest(request);
    }

    @Test
    public void createBodyMap() throws P24InvalidRequestException {
        P24RegisterRequest request = new P24RegisterRequest(createPaymentRequestMandatory());
        Map<String, String> map = request.createBodyMap();

        System.out.println(
                Arrays.toString(map.entrySet().toArray())

        );
        Assert.assertNotNull(map);
    }

    @Test
    public void isGoodCurrencyCode() {
        Assert.assertFalse(P24RegisterRequest.isGoodCurrencyCode(null));
        Assert.assertTrue(P24RegisterRequest.isGoodCurrencyCode("EUR"));
        Assert.assertFalse(P24RegisterRequest.isGoodCurrencyCode("JPY"));
    }


    private static PaymentRequest createInvalidCurrencyPaymentRequest() {

        final Amount amount = TestUtils.createAmount("JPY");
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();

        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
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
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();

        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
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
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();

        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(null, SUCCESS_URL, CANCEL_URL, true);

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

}
