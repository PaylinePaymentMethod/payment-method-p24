package com.payline.payment.p24.service;


import com.payline.payment.p24.bean.TestUtils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.bean.form.NoFieldForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;


@RunWith(MockitoJUnitRunner.class)
public class PaymentFormConfigurationServiceImplTest {

    @InjectMocks
    private PaymentFormConfigurationServiceImpl service;

    @Test
    public void testGetPaymentFormConfiguration() {
        // when: getPaymentFormConfiguration is called
        final PaymentFormConfigurationRequest paymentFormConfigurationRequest = PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withLocale(Locale.FRANCE)
                .withEnvironment(new Environment("","","",true))
                .withPartnerConfiguration(new PartnerConfiguration(Collections.emptyMap(),Collections.emptyMap()))
                .withContractConfiguration(new ContractConfiguration("",Collections.emptyMap()))
                .withOrder(Order.OrderBuilder.anOrder().withReference("ref").build())
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withAmount(new Amount(BigInteger.TEN, Currency.getInstance("EUR")))
                .build();        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(paymentFormConfigurationRequest);

        // then: returned object is an instance of PaymentFormConfigurationResponseProvided
        Assert.assertTrue(response instanceof PaymentFormConfigurationResponseSpecific);
        PaymentFormConfigurationResponseSpecific paymentFormConfigurationResponse = (PaymentFormConfigurationResponseSpecific) response;
        Assert.assertTrue(paymentFormConfigurationResponse.getPaymentForm() instanceof NoFieldForm);
        NoFieldForm noFieldForm = (NoFieldForm) paymentFormConfigurationResponse.getPaymentForm();
        Assert.assertFalse(noFieldForm.getButtonText().isEmpty());
        Assert.assertFalse(noFieldForm.getDescription().isEmpty());
        Assert.assertTrue(noFieldForm.isDisplayButton());
    }

    @Test
    public void testGetLogo(){
        // when: getLogo is called
        PaymentFormLogo paymentFormLogo = service.getLogo("",Locale.getDefault());

        // then: returned elements are not null
        Assert.assertNotNull(paymentFormLogo);
        Assert.assertNotNull(paymentFormLogo.getFile());
        Assert.assertNotNull(paymentFormLogo.getContentType());
    }

    @Test
    public void testGetPaymentFormLogo() throws IOException {
        // given: the logo image read from resources
        String filename = "p24-logo.png";
        InputStream input = PaymentFormConfigurationServiceImpl.class.getClassLoader().getResourceAsStream(filename);
        BufferedImage image = ImageIO.read(input);

        // when: getPaymentFormLogo is called
        PaymentFormLogoRequest request = PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withLocale(Locale.getDefault())
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withPartnerConfiguration(new PartnerConfiguration(null, null))
                .withEnvironment(new Environment("","","",true))
                .build();
        PaymentFormLogoResponse paymentFormLogoResponse = service.getPaymentFormLogo(request);

        // then: returned elements match the image file data
        Assert.assertTrue(paymentFormLogoResponse instanceof PaymentFormLogoResponseFile);
        PaymentFormLogoResponseFile casted = (PaymentFormLogoResponseFile) paymentFormLogoResponse;
        Assert.assertEquals(image.getHeight(), casted.getHeight());
        Assert.assertEquals(image.getWidth(), casted.getWidth());
        Assert.assertNotNull(casted.getTitle());
        Assert.assertNotNull(casted.getAlt());
    }

}
