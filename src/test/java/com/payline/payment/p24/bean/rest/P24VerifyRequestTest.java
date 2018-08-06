package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

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
    public void ConstructorInvocationWithoutContract() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_CONTRACT);
        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder().build();
        new P24VerifyRequest(redirectionPaymentRequest, null);
    }

    @Test
    public void ConstructorInvocationWithoutOrder() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_ORDER);
        RedirectionPaymentRequest redirectionPaymentRequest =
                RedirectionPaymentRequest.builder()
                        .withContractConfiguration(TestUtils.createContractConfiguration())
                        .build();
        new P24VerifyRequest(redirectionPaymentRequest, null);
    }

    @Test
    public void ConstructorInvocationWithoutAmount() throws P24ValidationException {
        expectedEx.expect(P24ValidationException.class);
        expectedEx.expectMessage(P24ErrorMessages.MISSING_AMOUNT);
        RedirectionPaymentRequest redirectionPaymentRequest =
                RedirectionPaymentRequest.builder()
                        .withOrder(TestUtils.createOrder("zzz"))
                        .withContractConfiguration(TestUtils.createContractConfiguration())
                        .build();
        new P24VerifyRequest(redirectionPaymentRequest, null);
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
