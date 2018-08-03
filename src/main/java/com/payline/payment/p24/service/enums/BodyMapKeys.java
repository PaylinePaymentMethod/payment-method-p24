package com.payline.payment.p24.service.enums;

public enum BodyMapKeys {

    MERCHAND_ID("p24_merchant_id"),
    POS_ID("p24_pos_id"),
    SIGN("p24_sign"),
    SESSION_ID("p24_session_id"),
    ORDER_ID("p24_order_id"),
    AMOUNT("p24_amount"),
    CURRECNCY("p24_currency"),
    DESCRIPTION("p24_description"),
    EMAIL("p24_email"),
    COUNTRY("p24_country"),
    URL_RETURN("p24_url_return"),
    API_VERSION("p24_api_version"),
    CLIENT("p24_client"),
    ADDRESS("p24_address"),
    ZIP("p24_zip"),
    CITY("p24_city"),
    PHONE("p24_phone"),
    LANGUAGE("p24_language"),
    METHOD("p24_method"),
    WAIT_FOR_RESULT("P24_wait_for_result"),
    CHANNEL("p24_channel"),
    SHIPPING("p24_shipping"),
    TRANSFER_LABEL("p24_transfer_label"),
    ENCODING("p24_encoding"),
    URL_STATUS("p24_url_status"),
    TIME_LIMIT("p24_time_limit");

    private String keyLabel;

    BodyMapKeys(String label) {
        keyLabel = label;
    }

    public String getKey() {
        return keyLabel;
    }
}
