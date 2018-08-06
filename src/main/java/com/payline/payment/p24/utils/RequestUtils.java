package com.payline.payment.p24.utils;

import com.payline.payment.p24.errors.P24ErrorMessages;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestUtils {

    private static final Logger LOG = LogManager.getLogger(RequestUtils.class);

    // FIXME message
    private static final String ERR_MESS = "";

    private static final String ERR_NO_CONTRACT_PROPERTY = "";

    /**
     * @param request ? extends Request
     * @return
     * @throws P24ValidationException
     */
    public boolean isSandbox(Request request) throws P24ValidationException {
        if (request.getPaylineEnvironment() == null) {
            throw new P24ValidationException(ERR_MESS);
        }
        return request.getPaylineEnvironment().isSandbox();
    }

    /**
     * @param request ContractParametersCheckRequest
     * @return
     * @throws P24ValidationException
     */
    public boolean isSandbox(ContractParametersCheckRequest request) throws P24ValidationException {
        if (request.getPaylineEnvironment() == null) {
            throw new P24ValidationException(ERR_MESS);
        }
        return request.getPaylineEnvironment().isSandbox();
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
            LOG.error(ERR_NO_CONTRACT_PROPERTY);
            throw new P24ValidationException(ERR_NO_CONTRACT_PROPERTY);
        }
        return property.getValue();
    }

}
