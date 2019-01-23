package com.payline.payment.p24.utils;

import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.soap.*;

/**
 * Created by Thales on 18/07/2018.
 */
public class SoapHelper {

    public SoapHelper() {
        // ras.
    }

    private static final Logger LOG = LogManager.getLogger(SoapHelper.class);

    /**
     * Build a SOAPMessage with filled envelope
     *
     * @return SOAPMessage : the SOAPMessage
     */
    public SOAPMessage buildBaseMsg(boolean isSandbox) {


        SOAPMessage soapMessage = null;

        try {

            // Initialize the SOAP message
            soapMessage = MessageFactory.newInstance().createMessage();

            SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
            // Fill the SOAP message's envelope
            envelope.addNamespaceDeclaration(P24Constants.SOAP_ENC, P24Constants.SOAP_ENCODING_URL);
            envelope.addNamespaceDeclaration(P24Constants.XSI, P24Constants.XSI_URL);
            envelope.addNamespaceDeclaration(P24Constants.XSD, P24Constants.XSD_URL);
            envelope.addNamespaceDeclaration(P24Constants.SER, P24Url.SOAP_SER.getUrl(isSandbox));

        } catch (SOAPException e) {
            LOG.error(e.getLocalizedMessage(), e);
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
    public SOAPMessage sendSoapMessage(SOAPMessage soapMessage, String endpointUrl) {

        // Don't send message if :
        // - SOAP message is null
        // - URL is null or empty
        if (soapMessage == null || endpointUrl == null || endpointUrl.isEmpty()) {
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
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (soapConnection != null) {
                try {
                    soapConnection.close();
                } catch (SOAPException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
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
    public String getErrorCodeFromSoapResponseMessage(SOAPMessage soapResponseMessage) {
        return getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.SOAP_TAG_ERROR_CODE);
    }

    /**
     * Get the error message value from SOAP response message
     *
     * @param soapResponseMessage
     * @return String : the error code value
     */
    public String getErrorMessageFromSoapResponseMessage(SOAPMessage soapResponseMessage) {
        return getTagContentFromSoapResponseMessage(soapResponseMessage, P24Constants.SOAP_TAG_ERROR_MESSAGE);
    }

    /**
     * Get the specified tag value from SOAP response message
     *
     * @param soapResponseMessage
     * @return String : the tag content value
     */
    public String getTagContentFromSoapResponseMessage(SOAPMessage soapResponseMessage, String tag) {

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
            LOG.error(e.getLocalizedMessage(), e);
        }

        return tagContent;

    }

}