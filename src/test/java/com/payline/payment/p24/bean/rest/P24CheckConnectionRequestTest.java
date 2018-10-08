package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.payment.p24.utils.LocalizationService;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class P24CheckConnectionRequestTest {

    private String merchantId = "merchantId";
    private String posId = "posId";
    private String key = "key";
    private String hash = "7d0b7db75b8210fcbb1a6694cf1492be";

    @Mock
    private SecurityManager securityManager;

    @Mock
    LocalizationService localizationService;

    @InjectMocks
    private P24CheckConnectionRequest p24CheckConnectionRequest;


    @Before
    public void setUp() {
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, merchantId);
        bodyMap.put(P24Constants.POS_ID, posId);
        bodyMap.put(P24Constants.MERCHANT_KEY, P24Constants.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withEnvironment(new Environment("", "", "", true))
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE).build();
        p24CheckConnectionRequest = new P24CheckConnectionRequest(contractParametersCheckRequest);
        MockitoAnnotations.initMocks(this);

        Assert.assertNotNull(p24CheckConnectionRequest);
    }

    @Test(expected = NullPointerException.class)
    public void badPaymentRequestConstructorInvocation() throws P24ValidationException {
        new P24CheckConnectionRequest((PaymentRequest) null);
    }

    @Test(expected = NullPointerException.class)
    public void badContractParametersCheckRequestConstructorInvocation() {
        new P24CheckConnectionRequest((ContractParametersCheckRequest) null);
    }


    @Test
    public void goodConstructorInvocation() throws P24ValidationException {

        PaymentRequest paymentRequest = TestUtils.createDefaultPaymentRequest();

        P24CheckConnectionRequest p24CheckConnectionRequest = new P24CheckConnectionRequest(paymentRequest);
        Assert.assertNotNull(p24CheckConnectionRequest);
    }

    @Test
    public void createBodyMap() {

        Mockito.when(securityManager.hash(Mockito.any())).thenReturn(hash);

        Map<String, String> bodyMap = p24CheckConnectionRequest.createBodyMap();

        Assert.assertNotNull(p24CheckConnectionRequest);
        Assert.assertEquals(3, bodyMap.size());
        Assert.assertTrue(bodyMap.containsKey(BodyMapKeys.MERCHAND_ID.getKey()));
        Assert.assertTrue(bodyMap.containsKey(BodyMapKeys.POS_ID.getKey()));
        Assert.assertTrue(bodyMap.containsKey(BodyMapKeys.SIGN.getKey()));
        Assert.assertEquals(merchantId, bodyMap.get(BodyMapKeys.MERCHAND_ID.getKey()));
        Assert.assertEquals(posId, bodyMap.get(BodyMapKeys.POS_ID.getKey()));
        Assert.assertEquals(hash, bodyMap.get(BodyMapKeys.SIGN.getKey()));

    }

    @Test
    public void validateRequest_badMerchantId() {
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, merchantId);
        bodyMap.put(P24Constants.POS_ID, "12");
        bodyMap.put(P24Constants.MERCHANT_KEY, P24Constants.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withEnvironment(new Environment("", "", "", true))
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE).build();
        P24CheckConnectionRequest p24Rq = new P24CheckConnectionRequest(contractParametersCheckRequest);

        Mockito.when(localizationService.getSafeLocalizedString(
                Mockito.eq("contract.merchantId.wrong"), Mockito.eq(Locale.FRANCE))).thenReturn("ko");

        Map<String, String> errors =
                p24Rq.validateRequest(localizationService, Locale.FRANCE);


        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.containsKey(P24Constants.MERCHANT_ID));

    }

    @Test
    public void validateRequest_badPosId() {
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, "12");
        bodyMap.put(P24Constants.POS_ID, posId);
        bodyMap.put(P24Constants.MERCHANT_KEY, P24Constants.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withEnvironment(new Environment("", "", "", true))
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE).build();
        P24CheckConnectionRequest p24Rq = new P24CheckConnectionRequest(contractParametersCheckRequest);

        Mockito.when(localizationService.getSafeLocalizedString(
                Mockito.eq("contract.posId.wrong"), Mockito.eq(Locale.FRANCE))).thenReturn("ko");

        Map<String, String> errors =
                p24Rq.validateRequest(localizationService, Locale.FRANCE);


        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.containsKey(P24Constants.POS_ID));

    }

    @Test
    public void isNotNumeric_emptyString() {
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, "");
        bodyMap.put(P24Constants.POS_ID, "12");
        bodyMap.put(P24Constants.MERCHANT_KEY, P24Constants.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withEnvironment(new Environment("", "", "", true))
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE).build();
        P24CheckConnectionRequest p24Rq = new P24CheckConnectionRequest(contractParametersCheckRequest);

        Mockito.when(localizationService.getSafeLocalizedString(
                Mockito.eq("contract.posId.wrong"), Mockito.eq(Locale.FRANCE))).thenReturn("ko");

        Map<String, String> errors =
                p24Rq.validateRequest(localizationService, Locale.FRANCE);


        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.containsKey(P24Constants.MERCHANT_ID));

    }

    @Test
    public void isNotNumeric_nullString() {
        final ContractConfiguration contractConfiguration = TestUtils.createContractConfiguration();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(P24Constants.MERCHANT_ID, null);
        bodyMap.put(P24Constants.POS_ID, "12");
        bodyMap.put(P24Constants.MERCHANT_KEY, P24Constants.MERCHANT_KEY);
        ContractParametersCheckRequest contractParametersCheckRequest =
                ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                        .withContractConfiguration(contractConfiguration)
                        .withEnvironment(new Environment("", "", "", true))
                        .withAccountInfo(bodyMap)
                        .withLocale(Locale.FRANCE).build();
        P24CheckConnectionRequest p24Rq = new P24CheckConnectionRequest(contractParametersCheckRequest);

        Mockito.when(localizationService.getSafeLocalizedString(
                Mockito.eq("contract.posId.wrong"), Mockito.eq(Locale.FRANCE))).thenReturn("ko");

        Map<String, String> errors =
                p24Rq.validateRequest(localizationService, Locale.FRANCE);


        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.containsKey(P24Constants.MERCHANT_ID));

    }


}
