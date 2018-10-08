package com.payline.payment.p24.utils;

import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestUtils {

    private static final Logger LOG = LogManager.getLogger(RequestUtils.class);

    /**
     * @param request ? extends Request
     * @return
     * @throws P24ValidationException
     */
    public boolean isSandbox(Request request) throws P24ValidationException {
        if (request.getEnvironment() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_ENVIRONNEMENT);
        }
        return request.getEnvironment().isSandbox();
    }

    /**
     * @param request ContractParametersCheckRequest
     * @return
     * @throws P24ValidationException
     */
    public boolean isSandbox(ContractParametersCheckRequest request) throws P24ValidationException {
        if (request.getEnvironment() == null) {
            throw new P24ValidationException(P24ErrorMessages.MISSING_ENVIRONNEMENT);
        }
        return request.getEnvironment().isSandbox();
    }

    /**
     * {@ContractConfiguration} null safe and {@ContractProperty null} safe
     * equivalent to: getContractConfiguration#getProperty(key)
     *
     * @param request
     * @param key
     * @return
     * @throws P24ValidationException
     */
    public String getContractValue(Request request, String key) throws P24ValidationException {
        if (request.getContractConfiguration() == null) {
            LOG.error(P24ErrorMessages.MISSING_CONTRACT);
            throw new P24ValidationException(P24ErrorMessages.MISSING_CONTRACT);
        }
        ContractProperty property = request.getContractConfiguration().getProperty(key);
        if (property == null) {
            LOG.error("Paramètre obligatoire : %s", key);
            throw new P24ValidationException(P24ErrorMessages.MISSING_PARAMETER, key);
        }
        return property.getValue();
    }


    public boolean isNotNumeric(String str) {
        if (isEmpty(str)) {
            return true;
        }

        for (int i = 0; i < str.length(); ++i) {
            if (!Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;

    }

    public boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
