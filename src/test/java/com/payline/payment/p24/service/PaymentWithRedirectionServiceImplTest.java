package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.P24HttpClient;
import com.payline.payment.p24.utils.P24Path;
import com.payline.payment.p24.utils.SoapHelper;
import com.payline.pmapi.bean.payment.PaylineEnvironment;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.PaymentResponseSuccess;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.payline.payment.p24.bean.TestUtils.CANCEL_URL;
import static com.payline.payment.p24.test.integration.AbstractPaymentIntegration.NOTIFICATION_URL;
import static com.payline.payment.p24.test.integration.AbstractPaymentIntegration.SUCCESS_URL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentWithRedirectionServiceImplTest {
    private final PaylineEnvironment paylineEnvironment =
            new PaylineEnvironment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);

    private HttpUrl url = new HttpUrl.Builder().scheme("http").host("host").addPathSegment("path").build();
    private Request request = new Request.Builder().url(url).build();
    private ResponseBody bodyOK = ResponseBody.create(MediaType.parse("plain/text"), "error=0");
    private final Response okResponse = new Response.Builder().code(200).request(request).protocol(Protocol.HTTP_2).body(bodyOK).message("foo").build();

    private ResponseBody bodyKO = ResponseBody.create(MediaType.parse("plain/text"), "error=err00&errorMessage=Błąd wywołania (1)");
    private final Response koResponse = new Response.Builder().code(200).request(request).protocol(Protocol.HTTP_2).body(bodyKO).message("foo").build();


    private final String successSoapResponse = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://sandbox.przelewy24.pl/external/wsdl/service.php\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "  <SOAP-ENV:Body>\n" +
            "     <ns1:TrnBySessionIdResponse>\n" +
            "        <return xsi:type=\"ns1:TransactionResult\">\n" +
            "           <result xsi:type=\"ns1:Transaction\">\n" +
            "              <orderId xsi:type=\"xsd:int\">890857</orderId>\n" +
            "              <orderIdFull xsi:type=\"xsd:int\">109890857</orderIdFull>\n" +
            "              <sessionId xsi:type=\"xsd:string\">PL_TEST_2018-08-03_09-34-24</sessionId>\n" +
            "              <status xsi:type=\"xsd:int\">1</status>\n" +
            "              <amount xsi:type=\"xsd:int\">100</amount>\n" +
            "              <date xsi:type=\"xsd:string\">201808030942</date>\n" +
            "              <dateOfTransaction xsi:type=\"xsd:string\">201808030942</dateOfTransaction>\n" +
            "              <clientEmail xsi:type=\"xsd:string\">jan.florent@mythalesgroup.io</clientEmail>\n" +
            "              <accountMD5 xsi:type=\"xsd:string\"/>\n" +
            "              <paymentMethod xsi:type=\"xsd:int\">26</paymentMethod>\n" +
            "              <description xsi:type=\"xsd:string\">Test Payline</description>\n" +
            "           </result>\n" +
            "           <error xsi:type=\"ns1:GeneralError\">\n" +
            "              <errorCode xsi:type=\"xsd:int\">0</errorCode>\n" +
            "              <errorMessage xsi:type=\"xsd:string\">Success, no error.</errorMessage>\n" +
            "           </error>\n" +
            "        </return>\n" +
            "     </ns1:TrnBySessionIdResponse>\n" +
            "  </SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>";

    InputStream is = new ByteArrayInputStream(successSoapResponse.getBytes());
    SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);


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
    public void finalizeRedirectionPaymentSuccess() throws IOException {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(okResponse);
        when(soapHelper.getErrorCodeFromSoapResponseMessage(any(SOAPMessage.class))).thenReturn("0");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.ORDER_ID))).thenReturn("orderId");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.EMAIL))).thenReturn("toto@toto.com");


        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withRedirectionContext("test")
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assert.assertEquals(PaymentResponseSuccess.class, response.getClass());
    }

    @Test
    public void finalizeRedirectionPaymentWithHttpError() throws IOException {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenReturn(koResponse);
        when(soapHelper.getErrorCodeFromSoapResponseMessage(any(SOAPMessage.class))).thenReturn("0");

        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withRedirectionContext("test")
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());

        System.out.println(((PaymentResponseFailure) response).getErrorCode());
        Assert.assertEquals("err00", ((PaymentResponseFailure) response).getErrorCode());

    }

    @Test
    public void finalizeRedirectionPaymentWithHttpException() throws IOException {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        when(httpClient.doPost(anyString(), any(P24Path.class), anyMap())).thenThrow(IOException.class);

        RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withRedirectionContext("test")
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withPaylineEnvironment(paylineEnvironment)
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
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
