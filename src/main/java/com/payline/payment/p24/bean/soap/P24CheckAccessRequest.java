package com.payline.payment.p24.bean.soap;

import com.payline.payment.p24.utils.P24Constants;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

/**
 * Created by Thales on 19/07/2018.
 */
public class P24CheckAccessRequest extends P24SoapRequest {

    /**
     * Default constructor
     */
    public P24CheckAccessRequest() {
    }

    /**
     * @param login login
     * @param pass  pass
     */
    public P24CheckAccessRequest(String login,
                                 String pass) {

        this.mLogin = login;
        this.mPass = pass;

    }

    public P24CheckAccessRequest login(String login) {
        this.mLogin = login;
        return this;
    }

    public P24CheckAccessRequest pass(String pwd) {
        this.mPass = pwd;
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
        SOAPElement soapElementSer = soapBody.addChildElement(P24Constants.TEST_ACCESS, P24Constants.SER);
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

    }

    @Override
    public String toString() {

        return "class P24TestAccessRequest {\n    login: " + toIndentedString(mLogin) + "\n    pass: " + toIndentedString(mPass) + "\n}";

    }

}