package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.BodyMapKeys;
import com.payline.payment.p24.ChannelKeys;
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
    private String timeLimit;
    private String waitForResult;
    private String channel;
    private String shipping;
    private String transferLabel;
    private String encoding = P24Constants.ENCODING;


    public P24RegisterRequest(PaymentRequest paymentRequest) {
        super(paymentRequest);

        // Buyer can't be null
        this.sessionId = paymentRequest.getOrder().getReference();
        this.amount = paymentRequest.getAmount().getAmountInSmallestUnit().toString();
        this.currency = paymentRequest.getAmount().getCurrency().getCurrencyCode();    // todo peut etre une verif du code: PLN, EUR, GBP, CZK
        this.description = paymentRequest.getSoftDescriptor();
        this.email = paymentRequest.getBuyer().getEmail();
        this.country = paymentRequest.getBuyer().getAddresses().get(Buyer.AddressType.BILLING).getCountry();
        this.urlReturn = paymentRequest.getPaylineEnvironment().getRedirectionReturnURL();
        this.signature = createSignature();

        Buyer.Address buyerAddress = paymentRequest.getBuyer().getAddresses().get(Buyer.AddressType.DELIVERY);
        this.client = paymentRequest.getBuyer().getFullName().toString();
        this.address = buyerAddress.getStreet1();   // todo peut etre ajouter le getStreet2???
        this.zip = buyerAddress.getZipCode();
        this.city = buyerAddress.getCity();

        if (paymentRequest.getBuyer().getPhoneNumbers() != null) {
            this.phone = paymentRequest.getBuyer().getPhoneNumberForType(Buyer.PhoneNumberType.BILLING);
        }

        this.language = paymentRequest.getLocale().getLanguage();

        this.urlStatus = paymentRequest.getPaylineEnvironment().getNotificationURL();

        this.timeLimit = paymentRequest.getContractConfiguration().getProperty(P24Constants.TIME_LIMIT).getValue();
        this.waitForResult = paymentRequest.getContractConfiguration().getProperty(P24Constants.WAIT_FOR_RESULT).getValue();
        this.shipping = paymentRequest.getContractConfiguration().getProperty(P24Constants.SHIPPING).getValue();

        this.transferLabel = this.description;

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
        bodyMap.put(BodyMapKeys.SIGN.getKey(), this.signature);

        // add non mandatory fields if they exist
        if (client != null) bodyMap.put(BodyMapKeys.CLIENT.getKey(), client);
        if (address != null) bodyMap.put(BodyMapKeys.ADDRESS.getKey(), address);
        if (zip != null) bodyMap.put(BodyMapKeys.ZIP.getKey(), zip);
        if (city != null) bodyMap.put(BodyMapKeys.CITY.getKey(), city);
        if (phone != null) bodyMap.put(BodyMapKeys.PHONE.getKey(), phone);
        if (language != null) bodyMap.put(BodyMapKeys.LANGUAGE.getKey(), language);
        if (waitForResult != null) bodyMap.put(BodyMapKeys.WAIT_FOR_RESULT.getKey(), waitForResult);
        if (channel != null) bodyMap.put(BodyMapKeys.CHANNEL.getKey(), channel);
        if (shipping != null) bodyMap.put(BodyMapKeys.SHIPPING.getKey(), shipping);
        if (transferLabel != null) bodyMap.put(BodyMapKeys.TRANSFER_LABEL.getKey(), transferLabel);
        if (encoding != null) bodyMap.put(BodyMapKeys.ENCODING.getKey(), encoding);
        if (urlStatus != null) bodyMap.put(BodyMapKeys.URL_STATUS.getKey(), urlStatus);
        if (timeLimit != null) bodyMap.put(BodyMapKeys.TIME_LIMIT.getKey(), timeLimit);

        return bodyMap;
    }

    @Override
    public String createSignature() {
        return (new SecurityManager()).hash(sessionId, getMerchantId(), amount, currency, getKey());
    }
}
