package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.bean.rest.P24CheckConnectionRequest;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.*;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ConfigurationServiceImplTest {
    private String goodMerchantId = "65840";
    private String goodPosId = "65840";
    private String goodKey = "0f67a7fec13ff180";
    private String goodPazzword = "76feca7a92aee7d069e32a66b7e8cef4";
    private String notNumericMerchantId = "foo";
    private String notNumericPosId = "bar";

    private String lang = "FR";
    private Locale locale = new Locale(lang);

    private HttpResponse okResponse = TestUtils.createResponseOK();

    private String soapOK = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://sandbox.przelewy24.pl/external/wsdl/service.php\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
            "  <SOAP-ENV:Body>" +
            "     <ns1:TestAccessResponse>" +
            "        <return xsi:type=\"xsd:boolean\">true</return>" +
            "     </ns1:TestAccessResponse>" +
            "  </SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    private String soapKO = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://sandbox.przelewy24.pl/external/wsdl/service.php\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
            "  <SOAP-ENV:Body>" +
            "     <ns1:TestAccessResponse>" +
            "        <return xsi:type=\"xsd:boolean\">false</return>" +
            "     </ns1:TestAccessResponse>" +
            "  </SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    private InputStream isOK = new ByteArrayInputStream(soapOK.getBytes());
    private SOAPMessage messageOK = MessageFactory.newInstance().createMessage(null, isOK);
    private InputStream isKO = new ByteArrayInputStream(soapKO.getBytes());
    private SOAPMessage messageKO = MessageFactory.newInstance().createMessage(null, isKO);

    @InjectMocks
    private ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl();

    @Mock
    private LocalizationService localization;
    @Mock
    private P24HttpClient httpClient;
    @Mock
    private SoapHelper soapHelper;
    @Mock
    private RequestUtils requestUtils;

    public ConfigurationServiceImplTest() throws IOException, SOAPException {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getName() {
        when(localization.getSafeLocalizedString(anyString(), eq(locale))).thenReturn(lang);
        String result = configurationService.getName(locale);
        Assert.assertFalse(StringUtils.isEmpty(result));
        Assert.assertEquals(lang, result);
    }

    @Test
    public void checkHttpConnectionOK() throws IOException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);

        P24CheckConnectionRequest checkConnectionRequest = new P24CheckConnectionRequest(createContractParametersCheckRequest("a", "a", "a", "a"));

        Map<String, String> errors = new HashMap<>();
        configurationService.checkHttpConnection(true, checkConnectionRequest, errors, locale);

        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void checkHttpConnectionWithErrorCode() throws IOException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(TestUtils.createResponse(500, "foo"));

        P24CheckConnectionRequest checkConnectionRequest = new P24CheckConnectionRequest(createContractParametersCheckRequest("a", "a", "a", "a"));

        Map<String, String> errors = new HashMap<>();
        configurationService.checkHttpConnection(true, checkConnectionRequest, errors, locale);

        Assert.assertEquals(1, errors.size());
    }

    @Test
    public void checkHttpConnectionWithErrorResponse() throws IOException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(TestUtils.createResponse(200, "I am not the good response"));

        P24CheckConnectionRequest checkConnectionRequest = new P24CheckConnectionRequest(createContractParametersCheckRequest("a", "a", "a", "a"));

        Map<String, String> errors = new HashMap<>();
        configurationService.checkHttpConnection(true, checkConnectionRequest, errors, locale);

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.keySet().contains(ContractParametersCheckRequest.GENERIC_ERROR));
    }

    @Test
    public void checkHttpConnectionParseErrorResponse_p24_sign() throws IOException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(TestUtils.createResponse(200, "error=125&=errorMessage=p24_sign:15"));

        P24CheckConnectionRequest checkConnectionRequest = new P24CheckConnectionRequest(createContractParametersCheckRequest("a", "a", "a", "a"));

        Map<String, String> errors = new HashMap<>();
        configurationService.checkHttpConnection(true, checkConnectionRequest, errors, locale);

        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.keySet().contains(P24Constants.MERCHANT_KEY));
    }

    @Test
    public void checkHttpConnectionParseErrorResponse_other() throws IOException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(TestUtils.createResponse(200, "error=125&=errorMessage=toto:15"));

        P24CheckConnectionRequest checkConnectionRequest = new P24CheckConnectionRequest(createContractParametersCheckRequest("a", "a", "a", "a"));

        Map<String, String> errors = new HashMap<>();
        configurationService.checkHttpConnection(true, checkConnectionRequest, errors, locale);

        Assert.assertEquals(2, errors.size());
        Assert.assertTrue(errors.keySet().contains(P24Constants.MERCHANT_ID));
        Assert.assertTrue(errors.keySet().contains(P24Constants.POS_ID));
    }


    @Test
    public void checkHttpConnectionWithException() throws URISyntaxException, IOException {
        // create response object
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenThrow(IOException.class);

        P24CheckConnectionRequest checkConnectionRequest = new P24CheckConnectionRequest(createContractParametersCheckRequest("a", "a", "a", "a"));

        Map<String, String> errors = new HashMap<>();
        configurationService.checkHttpConnection(true, checkConnectionRequest, errors, locale);

        Assert.assertEquals(1, errors.size());
    }


    @Test
    public void getParameters() {
        List<AbstractParameter> parameters = configurationService.getParameters(locale);
        Assert.assertEquals(10, parameters.size());
    }

    @Test
    public void checkOK() throws IOException, P24ValidationException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(messageOK);
        when(requestUtils.isSandbox(any(com.payline.pmapi.bean.Request.class))).thenReturn(true);
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq("return"))).thenReturn("true");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.EMAIL))).thenReturn("toto@toto.com");

        ContractParametersCheckRequest request = createContractParametersCheckRequest(goodMerchantId, goodPosId, goodKey, goodPazzword);
        Map errors = configurationService.check(request);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void checkWithRestKO() throws IOException, P24ValidationException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(messageOK);
        when(requestUtils.isSandbox(any(com.payline.pmapi.bean.Request.class))).thenReturn(true);
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq("return"))).thenReturn("true");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.EMAIL))).thenReturn("toto@toto.com");


        ContractParametersCheckRequest request = createContractParametersCheckRequest(notNumericMerchantId, notNumericPosId, goodKey, goodPazzword);
        Map errors = configurationService.check(request);
        Assert.assertEquals(2, errors.size());
    }

    @Test
    public void checkWithSoapKO() throws IOException, P24ValidationException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(messageKO);
        when(requestUtils.isSandbox(any(com.payline.pmapi.bean.Request.class))).thenReturn(true);

        ContractParametersCheckRequest request = createContractParametersCheckRequest(goodMerchantId, goodPosId, goodKey, goodPazzword);
        Map errors = configurationService.check(request);
        Assert.assertEquals(2, errors.size());
    }

    @Test
    public void checkWithSoapError() throws IOException, P24ValidationException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(null);
        when(requestUtils.isSandbox(any(com.payline.pmapi.bean.Request.class))).thenReturn(true);

        ContractParametersCheckRequest request = createContractParametersCheckRequest(goodMerchantId, goodPosId, goodKey, goodPazzword);
        Map errors = configurationService.check(request);
        Assert.assertEquals(1, errors.size());
    }

    @Test
    public void checkWithRequestUtilError() throws IOException, P24ValidationException, URISyntaxException {
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(messageOK);
        when(requestUtils.isSandbox(any(ContractParametersCheckRequest.class))).thenThrow(P24ValidationException.class);

        ContractParametersCheckRequest request = createContractParametersCheckRequest(goodMerchantId, goodPosId, goodKey, goodPazzword);
        Map errors = configurationService.check(request);
        Assert.assertEquals(1, errors.size());
    }

    @Test
    public void getReleaseInformation() {
        ReleaseInformation releaseInformation = configurationService.getReleaseInformation();
        Assert.assertNotNull(releaseInformation.getVersion());
        Assert.assertNotNull(releaseInformation.getDate());
    }


    private ContractParametersCheckRequest createContractParametersCheckRequest(String merchantId, String posId, String key, String password) {
        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(P24Constants.MERCHANT_ID, merchantId);
        accountInfo.put(P24Constants.POS_ID, posId);
        accountInfo.put(P24Constants.MERCHANT_KEY, key);
        accountInfo.put(P24Constants.MERCHANT_MDP, password);

        ContractConfiguration configuration = new ContractConfiguration("test", null);
        PaylineEnvironment environment = new PaylineEnvironment("notificationURL", "redirectionURL", "redirectionCancelURL", true);
        PartnerConfiguration partnerConfiguration = new PartnerConfiguration(new HashMap<>(), new HashMap<>());

        return ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                .withAccountInfo(accountInfo)
                .withLocale(locale)
                .withContractConfiguration(configuration)
                .withPaylineEnvironment(environment)
                .withPartnerConfiguration(partnerConfiguration)
                .build();
    }
}
