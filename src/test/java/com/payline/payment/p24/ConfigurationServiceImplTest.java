package com.payline.payment.p24;

import com.payline.payment.p24.utils.LocalizationService;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.pmapi.bean.configuration.AbstractParameter;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.ContractParametersCheckRequest.CheckRequestBuilder;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigurationServiceImplTest {
    private String lang = "FR";
    private Locale locale = new Locale(lang);

    @InjectMocks
    private ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl();

    @Mock
    private LocalizationService localization;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getName() {
        Mockito.when(localization.getSafeLocalizedString(Mockito.anyString(), Mockito.eq(locale))).thenReturn(lang);
        String result = configurationService.getName(locale);
        Assert.assertFalse(StringUtils.isEmpty(result));
        Assert.assertEquals(lang, result);

    }


    @Test
    public void isNotNumeric() {
        Assert.assertTrue(configurationService.isNotNumeric(null));
        Assert.assertTrue(configurationService.isNotNumeric(""));
        Assert.assertTrue(configurationService.isNotNumeric("foo"));
        Assert.assertFalse(configurationService.isNotNumeric("1"));
    }

    @Test
    public void checkHttpConnection() {
    }

    @Test
    public void checkSoapConnection() {
    }

    @Test
    public void getParameters() {
        List<AbstractParameter> parameters = configurationService.getParameters(locale);
        Assert.assertEquals(parameters.size(), 10);
    }

    @Test
    public void check() {
        // test a good connection test
        String goodMerchantId = "65840";
        String goodPosId = "65840";
        String goodKey = "0f67a7fec13ff180";
        String goodPassword = "76feca7a92aee7d069e32a66b7e8cef4";

        String notNumericMerchantId = "foo";
        String notNumericPosId = "bar";

        ContractParametersCheckRequest request = createContractParametersCheckRequest(goodMerchantId, goodPosId, goodKey, goodPassword);
        Map errors = configurationService.check(request);
        Assert.assertEquals(0, errors.size());

        request = createContractParametersCheckRequest(notNumericMerchantId, notNumericPosId, goodKey, goodPassword);
        errors = configurationService.check(request);
        Assert.assertEquals(2, errors.size());
    }

    @Test
    public void getReleaseInformation() {
        String version = "1.0";
        String date = "2012-12-12";

        ReleaseInformation releaseInformation = configurationService.getReleaseInformation();
        Assert.assertEquals(version, releaseInformation.getVersion());
        Assert.assertEquals(date, releaseInformation.getDate().toString());
    }


    private ContractParametersCheckRequest createContractParametersCheckRequest(String merchantId, String posId, String key, String password) {
        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(P24Constants.MERCHANT_ID, merchantId);
        accountInfo.put(P24Constants.POS_ID, posId);
        accountInfo.put(P24Constants.MERCHANT_KEY, key);
        accountInfo.put(P24Constants.MERCHANT_PASSWORD, password);

        ContractConfiguration configuration = new ContractConfiguration("test", null);
        PaylineEnvironment environment = new PaylineEnvironment("notificationURL", "redirectionURL", "redirectionCancelURL", true);
        PartnerConfiguration partnerConfiguration = new PartnerConfiguration(new HashMap<>());

        return CheckRequestBuilder.aCheckRequest()
                .withAccountInfo(accountInfo)
                .withLocale(locale)
                .withContractConfiguration(configuration)
                .withPaylineEnvironment(environment)
                .withPartnerConfiguration(partnerConfiguration)
                .build();

    }
}
