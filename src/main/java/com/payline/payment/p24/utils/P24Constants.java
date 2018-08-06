package com.payline.payment.p24.utils;

import java.util.regex.Pattern;

public class P24Constants {

    private P24Constants() {
        //RAS.
    }

    //
    public static final String ENCODING = "UTF-8";

    public static final String SCHEME = "https";

    public static final Pattern REGEX_ERROR_MESSAGES = Pattern.compile("errorMessage=(.*)");
    public static final Pattern REGEX_TOKEN = Pattern.compile("token=(.+)");

    public static final String MERCHANT_ID = "merchantId";
    public static final String POS_ID = "posId";
    public static final String MERCHANT_KEY = "key";
    public static final String MERCHANT_MDP = "password";
    public static final String API_VERSION = "3.2";
    public static final String TIME_LIMIT = "timeLimit";
    public static final String SHIPPING = "shipping";
    public static final String WAIT_FOR_RESULT = "waitForResult";


    // SOAP constants
    public static final String TRN_REFUND = "TrnRefund";
    public static final String TRN_BY_SESSION_ID = "TrnBySessionId";
    public static final String TEST_ACCESS = "TestAccess";
    public static final String REFUND = "Refund";
    public static final String BATCH = "batch";
    public static final String LIST = "list";
    public static final String ORDER_ID = "orderIdFull";
    public static final String SOAP_ORDER_ID = "orderId";
    public static final String EMAIL = "clientEmail";
    public static final String AMOUNT = "amount";

    public static final String SOAP_ENC = "SOAP-ENC";
    public static final String SOAP_ENC_ARRAY_TYPE = "SOAP-ENC:arrayType";
    public static final String SOAP_ENCODING_URL = "http://schemas.xmlsoap.org/soap/encoding/";

    public static final String XSI = "xsi";
    public static final String XSI_TYPE = "xsi:type";
    public static final String XSI_URL = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String XSD = "xsd";
    public static final String XSD_STRING = "xsd:string";
    public static final String XSD_INT = "xsd:int";
    public static final String XSD_URL = "http://www.w3.org/2001/XMLSchema";

    public static final String SER = "ser";
    public static final String SER_ARRAY_OF_REFUND = "ser:ArrayOfRefund";
    public static final String SER_REFUND = "ser:Refund[]";

    public static final String SOAP_TAG_ERROR_CODE = "errorCode";
    public static final String SOAP_TAG_ERROR_MESSAGE = "errorMessage";

    public static final String NO_ERROR_CODE = "no code transmitted";

}
