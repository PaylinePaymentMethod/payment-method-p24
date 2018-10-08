package com.payline.payment.p24.bean;

import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.common.Buyer.Address;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.*;

public class TestUtils {

    public static final String SUCCESS_URL = "https://succesurl.com/";
    public static final String CANCEL_URL = "http://localhost/cancelurl.com/";
    public static final String NOTIFICATION_URL = "http://google.com/";
    public static final String MERCHANT_ID = "65840";
    public static final String POS_ID = "65840";
    public static final String MERCHANT_KEY = "0f67a7fec13ff180";
    public static final String MERCHANT_PASSWORD = "76feca7a92aee7d069e32a66b7e8cef4";

    /**
     * Create a paymentRequest with default parameters.
     *
     * @return paymentRequest created
     */
    public static PaymentRequest createDefaultPaymentRequest() {
        final Amount amount = createAmount("EUR");
        final ContractConfiguration contractConfiguration = createContractConfiguration();
        final Environment environment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = "transactionID";
        final Order order = createOrder(transactionID);
        final String softDescriptor = "softDescriptor";

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(environment)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(),new HashMap<>()))
                .withEnvironment(new Environment("", "", "", true))
                .build();
    }

    public static PaymentRequest createCompletePaymentRequest() {
        return createCompletePaymentBuilder().build();

    }

    public static RefundRequest createRefundRequest(String transactionId) {
        final Environment environment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
//        final String transactionID = createRandom();
        final Amount amount = createAmount("EUR");
        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withOrder(createOrder(transactionId, amount))
                .withBuyer(createDefaultBuyer())
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(environment)
                .withTransactionId("10")
                .withPartnerTransactionId("toto")
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(),new HashMap<>()))
                .build();
    }

    public static RedirectionPaymentRequest createRedirectionPaymentRequest() {
        RedirectionPaymentRequest request = RedirectionPaymentRequest.builder().build();


        return request;
    }

    public static HttpResponse createResponse(int code, String body) {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, code, null), null);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(body.getBytes()));
        response.setEntity(entity);

        return response;
    }

    public static HttpResponse createResponseOK() {
        return createResponse(200, "error=0");
    }

    public static HttpResponse createResponseKO() {
        return createResponse(200, "error=err00&errorMessage=Błąd wywołania (1)");
    }


    public static PaymentRequest.Builder createCompletePaymentBuilder() {
        final Amount amount = createAmount("PLN");
        final ContractConfiguration contractConfiguration = createContractConfiguration();
        final Environment environment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final Order order = createOrder(createRandom());
        final String softDescriptor = "softDescriptor";
        final Locale locale = new Locale("FR");
        final Buyer buyer = createDefaultBuyer();

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(environment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(createRandom())
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(),new HashMap<>()))
                .withBrowser(new Browser("", Locale.FRANCE));
    }


    public static String createRandom() {
        String numaberId = ("" + Calendar.getInstance().getTimeInMillis());
        return numaberId.substring(numaberId.length() - 7, numaberId.length() - 1);
    }

    public static Map<Buyer.AddressType, Address> createAddresses(Address address) {
        Map<Buyer.AddressType, Address> addresses = new HashMap<>();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);

        return addresses;
    }

    public static Map<Buyer.AddressType, Address> createDefaultAddresses() {
        Address address = createDefaultAddress();
        return createAddresses(address);
    }

    public static Amount createAmount(String currency) {
        return new Amount(BigInteger.TEN, Currency.getInstance(currency));
    }

    public static Order createOrder(String transactionID) {
        return Order.OrderBuilder.anOrder().withReference(transactionID).build();
    }

    public static Order createOrder(String transactionID, Amount amount) {
        return Order.OrderBuilder.anOrder().withReference(transactionID).withAmount(amount).build();
    }

    public static Buyer.FullName createFullName() {
        return new Buyer.FullName("foo", "bar", Buyer.Civility.UNKNOWN);
    }

    public static Map<Buyer.PhoneNumberType, String> createDefaultPhoneNumbers() {
        Map<Buyer.PhoneNumberType, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, "0606060606");

        return phoneNumbers;
    }

    public static Map<String, ContractProperty> generateParameterContract() {
        final Map<String, ContractProperty> propertyMap = new HashMap<>();
        propertyMap.put(P24Constants.MERCHANT_ID, new ContractProperty(MERCHANT_ID));
        propertyMap.put(P24Constants.MERCHANT_MDP, new ContractProperty(MERCHANT_PASSWORD));
        propertyMap.put(P24Constants.POS_ID, new ContractProperty(POS_ID));
        propertyMap.put(P24Constants.MERCHANT_KEY, new ContractProperty(MERCHANT_KEY));
        propertyMap.put(P24Constants.TIME_LIMIT, new ContractProperty("15"));
        propertyMap.put(P24Constants.WAIT_FOR_RESULT, new ContractProperty("1"));
        propertyMap.put(P24Constants.SHIPPING, new ContractProperty("0"));
        return propertyMap;
    }

    public static ContractConfiguration createContractConfiguration() {
        return new ContractConfiguration("", generateParameterContract());
    }

    public static Address createAddress(String street, String city, String zip) {
        return Address.AddressBuilder.anAddress()
                .withStreet1(street)
                .withCity(city)
                .withZipCode(zip)
                .withCountry("country")
                .build();
    }

    public static Address createDefaultAddress() {
        return createAddress("a street", "a city", "a zip");
    }

    public static Buyer createBuyer(Map<Buyer.PhoneNumberType, String> phoneNumbers, Map<Buyer.AddressType, Address> addresses, Buyer.FullName fullName) {
        return Buyer.BuyerBuilder.aBuyer()
                .withEmail("foo@bar.baz")
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .withFullName(fullName)
                .build();
    }

    public static Buyer createDefaultBuyer() {
        return createBuyer(createDefaultPhoneNumbers(), createDefaultAddresses(), createFullName());
    }

}
