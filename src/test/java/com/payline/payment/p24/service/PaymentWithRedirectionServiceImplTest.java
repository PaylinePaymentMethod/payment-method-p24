package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.SoapHelper;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.PaymentResponseFailure;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentWithRedirectionServiceImplTest {

    @InjectMocks
    private PaymentWithRedirectionServiceImpl service;

    @Mock
    private SoapHelper soapHelper;


    @Test
    public void finalizeRedirectionPaymentWithSoapError() {
        RedirectionPaymentRequest a = mock(RedirectionPaymentRequest.class, RETURNS_DEEP_STUBS);
        PaymentResponse response = service.finalizeRedirectionPayment(a);

        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());

    }

    @Test
    public void finalizeRedirectionPayment() throws SOAPException, IOException {
        String s = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://sandbox.przelewy24.pl/external/wsdl/service.php\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
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

        InputStream is = new ByteArrayInputStream(s.getBytes());
        SOAPMessage message = MessageFactory.newInstance().createMessage(null, is);

        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(message);
        RedirectionPaymentRequest a = RedirectionPaymentRequest.builder()
                .withRedirectionContext("test")
                .withOrder(TestUtils.createOrder("test"))
                .withAmount(TestUtils.createAmount("EUR"))
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(a);

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
