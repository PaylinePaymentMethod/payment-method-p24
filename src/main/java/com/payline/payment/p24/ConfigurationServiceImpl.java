package com.payline.payment.p24;

import com.payline.payment.p24.bean.rest.P24CheckConnectionRequest;
import com.payline.payment.p24.bean.soap.P24CheckAccessRequest;
import com.payline.payment.p24.utils.*;
import com.payline.pmapi.bean.configuration.*;
import com.payline.pmapi.service.ConfigurationService;
import okhttp3.Response;

import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;

public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String CONTRACT = "contract.";
    private static final String LABEL = ".label";
    private static final String DESCRIPTION = ".description";
    private static final String VERSION = "1.0";
    private static final String RELEASE_DATE = "12/12/2012";

    // Errors messages
    public static final String WRONG_MERCHANT_ID = "contract.merchantId.wrong";

    private LocalizationService localization;

    private P24HttpClient p24HttpClient;

    private RequestUtils requestUtils;

    public ConfigurationServiceImpl() {
        localization = LocalizationImpl.getInstance();
        p24HttpClient = new P24HttpClient();
        requestUtils = new RequestUtils();
    }


    private Map<String, String> getErrors(String responseMessage, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        Matcher m = P24Constants.REGEX_ERROR_MESSAGES.matcher(responseMessage);

        if (m.find()) {
            // put error message in errors map
            for (int i = 1; i <= m.groupCount(); i++) {
                String[] keyVal = m.group(i).split(":");
                switch (keyVal[0]) {
                    case "p24_sign":
                        errors.put(P24Constants.MERCHANT_KEY, localization.getSafeLocalizedString("contract.key.wrong", locale));
                        break;
                    default:    // corresponding to case "p24_merchant_id" or "p24_pos_id"
                        errors.put(P24Constants.MERCHANT_ID, localization.getSafeLocalizedString(WRONG_MERCHANT_ID, locale));
                        errors.put(P24Constants.POS_ID, localization.getSafeLocalizedString("contract.posId.wrong", locale));
                        break;
                }
            }
        } else {
            errors.put(ContractParametersCheckRequest.GENERIC_ERROR, localization.getSafeLocalizedString("contract.error.wrongServerResponse", locale));
        }

        return errors;
    }

    public void checkHttpConnection(P24CheckConnectionRequest request, Map<String, String> errors, Locale locale) {
        try {
            // create the body
            Map<String, String> bodyMap = request.createBodyMap();

            boolean isSandbox = requestUtils.isSandbox(request);

            // do the request
            String host = P24Url.REST_HOST.getUrl(isSandbox);
            Response response = p24HttpClient.doPost(host, P24Path.CHECK, bodyMap);

            // parse the response
            if (response.code() != 200) {
                errors.put(ContractParametersCheckRequest.GENERIC_ERROR, localization.getSafeLocalizedString("contract.error.wrongServerResponse", locale));
            } else {

                String responseMessage = response.body().string();
                if (!"error=0".equals(responseMessage)) {
                    // response message contains errors
                    errors.putAll(getErrors(responseMessage, locale));
                }

            }

        } catch (IOException e) {
            errors.put(ContractParametersCheckRequest.GENERIC_ERROR, localization.getSafeLocalizedString("contract.error.networkError", locale));

        } catch (P24InvalidRequestException e) {
            // FIXME
        }
    }

    public void checkSoapConnection(P24CheckAccessRequest request, boolean isSandbox, Map<String, String> errors, Locale locale) {
        SOAPMessage soapResponseMessage = SoapHelper.sendSoapMessage(
                request.buildSoapMessage(isSandbox),
                P24Url.SOAP_ENDPOINT.getUrl(isSandbox));

        if (soapResponseMessage != null) {
            String tag = SoapHelper.getTagContentFromSoapResponseMessage(soapResponseMessage, "return");
            if (!"true".equals(tag)) {
                errors.put(P24Constants.MERCHANT_ID, localization.getSafeLocalizedString(WRONG_MERCHANT_ID, locale));
                errors.put(P24Constants.MERCHANT_PASSWORD, localization.getSafeLocalizedString("contract.password.wrong", locale));
            }
        } else {
            errors.put(ContractParametersCheckRequest.GENERIC_ERROR, localization.getSafeLocalizedString("contract.error.networkError", locale));
        }
    }

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // merchant id
        final InputParameter merchantId = new InputParameter();
        merchantId.setKey(P24Constants.MERCHANT_ID);
        merchantId.setLabel(localization.getSafeLocalizedString("contract.merchantId.label", locale));
        merchantId.setDescription(localization.getSafeLocalizedString("contract.merchantId.description", locale));
        merchantId.setRequired(true);

        parameters.add(merchantId);

        // pos id
        final InputParameter merchantPos = new InputParameter();
        merchantPos.setKey(P24Constants.POS_ID);
        merchantPos.setLabel(localization.getSafeLocalizedString("contract.merchantPos.label", locale));
        merchantPos.setDescription(localization.getSafeLocalizedString("contract.merchantPos.description", locale));
        merchantPos.setRequired(false);  // by default is the same as merchant id

        parameters.add(merchantPos);

        // key shared between the merchant and P24
        final InputParameter merchantKey = new InputParameter();
        merchantKey.setKey(P24Constants.MERCHANT_KEY);
        merchantKey.setLabel(localization.getSafeLocalizedString("contract.merchantKey.label", locale));
        merchantKey.setDescription(localization.getSafeLocalizedString("contract.merchantKey.description", locale));
        merchantKey.setRequired(true);

        parameters.add(merchantKey);

        // P24 password of the merchant
        final InputParameter merchantPassword = new InputParameter();
        merchantPassword.setKey(P24Constants.MERCHANT_PASSWORD);
        merchantPassword.setLabel(localization.getSafeLocalizedString("contract.merchantPassword.label", locale));
        merchantPassword.setDescription(localization.getSafeLocalizedString("contract.merchantPassword.description", locale));
        merchantPassword.setRequired(true);

        parameters.add(merchantPassword);

        // channels
        addChannelsListBoxes(parameters, locale);

        return parameters;
    }

    private void addChannelsListBoxes(List<AbstractParameter> parameters, Locale locale) {
        Map<String, String> listBoxList = new HashMap<>();
        listBoxList.put(localization.getSafeLocalizedString("contract.yes", locale), "true");
        listBoxList.put(localization.getSafeLocalizedString("contract.no", locale), "false");

        for (ChannelKeys channelKey : ChannelKeys.values()) {
            final ListBoxParameter channel = new ListBoxParameter();
            String key = channelKey.getKey();
            channel.setKey(key);
            channel.setLabel(localization.getSafeLocalizedString(CONTRACT.concat(key).concat(LABEL), locale));
            channel.setDescription(localization.getSafeLocalizedString(CONTRACT.concat(key).concat(DESCRIPTION), locale));
            channel.setList(listBoxList);
            parameters.add(channel);
        }
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        Locale locale = contractParametersCheckRequest.getLocale();

        // get all fields to check
        final Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
        final String password = accountInfo.get(P24Constants.MERCHANT_PASSWORD);


        P24CheckConnectionRequest connectionRequest = new P24CheckConnectionRequest(contractParametersCheckRequest);
        Map<String, String> errors = connectionRequest.validateRequest(localization, locale);

        if (errors.isEmpty()) {
            // test the http connection
            //TODO
            checkHttpConnection(connectionRequest, errors, locale);

            if (errors.isEmpty()) {
                // test the soap connection
                boolean isSandBox = false;
                try {
                    isSandBox = requestUtils.isSandbox(contractParametersCheckRequest);
                } catch (P24InvalidRequestException e) {
                    e.printStackTrace();
                }
                P24CheckAccessRequest testAccessRequest =
                        new P24CheckAccessRequest(connectionRequest.getMerchantId(), password);
                checkSoapConnection(testAccessRequest, isSandBox, errors, locale);
            }
        }

        // return all errors
        return errors;
    }

    @Override
    public ReleaseInformation getReleaseInformation() {
        LocalDate date = LocalDate.parse(RELEASE_DATE, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return ReleaseInformation.ReleaseBuilder.aRelease().withDate(date).withVersion(VERSION).build();
    }

    @Override
    public String getName(Locale locale) {
        return localization.getSafeLocalizedString("project.name", locale);

    }
}
