package com.payline.payment.p24.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.payline.payment.p24.utils.P24Constants.ORDER_ID;
import static com.payline.payment.p24.utils.P24Constants.SESSION_ID;
import static com.payline.payment.p24.utils.P24Constants.SOAP_ORDER_ID;

public class TransactionManagerServiceImplTest {
    private static final String orderId = "111";
    private static final String orderIdFull = "111111";
    private static final String sessionId = "222";
    private static final String expected = "111[.]111111[.]222";
    private static final String base = "111.111111.222";

    private TransactionManagerServiceImpl service = new TransactionManagerServiceImpl();

    @Test
    public void encode(){
        String encoded = TransactionManagerServiceImpl.encode(orderId, orderIdFull, sessionId);
        Assert.assertEquals(expected, encoded);
    }


    @Test
    public void readAdditionalData(){
        Map<String, String> map = service.readAdditionalData(base, "dummyString");
        Assert.assertEquals(3, map.size());
        Assert.assertTrue(map.containsKey(SOAP_ORDER_ID));
        Assert.assertTrue(map.containsKey(ORDER_ID));
        Assert.assertTrue(map.containsKey(SESSION_ID));

        Assert.assertEquals(orderId, map.get(SOAP_ORDER_ID));
        Assert.assertEquals(orderIdFull, map.get(ORDER_ID));
        Assert.assertEquals(sessionId, map.get(SESSION_ID));
    }
}
