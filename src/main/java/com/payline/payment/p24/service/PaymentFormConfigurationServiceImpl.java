package com.payline.payment.p24.service;

import com.payline.payment.p24.utils.LocalizationImpl;
import com.payline.payment.p24.utils.LocalizationService;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.bean.form.NoFieldForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class PaymentFormConfigurationServiceImpl implements PaymentFormConfigurationService {
    private static final Logger logger = LogManager.getLogger("PaymentFormConfigurationService");

    private static final String LOGO_CONTENT_TYPE = "image/png";
    private static final int LOGO_HEIGHT = 25;
    private static final int LOGO_WIDTH = 56;
    public static final String BUTTON_KEY = "form.buttonText";
    public static final String DESCRYPTION_KEY = "form.description";
    public static final String PROJECT_NAME_KEY = "project.name";

    private LocalizationService localization;

    public PaymentFormConfigurationServiceImpl() {
        localization = LocalizationImpl.getInstance();
    }

    /**
     * Build a new PaymentFormConfigurationResponse
     *
     * @param paymentFormConfigurationRequest
     * @return
     */
    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        final Locale locale = paymentFormConfigurationRequest.getLocale();
        return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder.aPaymentFormConfigurationResponseSpecific().withPaymentForm(NoFieldForm.NoFieldFormBuilder.aNoFieldForm()
                .withButtonText(localization.getSafeLocalizedString(BUTTON_KEY, locale))
                .withDescription(localization.getSafeLocalizedString(DESCRYPTION_KEY, locale))
                .withDisplayButton(true)
                .build()).build();
    }

    @Override
    public PaymentFormLogoResponse getPaymentFormLogo(PaymentFormLogoRequest paymentFormLogoRequest) {
        final Locale locale = paymentFormLogoRequest.getLocale();
        return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                .withHeight(LOGO_HEIGHT)
                .withWidth(LOGO_WIDTH)
                .withTitle(localization.getSafeLocalizedString(PROJECT_NAME_KEY, locale))
                .withAlt(localization.getSafeLocalizedString(PROJECT_NAME_KEY, locale))
                .build();
    }

    @Override
    public PaymentFormLogo getLogo(String var1, Locale locale) {
        try {
            // Read logo file
            InputStream input = PaymentFormConfigurationServiceImpl.class.getClassLoader().getResourceAsStream("p24-logo.png");
            BufferedImage logo = ImageIO.read(input);
            // Recover byte array from image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(logo, "png", baos);

            return PaymentFormLogo.PaymentFormLogoBuilder.aPaymentFormLogo()
                    .withFile(baos.toByteArray())
                    .withContentType(LOGO_CONTENT_TYPE)
                    .build();
        } catch (IOException e) {
            logger.error("Unable to load the logo", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
