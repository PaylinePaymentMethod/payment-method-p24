package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class P24VerifyRequestTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Amount amount;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        amount = TestUtils.createAmount("EUR");
    }

    @Test
    public void ConstructorInvocationSuccess() throws P24ValidationException {
        RedirectionPaymentRequest redirectionPaymentRequest = getRedirectionPaymentRequest();
        P24VerifyRequest test = new P24VerifyRequest(redirectionPaymentRequest, null);
        Assert.assertNotNull(test);
        Assert.assertEquals(TestUtils.MERCHANT_ID, test.getMerchantId());
        Assert.assertEquals(TestUtils.MERCHANT_KEY, test.getKey());
        Assert.assertEquals(TestUtils.POS_ID, test.getPosId());
    }

    private RedirectionPaymentRequest getRedirectionPaymentRequest() {
        return RedirectionPaymentRequest.builder()
                .withOrder(TestUtils.createOrder("zzz"))
                .withAmount(amount)
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(),new HashMap<>()))
                .withBrowser(new Browser("", Locale.FRANCE))
                .withEnvironment(new Environment("", "", "", true))
                .withTransactionId("")
                .withSoftDescriptor("")
                .build();
    }


    @Test
    public void createBodyMap() throws P24ValidationException {
        RedirectionPaymentRequest redirectionPaymentRequest = getRedirectionPaymentRequest();
        P24VerifyRequest test = new P24VerifyRequest(redirectionPaymentRequest, "dd");

        Map<String, String> bodyMap = test.createBodyMap();
        Assert.assertNotNull(test);
        Assert.assertEquals(bodyMap.get(BodyMapKeys.MERCHAND_ID.getKey()), test.getMerchantId());
        Assert.assertEquals(bodyMap.get(BodyMapKeys.POS_ID.getKey()), test.getPosId());

        Assert.assertEquals(bodyMap.get(BodyMapKeys.AMOUNT.getKey()), String.valueOf(amount.getAmountInSmallestUnit()));
        Assert.assertEquals(bodyMap.get(BodyMapKeys.CURRECNCY.getKey()), amount.getCurrency().getCurrencyCode());
        Assert.assertEquals("dd", bodyMap.get(BodyMapKeys.ORDER_ID.getKey()));

        Assert.assertNotNull(bodyMap.get(BodyMapKeys.SIGN.getKey()));
    }


}
