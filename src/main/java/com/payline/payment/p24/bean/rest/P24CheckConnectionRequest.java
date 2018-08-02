package com.payline.payment.p24.bean.rest;

import com.payline.payment.p24.BodyMapKeys;
import com.payline.payment.p24.utils.LocalizationService;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SecurityManager;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.p24.ConfigurationServiceImpl.WRONG_MERCHANT_ID;

public class P24CheckConnectionRequest extends P24Request {

    public P24CheckConnectionRequest(PaymentRequest paymentRequest) {
        super(paymentRequest);
    }

    public P24CheckConnectionRequest(ContractParametersCheckRequest contractParametersCheckRequest) {
        super(contractParametersCheckRequest);
    }


    @Override
    public Map<String, String> createBodyMap() {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put(BodyMapKeys.MERCHAND_ID.getKey(), getMerchantId());
        bodyMap.put(BodyMapKeys.POS_ID.getKey(), getPosId());
        bodyMap.put(BodyMapKeys.SIGN.getKey(), createSignature());
        return bodyMap;
    }

    @Override
    public String createSignature() {
        return (new SecurityManager()).hash(getPosId(), getKey());
    }

    public Map<String, String> validateRequest(LocalizationService localization, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        // check all fields values
        if (isNotNumeric(this.getMerchantId())) {
            errors.put(P24Constants.MERCHANT_ID, localization.getSafeLocalizedString(WRONG_MERCHANT_ID, locale));
        }
        String posId = this.getPosId();
        if (isNotNumeric(posId)) {
            errors.put(P24Constants.POS_ID, localization.getSafeLocalizedString("contract.posId.wrong", locale));
        }
        return errors;
    }


    private boolean isNotNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        } else {
            for (int i = 0; i < str.length(); ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return true;
                }
            }
            return false;
        }
    }

}
