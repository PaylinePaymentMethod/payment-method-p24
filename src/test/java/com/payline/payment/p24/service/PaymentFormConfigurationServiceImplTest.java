package com.payline.payment.p24.service;

import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentForm.PaymentFormConfigurationResponse;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class PaymentFormConfigurationServiceImplTest {

    @InjectMocks
    private PaymentFormConfigurationService configurationService = new PaymentFormConfigurationServiceImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getPaymentFormConfiguration_null() {
        PaymentFormConfigurationResponse reponse = configurationService.getPaymentFormConfiguration(null);
        Assert.assertNotNull(reponse);
    }

    @Test
    public void getPaymentFormConfiguration_notNull() {
        PaymentFormConfigurationRequest paymentFormConfigurationRequest =
                PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder
                        .aPaymentFormConfigurationRequest()
                        .build();

        PaymentFormConfigurationResponse reponse =
                configurationService.getPaymentFormConfiguration(paymentFormConfigurationRequest);
        Assert.assertNotNull(reponse);
    }

}
