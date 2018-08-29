package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.P24Constants;
import com.payline.payment.p24.utils.SoapHelper;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RefundServiceImplTest {
    private String soapResponseOK = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://sandbox.przelewy24.pl/external/wsdl/service.php\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "  <SOAP-ENV:Body>" +
            "     <ns1:TrnRefundResponse>" +
            "        <return xsi:type=\"ns1:TrnRefundResult\">" +
            "           <result SOAP-ENC:arrayType=\"ns1:SingleRefund[1]\" xsi:type=\"ns1:ArrayOfSingleRefund\">" +
            "              <item xsi:type=\"ns1:SingleRefund\">" +
            "                 <orderId xsi:type=\"xsd:int\">890857</orderId>" +
            "                 <orderIdFull xsi:type=\"xsd:int\">890857</orderIdFull>" +
            "                 <sessionId xsi:type=\"xsd:string\">PL_TEST_2018-08-03_09-34-24</sessionId>" +
            "                 <status xsi:type=\"xsd:boolean\">false</status>" +
            "              </item>" +
            "           </result>" +
            "           <error xsi:type=\"ns1:GeneralError\">" +
            "              <errorCode xsi:type=\"xsd:int\">0</errorCode>" +
            "              <errorMessage xsi:type=\"xsd:string\">NoError</errorMessage>" +
            "           </error>" +
            "        </return>" +
            "     </ns1:TrnRefundResponse>" +
            "  </SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    InputStream isOK = new ByteArrayInputStream(soapResponseOK.getBytes());
    SOAPMessage messageOK = MessageFactory.newInstance().createMessage(null, isOK);

    private String soapResponseKO = "<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://sandbox.przelewy24.pl/external/wsdl/service.php\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "  <SOAP-ENV:Body>" +
            "     <ns1:TrnRefundResponse>" +
            "        <return xsi:type=\"ns1:TrnRefundResult\">" +
            "           <result SOAP-ENC:arrayType=\"ns1:SingleRefund[1]\" xsi:type=\"ns1:ArrayOfSingleRefund\">" +
            "              <item xsi:type=\"ns1:SingleRefund\">" +
            "                 <orderId xsi:type=\"xsd:int\">890857</orderId>" +
            "                 <orderIdFull xsi:type=\"xsd:int\">890857</orderIdFull>" +
            "                 <sessionId xsi:type=\"xsd:string\">PL_TEST_2018-08-03_09-34-24</sessionId>" +
            "                 <status xsi:type=\"xsd:boolean\">false</status>" +
            "              </item>" +
            "           </result>" +
            "           <error xsi:type=\"ns1:GeneralError\">" +
            "              <errorCode xsi:type=\"xsd:int\">10000</errorCode>" +
            "              <errorMessage xsi:type=\"xsd:string\">foo</errorMessage>" +
            "           </error>" +
            "        </return>" +
            "     </ns1:TrnRefundResponse>" +
            "  </SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

    InputStream isKO = new ByteArrayInputStream(soapResponseKO.getBytes());
    SOAPMessage messageKO = MessageFactory.newInstance().createMessage(null, isKO);

    @InjectMocks
    private RefundServiceImpl service;

    @Mock
    private SoapHelper soapHelper;

    public RefundServiceImplTest() throws IOException, SOAPException {
    }

    @Test
    public void refundRequestOK() {
        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(messageOK);
        when(soapHelper.getErrorCodeFromSoapResponseMessage(any(SOAPMessage.class))).thenReturn("0");
        when(soapHelper.getTagContentFromSoapResponseMessage(any(SOAPMessage.class), eq(P24Constants.ORDER_ID))).thenReturn("12");
        RefundRequest paymentRequest = TestUtils.createRefundRequest("dumbId");

        RefundResponse response = service.refundRequest(paymentRequest);

        Assert.assertNotNull(response);
        Assert.assertEquals(RefundResponseSuccess.class, response.getClass());
    }

    @Test
    public void refundRequestWithSOAPError() {

        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(null);
        RefundRequest paymentRequest = TestUtils.createRefundRequest("2");

        RefundResponse response = service.refundRequest(paymentRequest);

        Assert.assertNotNull(response);
        Assert.assertEquals(RefundResponseFailure.class, response.getClass());
    }


    @Test
    public void refundRequestKO() {

        when(soapHelper.sendSoapMessage(any(SOAPMessage.class), anyString())).thenReturn(messageKO);
        RefundRequest paymentRequest = TestUtils.createRefundRequest("2");

        RefundResponse response = service.refundRequest(paymentRequest);

        Assert.assertNotNull(response);
        Assert.assertEquals(RefundResponseFailure.class, response.getClass());
    }


    @Test
    public void canMultiple() {
        Assert.assertNotNull(service.canMultiple());
    }

    @Test
    public void canPartial() {
        Assert.assertNotNull(service.canPartial());
    }

}
