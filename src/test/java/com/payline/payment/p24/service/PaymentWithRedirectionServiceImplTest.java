package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.P24HttpClient;
import com.payline.payment.p24.utils.P24Path;
import com.payline.payment.p24.utils.SoapHelper;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;

import static com.payline.payment.p24.bean.TestUtils.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentWithRedirectionServiceImplTest {
    private final Environment environment =
            new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);

    private HttpResponse okResponse = TestUtils.createResponseOK();
    private HttpResponse koResponse = TestUtils.createResponseKO();

    private final String successSoapResponse = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/' xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns1='https://sandbox.przelewy24.pl/external/wsdl/service.php' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:SOAP-ENC='http://schemas.xmlsoap.org/soap/encoding/'>\n" +
            "  <SOAP-ENV:Body>" +
            "     <ns1:TrnBySessionIdResponse>" +
            "        <return xsi:type='ns1:TransactionResult'>" +
            "           <result xsi:type='ns1:Transaction'>" +
            "              <orderId xsi:type='xsd:int'>890857</orderId>" +
            "              <orderIdFull xsi:type='xsd:int'>109890857</orderIdFull>" +
            "              <sessionId xsi:type='xsd:string'>PL_TEST_2018-08-03_09-34-24</sessionId>" +
            "              <status xsi:type='xsd:int'>1</status>" +
            "              <amount xsi:type='xsd:int'>100</amount>" +
            "              <date xsi:type='xsd:string'>201808030942</date>" +
            "              <dateOfTransaction xsi:type='xsd:string'>201808030942</dateOfTransaction>" +
            "              <clientEmail xsi:type='xsd:string'>jan.florent@mythalesgroup.io</clientEmail>" +
            "              <accountMD5 xsi:type='xsd:string'/>" +
            "              <paymentMethod xsi:type='xsd:int'>26</paymentMethod>" +
            "              <description xsi:type='xsd:string'>Test Payline</description>" +
            "           </result>" +
            "           <error xsi:type='ns1:GeneralError'>" +
            "              <errorCode xsi:type='xsd:int'>0</errorCode>" +
            "              <errorMessage xsi:type='xsd:string'>Success, no error.</errorMessage>" +
            "           </error>" +
            "        </return>" +
            "     </ns1:TrnBySessionIdResponse>" +
            "  </SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    private InputStream is = new ByteArrayInputStream(successSoapResponse.getBytes());
    private SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);


    @InjectMocks
    private PaymentWithRedirectionServiceImpl service;

    @Mock
    private SoapHelper soapHelper;

    @Mock
    private P24HttpClient httpClient;

    public PaymentWithRedirectionServiceImplTest() throws IOException, SOAPException {
    }


    @Test
    public void finalizeRedirectionPaymentWithSoapError() {
        RedirectionPaymentRequest a = mock(RedirectionPaymentRequest.class, RETURNS_DEEP_STUBS);
        PaymentResponse response = service.finalizeRedirectionPayment(a);

        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
    }

    @Test
    public void finalizeRedirectionPaymentSuccess() throws IOException, URISyntaxException {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.getErrorCodeFromSoapResponseMessage(any(SOAPMessage.class))).thenReturn("0");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.ORDER_ID))).thenReturn("orderId");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.SOAP_ORDER_ID))).thenReturn("orderIdFull");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.EMAIL))).thenReturn("toto@toto.com");

        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withTransactionId("123")
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withEnvironment(environment)
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
                .withBrowser(new Browser("", Locale.FRANCE))
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withSoftDescriptor("")
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assert.assertEquals(PaymentResponseSuccess.class, response.getClass());
    }

    @Test
    public void finalizeRedirectionPaymentWithHttpError() throws IOException, URISyntaxException {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(koResponse);
        when(soapHelper.getErrorCodeFromSoapResponseMessage(any(SOAPMessage.class))).thenReturn("0");

        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withEnvironment(environment)
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .withBrowser(new Browser("", Locale.FRANCE))
                .withTransactionId("")
                .withSoftDescriptor("")
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals("err00", ((PaymentResponseFailure) response).getErrorCode());
    }

    @Test
    public void finalizeRedirectionPaymentWithHttpException() throws IOException, URISyntaxException {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        when(soapHelper.getErrorCodeFromSoapResponseMessage(any(SOAPMessage.class))).thenReturn("0");

        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenThrow(IOException.class);

        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withEnvironment(environment)
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
                .withBuyer(Buyer.BuyerBuilder.aBuyer().build())
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .withBrowser(new Browser("", Locale.FRANCE))
                .withTransactionId("")
                .withSoftDescriptor("")
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
    }

    @Test
    public void getVerifyError() {
        String errorMessage1 = "error=err00&errorMessage=Błąd wywołania (1)";
        String errorMessage2 = "";

        String errorCode = service.getVerifyError(errorMessage1);
        Assert.assertEquals("err00", errorCode);

        errorCode = service.getVerifyError(errorMessage2);
        Assert.assertEquals("unknown error", errorCode);
    }
}
