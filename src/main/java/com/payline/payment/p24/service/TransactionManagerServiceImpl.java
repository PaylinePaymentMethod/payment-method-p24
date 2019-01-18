package com.payline.payment.p24.service;

import com.payline.pmapi.service.TransactionManagerService;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.p24.utils.P24Constants.ORDER_ID;
import static com.payline.payment.p24.utils.P24Constants.SESSION_ID;
import static com.payline.payment.p24.utils.P24Constants.SOAP_ORDER_ID;

public class TransactionManagerServiceImpl implements TransactionManagerService {
    private static final String DELIMS = ".";
    private static final String DELIMS_REGEX = "[.]";


    @Override
    public Map<String, String> readAdditionalData(String data, String version) {
        String[] tokens = data.split(DELIMS_REGEX);
        Map<String,String> dataMap = new HashMap();
        dataMap.put(SOAP_ORDER_ID, tokens[0]);
        dataMap.put(ORDER_ID, tokens[1]);
        dataMap.put(SESSION_ID, tokens[2]);
        return dataMap;
    }


    public static String encode(String orderId, String orderIdFull, String sessionId) {
        return orderId + DELIMS + orderIdFull + DELIMS + sessionId;
    }
}
