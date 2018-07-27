package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.BodyMapKeys;
import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class P24CheckConnectionRequestTest {

    private String merchantId = "merchantId";
    private String posId = "posId";
    private String key = "key";
    private String hash = "7d0b7db75b8210fcbb1a6694cf1492be";

    @Mock
    private SecurityManager securityManager;

    @InjectMocks
    private P24CheckConnectionRequest p24CheckConnectionRequest = new P24CheckConnectionRequest(merchantId, posId, key);


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Assert.assertNotNull(p24CheckConnectionRequest);
    }

    @Test(expected = NullPointerException.class)
    public void badConstructorInvocation() {
        new P24CheckConnectionRequest(null);
    }

    @Test
    public void goodConstructorInvocation() {

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
