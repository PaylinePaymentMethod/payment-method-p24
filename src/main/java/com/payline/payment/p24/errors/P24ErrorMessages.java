package com.payline.payment.p24.errors;

public class P24ErrorMessages {

    private P24ErrorMessages() {
        //RAS.
    }


    public static final String WRONG_MERCHANT_ID = "contract.merchantId.wrong";
    public static final String WRONG_KEY = "contract.key.wrong";
    public static final String WRONG_POS_ID = "contract.posId.wrong";
    public static final String WRONG_PASS = "contract.password.wrong";
    public static final String WRONG_SEVER_RESPONSE = "contract.error.wrongServerResponse";
    public static final String NETWORK_ERROR = "contract.error.networkError";
    public static final String TECHNICAL_ERROR = "contract.technicalError";
    public static final String MISSING_PARAMETER = "p24.missing.parameter";
    public static final String MISSING_ORDER = "p24.missing.order";
    public static final String MISSING_AMOUNT = "p24.missing.amount";
    public static final String MISSING_CONTRACT = "p24.missing.contract.configuration";

}
