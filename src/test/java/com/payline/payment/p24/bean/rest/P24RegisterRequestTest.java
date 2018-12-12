package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.payment.p24.utils.LocalizationImpl;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.*;

import static com.payline.payment.p24.bean.TestUtils.CANCEL_URL;
import static com.payline.payment.p24.bean.TestUtils.SUCCESS_URL;

public class P24RegisterRequestTest {

    private static final Environment ENVIRONMENT =
            new Environment(null, SUCCESS_URL, CANCEL_URL, true);

    private static final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();

    private Amount amount = new Amount(BigInteger.TEN, Currency.getInstance("EUR"));

    private Browser browser = new Browser("", Locale.FRANCE);

    private Order order = Order.OrderBuilder.anOrder().withReference("REF").build();

    private String mail = "foo@bar.baz";
    private String phone = "0606060606";
    private Map<Buyer.AddressType, Buyer.Address> addresses = new HashMap<>();
    private Map<Buyer.PhoneNumberType, String> phoneNumbers = new HashMap<>();


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    LocalizationImpl localizationService;

    final String transactionID = "transactionID";
    final String softDescriptor = "softDescriptor";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void ConstructorInvocationWoOrderReference() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_ORDER);

        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .withOrder(Order.OrderBuilder.anOrder().withReference("").build())
                .build();

        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoBuyer() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_BUYER);
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoMail() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_BUYER);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail("")
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoAddresses() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_BUYER);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoCountry() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_BUYER);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoAmount() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_AMOUNT);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .withAmount(new Amount(null, Currency.getInstance("EUR")))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoAmountSmallUnit() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_AMOUNT);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(new Amount(null, Currency.getInstance("EUR")))
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoAmountCurrency() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_AMOUNT);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(new Amount(BigInteger.TEN, null))
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoEnvironment() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_ENVIRONNEMENT);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .withEnvironment(new Environment("", "", "", true))
                .build();
        new P24RegisterRequest(request);
    }

    @Test
    public void ConstructorInvocationWoRedirectionReturnURL() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_ENVIRONNEMENT);
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withCountry("country")
                .build();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, phone);
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, phone);
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(mail)
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .build();
        PaymentRequest request = PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(browser)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(new Environment(null, "", CANCEL_URL, true))
                .withOrder(order)
                .withTransactionId(transactionID)
                .withSoftDescriptor(softDescriptor)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
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

    @Test
    public void createBodyMap_lenght() throws Exception {
        P24RegisterRequest request = new P24RegisterRequest(createPaymentRequestMandatory());
        //create char array of specified length
        char[] charArray = new char[20];

        //fill all elements with the specified char
        Arrays.fill(charArray, 'A');

        /*
        Test 20
         */
        //create string from char array and returnreturn ;
        String test20 = new String(charArray);
        FieldUtils.writeField(request, "transferLabel", test20, true);
        Map<String, String> map = request.createBodyMap();

        Assert.assertNotNull(map);
        Assert.assertEquals(test20, map.get(BodyMapKeys.TRANSFER_LABEL.getKey()));

        /*
        Test 21
         */
        String test21 = test20 + "x";
        FieldUtils.writeField(request, "transferLabel", test21, true);
        map = request.createBodyMap();

        Assert.assertNotNull(map);
        Assert.assertEquals(test20, map.get(BodyMapKeys.TRANSFER_LABEL.getKey()));

        /*
        Test 19
         */
        String test19 = test20.substring(1);
        FieldUtils.writeField(request, "transferLabel", test19, true);
        map = request.createBodyMap();

        Assert.assertNotNull(map);
        Assert.assertEquals(test19, map.get(BodyMapKeys.TRANSFER_LABEL.getKey()));


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
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(ENVIRONMENT)
                .withOrder(order)
                .withTransactionId(transactionID)
                .withBuyer(buyer)
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .withBrowser(new Browser("", Locale.FRANCE))
                .withSoftDescriptor("")
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
                        .withEnvironment(ENVIRONMENT)
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE)
                        .build();
        return new P24CheckConnectionRequest(contractParametersCheckRequest);
    }

}
