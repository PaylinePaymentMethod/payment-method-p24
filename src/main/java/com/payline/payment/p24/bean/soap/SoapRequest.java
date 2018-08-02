package com.payline.payment.p24.bean.soap;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Created by Thales on 18/07/2018.
 */
public interface SoapRequest {

    /**
     * Build the SOAP message from the request's attributes
     *
     * @param isSandbox
     * @return SOAPMessage : the SOAP message
     */
    SOAPMessage buildSoapMessage(boolean isSandbox);

    /**
     * Fill the SOAP messag's body with the request's attributes
     */
    void fillSoapMessageBody() throws SOAPException;

}
