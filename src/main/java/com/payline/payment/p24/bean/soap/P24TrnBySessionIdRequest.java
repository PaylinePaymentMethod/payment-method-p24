package com.payline.payment.p24.bean.soap;

import com.payline.payment.p24.utils.P24Constants;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * Created by Thales on 18/07/2018.
 */
public class P24TrnBySessionIdRequest extends P24SoapRequest {

    /**
     * Default constructor
     */
    public P24TrnBySessionIdRequest() {
    }

    /**
     * @param login
     * @param pass
     * @param sessionId
     */
    public P24TrnBySessionIdRequest(String login,
                                    String pass,
                                    String sessionId) {

        this.mLogin = login;
        this.mPass = pass;
        this.mSessionId = sessionId;

    }

    public P24TrnBySessionIdRequest login(String login) {
        this.mLogin = login;
        return this;
    }

    public P24TrnBySessionIdRequest pass(String pwd) {
        this.mPass = pwd;
        return this;
    }

    public P24TrnBySessionIdRequest sessionId(String sessionId) {
        this.mSessionId = sessionId;
        return this;
    }

    @Override
    public void fillSoapMessageBody() throws SOAPException {

        // Get the body part from envelope
        SOAPBody soapBody = this.mSoapMessage.getSOAPPart().getEnvelope().getBody();

        //*************************
        // Add elements to the body
        //*************************

        // <ser:TrnBySessionId>
        SOAPElement soapElementSer = soapBody.addChildElement(P24Constants.TRN_BY_SESSION_ID, P24Constants.SER);
        soapElementSer.setEncodingStyle(P24Constants.SOAP_ENCODING_URL);

        // <login>
        if (this.mLogin != null) {
            SOAPElement soapElementLogin = soapElementSer.addChildElement(LOGIN);
            soapElementLogin.setAttribute(P24Constants.XSI_TYPE, P24Constants.XSD_STRING);
            soapElementLogin.addTextNode(this.mLogin);
        }

        // <pass>
        if (this.mPass != null) {
            SOAPElement soapElementPass = soapElementSer.addChildElement(PASS);
            soapElementPass.setAttribute(P24Constants.XSI_TYPE, P24Constants.XSD_STRING);
            soapElementPass.addTextNode(this.mPass);
        }

        // <sessionId>
        if (this.mSessionId != null) {
            SOAPElement soapElementPass = soapElementSer.addChildElement(SESSION_ID);
            soapElementPass.setAttribute(P24Constants.XSI_TYPE, P24Constants.XSD_STRING);
            soapElementPass.addTextNode(this.mSessionId);
        }

    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("class P24TrnBySessionIdRequest {\n");
        sb.append("    login: ").append(toIndentedString(mLogin)).append("\n");
        sb.append("    pass: ").append(toIndentedString(mPass)).append("\n");
        sb.append("    sessionId: ").append(toIndentedString(mSessionId)).append("\n");
        sb.append("}");

        return sb.toString();

    }

}