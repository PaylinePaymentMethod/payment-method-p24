package com.payline.payment.p24.service;

import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationRequest;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class PaymentFormConfigurationServiceImplTest {

    @InjectMocks
    private PaymentFormConfigurationService service;

    public void setUp() {
        service = new PaymentFormConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);

    }

    @Test
    void getPaymentFormConfiguration() {
        PaymentFormConfigurationRequest request = PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest().build();
        service.getPaymentFormConfiguration(request);
    }

}