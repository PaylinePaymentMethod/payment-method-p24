package com.payline.payment.p24;

import com.payline.payment.p24.utils.LocalizationService;
import com.payline.pmapi.service.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

public class ConfigurationServiceImplTest {

    @InjectMocks
    ConfigurationService configurationService = new ConfigurationServiceImpl();

    @Mock
    private LocalizationService localization;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getName() {

        String lang = "FR";
        Locale loc = new Locale(lang);
        Mockito.when(localization.getSafeLocalizedString(Mockito.anyString(), Mockito.eq(loc))).thenReturn(lang);
        String result = configurationService.getName(loc);
        Assert.assertFalse(StringUtils.isEmpty(result));
        Assert.assertEquals(lang, result);
    }

}
