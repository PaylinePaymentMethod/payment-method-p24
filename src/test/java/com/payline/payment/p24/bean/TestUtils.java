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

import java.math.BigInteger;
import java.util.*;

public class TestUtils {

    public static final String SUCCESS_URL = "https://succesurl.com/";
    public static final String CANCEL_URL = "http://localhost/cancelurl.com/";
    public static final String NOTIFICATION_URL = "http://google.com/";
    public static final String MERCHANT_ID = "merchantId";
    public static final String POS_ID = "posId";
    public static final String MERCHANT_KEY = "merchantKey";

    /**
     * Create a paymentRequest with default parameters.
     *
     * @return paymentRequest created
     */
    public static PaymentRequest createDefaultPaymentRequest() {
        final Amount amount = createAmount("EUR");
        final ContractConfiguration contractConfiguration = createContractConfiguration();
        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = "transactionID";
        final Order order = createOrder(transactionID);
        final String softDescriptor = "softDescriptor";

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .build();
    }

    public static PaymentRequest createCompletePaymentRequest() {
        return createCompletePaymentBuilder().build();

    }

    public static RefundRequest createRefundRequest(String transactionId) {
        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
//        final String transactionID = createTransactionId();
        final Amount amount = createAmount("EUR");
        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withOrder(createOrder(transactionId, amount))
                .withBuyer(createDefaultBuyer())
                .withContractConfiguration(createContractConfiguration())
                .withPaylineEnvironment(paylineEnvironment)
                .withTransactionId(transactionId)
                .withPartnerTransactionId("toto")
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
    }

    public static RedirectionPaymentRequest createRedirectionPaymentRequest() {
        RedirectionPaymentRequest request = RedirectionPaymentRequest.builder().build();


        return request;
    }

    public static PaymentRequest.Builder createCompletePaymentBuilder() {
        final Amount amount = createAmount("PLN");
        final ContractConfiguration contractConfiguration = createContractConfiguration();

        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = createTransactionId();
        final Order order = createOrder(transactionID);
        final String softDescriptor = "softDescriptor";
        final Locale locale = new Locale("FR");

        Buyer buyer = createDefaultBuyer();

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer);
    }


    public static String createTransactionId() {
        return "transactionID" + Calendar.getInstance().getTimeInMillis();
    }

    public static Map<Buyer.AddressType, Buyer.Address> createAddresses(Buyer.Address address) {
        Map<Buyer.AddressType, Buyer.Address> addresses = new HashMap<>();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);

        return addresses;
    }

    public static Map<Buyer.AddressType, Buyer.Address> createDefaultAddresses() {
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

    public static ContractConfiguration createContractConfiguration() {
        final ContractConfiguration contractConfiguration = new ContractConfiguration("", new HashMap<>());
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_ID, new ContractProperty(MERCHANT_ID));
        contractConfiguration.getContractProperties().put(P24Constants.POS_ID, new ContractProperty(POS_ID));
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_KEY, new ContractProperty(MERCHANT_KEY));
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_MDP, new ContractProperty("merchantPassword"));

        return contractConfiguration;
    }

    public static Buyer.Address createAddress(String street, String city, String zip) {
        return Buyer.Address.AddressBuilder.anAddress()
                .withStreet1(street)
                .withCity(city)
                .withZipCode(zip)
                .withCountry("country")
                .build();
    }

    public static Buyer.Address createDefaultAddress() {
        return createAddress("a street", "a city", "a zip");
    }

    public static Buyer createBuyer(Map<Buyer.PhoneNumberType, String> phoneNumbers, Map<Buyer.AddressType, Buyer.Address> addresses, Buyer.FullName fullName) {
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
