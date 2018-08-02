package com.payline.payment.p24.test.integration;

import com.payline.payment.p24.RefundServiceImpl;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.RequestUtils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.service.RefundService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigInteger;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

class RefundServiceImplTest {
    private String merchantId = "65840";
    private String posId = "65840";
    private String key = "0f67a7fec13ff180";
    private String password = "76feca7a92aee7d069e32a66b7e8cef4";

    @InjectMocks
    private RefundService service;

    @Mock
    private RequestUtils requestUtils;

    @Test
    void refundRequest() {
        service = new RefundServiceImpl(requestUtils);
        RefundRequest request = createRefundRequest();
        service.refundRequest(request);
    }

    @Test
    void canMultiple() {
        Assert.assertFalse(service.canMultiple());
    }

    @Test
    void canPartial() {
        Assert.assertFalse(service.canPartial());
    }

    RefundRequest createRefundRequest() {
        String transactionId = "1";

        final Amount amount = new Amount(BigInteger.TEN, Currency.getInstance("EUR"));
        final Order order = Order.OrderBuilder.anOrder().withReference(transactionId).withAmount(amount).build();
        final ContractConfiguration contractConfiguration = new ContractConfiguration("", this.generateParameterContract());
        PaylineEnvironment environment = new PaylineEnvironment("notificationURL", "redirectionURL", "redirectionCancelURL", true);
        PartnerConfiguration partnerConfiguration = new PartnerConfiguration(new HashMap<>());

        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withOrder(order)
                .withBuyer(createDefaultBuyer())
                .withContractConfiguration(contractConfiguration)
                .withPaylineEnvironment(environment)
                .withTransactionId(transactionId)
                .withPartnerTransactionId("1")
                .withPartnerConfiguration(partnerConfiguration)
                .build();
    }


    private Buyer createDefaultBuyer() {

        Map<Buyer.PhoneNumberType, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, "0606060606");

        Map<Buyer.AddressType, Buyer.Address> addresses = new HashMap<>();
        Buyer.Address address = createDefaultAddress();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);

        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail("foo@bar.baz")
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .withFullName(new Buyer.FullName("foo", "bar", Buyer.Civility.UNKNOWN))
                .build();
        return buyer;
    }

    private Buyer.Address createDefaultAddress() {

        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withStreet1("street1")
                .withCity("city")
                .withCountry("country")
                .withZipCode("zip")
                .build();
        return address;
    }

    protected Map<String, ContractProperty> generateParameterContract() {
        final Map<String, ContractProperty> propertyMap = new HashMap<>();
        propertyMap.put(P24Constants.MERCHANT_ID, new ContractProperty(merchantId));
        propertyMap.put(P24Constants.MERCHANT_PASSWORD, new ContractProperty(password));
        propertyMap.put(P24Constants.POS_ID, new ContractProperty(posId));
        propertyMap.put(P24Constants.MERCHANT_KEY, new ContractProperty(key));
        propertyMap.put(P24Constants.TIME_LIMIT, new ContractProperty("15"));
        propertyMap.put(P24Constants.WAIT_FOR_RESULT, new ContractProperty("1"));
        propertyMap.put(P24Constants.SHIPPING, new ContractProperty("0"));
        return propertyMap;
    }
}