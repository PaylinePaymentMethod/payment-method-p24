package com.payline.payment.p24;

import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationRequest;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;


class PaymentFormConfigurationServiceImplTest {
    @InjectMocks
    private PaymentFormConfigurationService service = new PaymentFormConfigurationServiceImpl();

    @Test
    void getPaymentFormConfiguration() {
        PaymentFormConfigurationRequest request = PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest().build();
        service.getPaymentFormConfiguration(request);
    }
}