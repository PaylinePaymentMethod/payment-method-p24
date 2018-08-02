package com.payline.payment.p24.utils;

import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;

public class RequestUtils {

    // FIXME message
    private static String ERR_MESS = "";

    /**
     * @param request ? extends Request
     * @return
     * @throws P24InvalidRequestException
     */
    public boolean isSandbox(Request request) throws P24InvalidRequestException {
        if (request.getPaylineEnvironment() == null) {
            throw new P24InvalidRequestException(ERR_MESS);
        }
        return request.getPaylineEnvironment().isSandbox();
    }

    /**
     * @param request ContractParametersCheckRequest
     * @return
     * @throws P24InvalidRequestException
     */
    public boolean isSandbox(ContractParametersCheckRequest request) throws P24InvalidRequestException {
        if (request.getPaylineEnvironment() == null) {
            throw new P24InvalidRequestException(ERR_MESS);
        }
        return request.getPaylineEnvironment().isSandbox();
    }
}
