package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.service.enums.BodyMapKeys;
import com.payline.payment.p24.service.enums.ChannelKeys;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class P24RegisterRequest extends P24Request {

    private static final Logger LOG = LogManager.getLogger(P24RegisterRequest.class);

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

        // mandatory fields
        Amount amountObj = paymentRequest.getAmount();
        validateAmount(amountObj);
        this.amount = amountObj.getAmountInSmallestUnit().toString();
        this.currency = amountObj.getCurrency().getCurrencyCode();

        this.sessionId = getOrderReference(paymentRequest);
        this.description = getOrderReference(paymentRequest);

        Buyer buyer = paymentRequest.getBuyer();
        validateBuyer(buyer);
        this.email = buyer.getEmail();
        Buyer.Address buyerAddress = getBuyerAdresse(buyer);
        this.country = buyerAddress.getCountry();


        this.urlReturn = getRedirectionReturnURL(paymentRequest);
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

        this.urlStatus = paymentRequest.getEnvironment().getNotificationURL();

        /**
         * FIXME attendre l'évolution de l'APM API =>
         * this.shipping = paymentRequest.getOrder().getDeliveryCharge().getAmountInSmallUnit().toString();
         */

        this.transferLabel = paymentRequest.getSoftDescriptor();
        this.channel = evaluateChannel(paymentRequest.getContractConfiguration());
    }

    private String evaluateChannel(ContractConfiguration contractConfiguration) {
        int calculatedChannel = 0;
        for (ChannelKeys channelKey : ChannelKeys.values()) {

            ContractProperty contractProperty = contractConfiguration.getProperty(channelKey.getKey());
            if (contractProperty != null && Boolean.valueOf(contractProperty.getValue())) {
                calculatedChannel += channelKey.getValue();
            }
        }
        // if no channel selected set default value to 63 (00111111)
        if (calculatedChannel == 0) {
            calculatedChannel = 63;
        }
        return String.valueOf(calculatedChannel);
    }

    /**
     * create the Map used to create the body
     *
     * @return BodyMap
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
        addIfNotNull(bodyMap, abbreviate(transferLabel, 20), BodyMapKeys.TRANSFER_LABEL);
        addIfNotNull(bodyMap, encoding, BodyMapKeys.ENCODING);
        addIfNotNull(bodyMap, urlStatus, BodyMapKeys.URL_STATUS);
        addIfNotNull(bodyMap, timeLimit, BodyMapKeys.TIME_LIMIT);

        return bodyMap;
    }

    private String abbreviate(String str, int maxLenght) {
        if (str == null || str.isEmpty() || str.length() <= maxLenght) {
            return str;
        }

        return str.substring(0, maxLenght);
    }

    /**
     * Put not null field in bodyMap
     *
     * @param bodyMap the BodyMap
     * @param field   value to put
     * @param key     key to put
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


    private Buyer.Address getBuyerAdresse(Buyer buyer) throws P24ValidationException {

        if (buyer.getAddresses() == null || buyer.getAddresses().isEmpty()) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_BUYER, P24ErrorMessages.MISSING_ADRESSE_TYPE);
        }
        Buyer.Address addr = buyer.getAddressForType(Buyer.AddressType.BILLING);
        if (addr == null || super.getRequestUtils().isEmpty(addr.getCountry())) {

            throw new P24ValidationException(P24ErrorMessages.MISSING_BUYER, P24ErrorMessages.MISSING_ADRESSE_COUNTRY);
        }
        return addr;

    }

    private void validateBuyer(Buyer buyer) throws P24ValidationException {

        if (buyer == null || super.getRequestUtils().isEmpty(buyer.getEmail())) {
            LOG.error("Invalid data : buyer's mail is mandatory");
            throw new P24ValidationException(P24ErrorMessages.MISSING_BUYER, P24ErrorMessages.MISSING_BUYER_EMAIL);
        }
    }

    private void validateAmount(Amount amount) throws P24ValidationException {

        if (amount.getAmountInSmallestUnit() == null) {
            LOG.error("Invalid data : amount in smallest unit is mandatory");
            throw new P24ValidationException(P24ErrorMessages.MISSING_AMOUNT, P24ErrorMessages.MISSING_AMOUNT_UNIT);
        }

        if (amount.getCurrency() == null || amount.getCurrency().getCurrencyCode() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_AMOUNT, P24ErrorMessages.MISSING_AMOUNT_CURRENCY);
        }
    }

    private String getRedirectionReturnURL(PaymentRequest paymentRequest) throws P24ValidationException {
        Environment environment = paymentRequest.getEnvironment();
        if (environment == null
                || super.getRequestUtils().isEmpty(environment.getRedirectionReturnURL())) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_ENVIRONNEMENT, P24ErrorMessages.MISSING_RETURN_URL);
        }

        return environment.getRedirectionReturnURL();
    }

    private String getOrderReference(PaymentRequest paymentRequest) throws P24ValidationException {
        Order order = paymentRequest.getOrder();
        if (super.getRequestUtils().isEmpty(order.getReference())) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_ORDER, P24ErrorMessages.MISSING_ORDER_REF);
        }

        return order.getReference();
    }

}
