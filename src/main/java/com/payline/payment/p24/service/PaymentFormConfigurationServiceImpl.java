package com.payline.payment.p24.service;

import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationResponse.PaymentFormConfigurationResponseBuilder;
import com.payline.pmapi.service.PaymentFormConfigurationService;

import java.util.HashMap;

public class PaymentFormConfigurationServiceImpl implements PaymentFormConfigurationService {

    /**
     * Build a new PaymentFormConfigurationResponse
     *
     * @param paymentFormConfigurationRequest
     * @return
     */
    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        return PaymentFormConfigurationResponseBuilder.aPaymentFormConfigurationResponse().withContextPaymentForm(new HashMap<>()).build();
    }

}
