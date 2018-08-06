package com.payline.payment.p24.utils;


import com.payline.pmapi.bean.common.FailureCause;

/**
 * Created by Thales on 19/07/2018.
 * <p>
 * This enum is used to map the error code from P24 string value to PayLine FailureCause enum value
 */
public enum SoapErrorCodeEnum {

    OK("0", null),

    ACCESS_DENIED("1", FailureCause.REFUSED),
    TRANSACTION_NOT_FOUND("10", FailureCause.INVALID_DATA),
    EMPTY_TRANSACTION_LIST("11", FailureCause.INVALID_DATA),
    REPEATED_BATCH_NUMBER("100", FailureCause.INVALID_DATA),
    EMPTY_REFUND_LIST("101", FailureCause.INVALID_DATA),
    INCORRECT_BATCH_ID("102", FailureCause.INVALID_DATA),
    EMPTY_LIST_1("120", FailureCause.INVALID_DATA),
    ERRORS_IN_REFUND_LIST("199", FailureCause.REFUSED),
    EMPTY_LIST_2("200", FailureCause.INVALID_DATA),
    INCORRECT_AMOUNT("300", FailureCause.INVALID_DATA),
    INCORRECT_EMAIL_ADDRESS("301", FailureCause.INVALID_DATA),
    CLIENT_REJECTED("302", FailureCause.REFUSED),
    TRANSACTION_VERIFICATION_ERROR("701", FailureCause.REFUSED),
    UNKNOWN_ERROR("10000", FailureCause.COMMUNICATION_ERROR);

    /**
     * P24 code
     */
    private String p24ErrorCode;

    /**
     * PayLine code
     */
    private FailureCause paylineFailureCause;

    SoapErrorCodeEnum(String p24ErrorCode,
                      FailureCause paylineFailureCause) {

        this.p24ErrorCode = p24ErrorCode;
        this.paylineFailureCause = paylineFailureCause;

    }

    public String getP24ErrorCode() {
        return p24ErrorCode;
    }

    public FailureCause getPaylineFailureCause() {
        return paylineFailureCause;
    }

    public static SoapErrorCodeEnum fromP24CodeValue(String text) {

        for (SoapErrorCodeEnum soapError : SoapErrorCodeEnum.values()) {
            if (soapError.getP24ErrorCode().equals(text)) {
                return soapError;
            }
        }

        return null;
    }

}