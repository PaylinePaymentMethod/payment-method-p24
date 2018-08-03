package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.apache.commons.lang3.StringUtils;
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
                        .withPaylineEnvironment(new PaylineEnvironment("", "", "", true))
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
    public void createSignature() {

        Mockito.when(securityManager.hash(Mockito.any())).thenReturn(hash);

        String signature = p24CheckConnectionRequest.createSignature();

        Assert.assertFalse(StringUtils.isEmpty(signature));
        Assert.assertEquals(hash, signature);

    }
}
