package com.payline.payment.p24.utils;

import javax.xml.soap.*;

/**
 * Created by Thales on 18/07/2018.
 */
public class SoapHelper {

    /**
     * Build a SOAPMessage with filled envelope
     *
     * @return SOAPMessage : the SOAPMessage
     */
    public static SOAPMessage buildBaseMsg() {

        SOAPMessage soapMessage = null;

        try {

            // Initialize the SOAP message
            soapMessage = MessageFactory.newInstance().createMessage();

            // Fill the SOAP message's envelope
            soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration(P24Constants.SOAP_ENC, P24Constants.SOAP_ENCODING_URL);
            soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration(P24Constants.XSI, P24Constants.XSI_URL);
            soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration(P24Constants.XSD, P24Constants.XSD_URL);
            soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration(P24Constants.SER, P24Constants.SER_URL);

        } catch (SOAPException e) {
            e.printStackTrace();
        }

        return soapMessage;

    }

    /**
     * Send a SOAP message to the specified URL and get back the SOAP response message
     *
     * @param soapMessage : the SOAP message to send
     * @param endpointUrl : the web service endpoint URL
     * @return SOAPMessage : the SOAP response message
     */
    public static SOAPMessage sendSoapMessage(SOAPMessage soapMessage, String endpointUrl) {

        // Don't send message if :
        // - SOAP message is null
        // - URL is null or empty
        if (soapMessage == null || endpointUrl == null || (endpointUrl != null && endpointUrl.isEmpty())) {
            return null;
        }

        SOAPConnection soapConnection = null;
        SOAPMessage soapMessageResponse = null;

        try {

            // Create SOAP Connection
            soapConnection = SOAPConnectionFactory.newInstance().createConnection();

            // Send the SOAP message to the URL
            soapMessageResponse = soapConnection.call(soapMessage, endpointUrl);


        } catch (SOAPException e) {
            e.printStackTrace();
        } finally {
            if (soapConnection != null) {
                try {
                    soapConnection.close();
                } catch (SOAPException e) {
                    e.printStackTrace();
                }
            }

        }

        return soapMessageResponse;

    }

    /**
     * Get the error code value from SOAP response message
     *
     * @param soapResponseMessage
     * @return String : the error code value
     */
    public static String getErrorCodeFromSoapResponseMessage(SOAPMessage soapResponseMessage) {
        return getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.SOAP_TAG__ERROR_CODE);
    }

    /**
     * Get the error message value from SOAP response message
     *
     * @param soapResponseMessage
     * @return String : the error code value
     */
    public static String getErrorMessageFromSoapResponseMessage(SOAPMessage soapResponseMessage) {
        return getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.SOAP_TAG__ERROR_MESSAGE);
    }

    /**
     * Get the specified tag value from SOAP response message
     *
     * @param soapResponseMessage
     * @return String : the tag content value
     */
    public static String getTagContentFromSoapResponseMessage(SOAPMessage soapResponseMessage, String tag) {

        String tagContent = "";

        try {

            // Get the SOAP message's body
            SOAPBody soapBody = soapResponseMessage.getSOAPBody();

            // Retrieve the errorCode tag and get its content
            if (soapBody != null
                    && soapBody.getElementsByTagName(tag) != null
                    && soapBody.getElementsByTagName(tag).item(0) != null) {

                tagContent = soapBody.getElementsByTagName(tag).item(0).getTextContent();

            }

        } catch (SOAPException e) {
            e.printStackTrace();
        }

        return tagContent;

    }

}