package com.payline.payment.p24.bean;

import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.common.Buyer.Address;
import com.payline.pmapi.bean.common.Buyer.BuyerBuilder;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.math.BigInteger;
import java.util.*;

public class TestUtils {

    public static final String SUCCESS_URL = "https://succesurl.com/";
    public static final String CANCEL_URL = "http://localhost/cancelurl.com/";
    public static final String NOTIFICATION_URL = "http://google.com/";

    /**
     * Create a paymentRequest with default parameters.
     *
     * @return paymentRequest created
     */
    public static PaymentRequest createDefaultPaymentRequest() {
        final Amount amount = new Amount(BigInteger.TEN, Currency.getInstance("EUR"));
        final ContractConfiguration contractConfiguration = new ContractConfiguration("", new HashMap<String, ContractProperty>());
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_ID, new ContractProperty("merchantId"));
        contractConfiguration.getContractProperties().put(P24Constants.POS_ID, new ContractProperty("posId"));
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_KEY, new ContractProperty("merchantKey"));
        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = "transactionID";
        final Order order = Order.OrderBuilder.anOrder().withReference(transactionID).build();
        final String softDescriptor = "softDescriptor";

        final PaymentRequest paymentRequest = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .build();

        return paymentRequest;
    }

    public static PaymentRequest createCompletePaymentRequest() {

        final Amount amount = new Amount(BigInteger.TEN, Currency.getInstance("PLN"));
        final ContractConfiguration contractConfiguration = new ContractConfiguration("", new HashMap<String, ContractProperty>());
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_ID, new ContractProperty("merchantId"));
        contractConfiguration.getContractProperties().put(P24Constants.POS_ID, new ContractProperty("posId"));
        contractConfiguration.getContractProperties().put(P24Constants.MERCHANT_KEY, new ContractProperty("merchantKey"));
        contractConfiguration.getContractProperties().put(P24Constants.TIME_LIMIT, new ContractProperty("10"));
        contractConfiguration.getContractProperties().put(P24Constants.WAIT_FOR_RESULT, new ContractProperty("1"));
        contractConfiguration.getContractProperties().put(P24Constants.SHIPPING, new ContractProperty("0"));
        
        final PaylineEnvironment paylineEnvironment = new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = "transactionID" + Calendar.getInstance().getTimeInMillis();;
        final Order order = Order.OrderBuilder.anOrder().withReference(transactionID).build();
        final String softDescriptor = "softDescriptor";
        final Locale locale = new Locale("FR");

        Address address = Address.AddressBuilder.anAddress()
                .withStreet1("street1")
                .withCity("city")
                .withCountry("country")
                .withZipCode("zip")
                .build();

        Map<Buyer.AddressType, Address> addresses = new HashMap<>();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);

        Map<Buyer.PhoneNumberType, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, "0606060606");

        Buyer buyer = BuyerBuilder.aBuyer()
                .withEmail("foo@bar.baz")
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .withFullName(new Buyer.FullName("foo", "bar", Buyer.Civility.UNKNOWN))
                .build();

        final PaymentRequest paymentRequest = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .build();

        return paymentRequest;
    }

}
