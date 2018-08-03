package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.payment.p24.service.enums.ChannelKeys;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

public class P24RegisterRequest extends P24Request {

    // mandatory fields
    private String sessionId;
    private String amount;
    private String currency;
    private String description;
    private String email;
    private String country;
    private String urlReturn;
    private String signature;
    private String apiVersion = P24Constants.API_VERSION;

    // non mandatory fields
    private String client;
    private String address;
    private String zip;
    private String city;
    private String phone;
    private String language;
    private String urlStatus;
    private String timeLimit = "5";
    private String waitForResult = "1"; // 1 for true
    private String channel;
    private String shipping;
    private String transferLabel;
    private String encoding = P24Constants.ENCODING;


    public P24RegisterRequest(PaymentRequest paymentRequest) throws P24ValidationException {
        super(paymentRequest);

        Buyer buyer = paymentRequest.getBuyer();
        if (buyer == null) {
            throw new P24ValidationException("buyer is mandatory but not provided");
        }
        // FIXME GET OR GETFORTYPE
        if (buyer.getAddresses() == null || buyer.getAddresses().get(Buyer.AddressType.BILLING) == null) {
            throw new P24ValidationException("buyer address is mandatory but not provided");
        }
        Buyer.Address buyerAddress = buyer.getAddresses().get(Buyer.AddressType.BILLING);

        // mandatory fields
        this.sessionId = paymentRequest.getOrder().getReference();
        this.amount = paymentRequest.getAmount().getAmountInSmallestUnit().toString();

        if (!isGoodCurrencyCode(paymentRequest.getAmount().getCurrency().getCurrencyCode())) {
            // FIXME : ask CAA
            throw new P24ValidationException("bad currency code (PLN, EUR, GBP, CZK");
        }
        this.currency = paymentRequest.getAmount().getCurrency().getCurrencyCode();

        this.description = this.sessionId;
        this.email = buyer.getEmail();

        if (buyerAddress.getCountry() == null) {
            throw new P24ValidationException("country is mandatory but not provided");
        }
        this.country = buyer.getAddressForType(Buyer.AddressType.BILLING).getCountry();

        if (paymentRequest.getPaylineEnvironment().getRedirectionReturnURL() == null) {
            throw new P24ValidationException("redirectionURL is mandatory but not provided");
        }
        this.urlReturn = paymentRequest.getPaylineEnvironment().getRedirectionReturnURL();
        this.signature = createSignature();


        // non mandatory fields
        if (buyer.getFullName() != null) {
            this.client = buyer.getFullName().toString();
        }
        this.address = buyerAddress.getStreet1();
        this.zip = buyerAddress.getZipCode();
        this.city = buyerAddress.getCity();

        if (buyer.getPhoneNumbers() != null) {
            this.phone = buyer.getPhoneNumberForType(Buyer.PhoneNumberType.BILLING);
        }

        if (paymentRequest.getLocale() != null) {
            this.language = paymentRequest.getLocale().getLanguage();
        }

        this.urlStatus = paymentRequest.getPaylineEnvironment().getNotificationURL();
//        this.shipping = "0";    // FIXME attendre l'évolution de l'APM API => this.shipping = paymentRequest.getOrder().getDeliveryCharge().getAmountInSmallUnit().toString();
        this.transferLabel = paymentRequest.getSoftDescriptor();
        this.channel = evaluateChannel(paymentRequest.getContractConfiguration());
    }

    private String evaluateChannel(ContractConfiguration contractConfiguration) {
        int channel = 0;
        for (ChannelKeys channelKey : ChannelKeys.values()) {

            ContractProperty contractProperty = contractConfiguration.getProperty(channelKey.getKey());
            if (contractProperty != null && Boolean.valueOf(contractProperty.getValue())) {
                channel += channelKey.getValue();
            }
        }
        // if no channel selected set default value to 63 (00111111)
        if (channel == 0) {
            channel = 63;
        }
        return String.valueOf(channel);
    }

    /**
     * create the Map used to create the body
     *
     * @return
     */
    @Override
    public Map<String, String> createBodyMap() {
        Map<String, String> bodyMap = new HashMap<>();

        // create bodyMap from mandatory fields
        bodyMap.put(BodyMapKeys.MERCHAND_ID.getKey(), getMerchantId());
        bodyMap.put(BodyMapKeys.POS_ID.getKey(), getPosId());
        bodyMap.put(BodyMapKeys.SESSION_ID.getKey(), sessionId);
        bodyMap.put(BodyMapKeys.AMOUNT.getKey(), amount);
        bodyMap.put(BodyMapKeys.CURRECNCY.getKey(), currency);
        bodyMap.put(BodyMapKeys.DESCRIPTION.getKey(), description);
        bodyMap.put(BodyMapKeys.EMAIL.getKey(), email);
        bodyMap.put(BodyMapKeys.COUNTRY.getKey(), country);
        bodyMap.put(BodyMapKeys.URL_RETURN.getKey(), urlReturn);
        bodyMap.put(BodyMapKeys.API_VERSION.getKey(), apiVersion);
        bodyMap.put(BodyMapKeys.SIGN.getKey(), signature);

        // add non mandatory fields if they exist
        addIfNotNull(bodyMap, client, BodyMapKeys.CLIENT);
        addIfNotNull(bodyMap, address, BodyMapKeys.ADDRESS);
        addIfNotNull(bodyMap, zip, BodyMapKeys.ZIP);
        addIfNotNull(bodyMap, city, BodyMapKeys.CITY);
        addIfNotNull(bodyMap, phone, BodyMapKeys.PHONE);
        addIfNotNull(bodyMap, language, BodyMapKeys.LANGUAGE);
        addIfNotNull(bodyMap, waitForResult, BodyMapKeys.WAIT_FOR_RESULT);
        addIfNotNull(bodyMap, channel, BodyMapKeys.CHANNEL);
        addIfNotNull(bodyMap, shipping, BodyMapKeys.SHIPPING);
        addIfNotNull(bodyMap, transferLabel, BodyMapKeys.TRANSFER_LABEL);
        addIfNotNull(bodyMap, encoding, BodyMapKeys.ENCODING);
        addIfNotNull(bodyMap, urlStatus, BodyMapKeys.URL_STATUS);
        addIfNotNull(bodyMap, timeLimit, BodyMapKeys.TIME_LIMIT);

        return bodyMap;
    }

    /**
     * Put not null field in bodyMap
     *
     * @param bodyMap
     * @param field
     * @param key
     */
    private void addIfNotNull(Map<String, String> bodyMap, String field, BodyMapKeys key) {
        if (field != null) {
            bodyMap.put(key.getKey(), field);
        }
    }

    /**
     * Create a hash of from fields sessionId, merchantId, amount, currency and key by calling SecurityManager.hash()
     *
     * @return the hashed String
     */
    @Override
    public String createSignature() {
        return (new SecurityManager()).hash(sessionId, getMerchantId(), amount, currency, getKey());
    }

    /**
     * check if the currency code is accepted by P24 ("PLN", "EUR", "GBP" or "CZK")
     *
     * @param currencyCode the String containing the currencyCode to compare with.
     * @return true if the currency code is accepted by P24 ("PLN", "EUR", "GBP" or "CZK"). else return false
     */
    public static boolean isGoodCurrencyCode(String currencyCode) {
        if (currencyCode == null) {
            return false;
        }
        switch (currencyCode) {
            case "PLN":
            case "EUR":
            case "GBP":
            case "CZK":
                return true;
            default:
                return false;
        }
    }
}
