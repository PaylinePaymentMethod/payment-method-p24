package com.payline.payment.p24.bean.soap;


import com.payline.payment.p24.utils.SoapHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Created by Thales on 18/07/2018.
 */
public abstract class P24SoapRequest implements SoapRequest {


    private static final Logger LOG = LogManager.getLogger(P24SoapRequest.class);

    private SoapHelper soapHelper;

    protected static final String LOGIN = "login";
    protected static final String PASS = "pass";
    protected static final String SESSION_ID = "sessionId";

    protected String mLogin;
    protected String mPass;
    protected String mSessionId;

    protected SOAPMessage mSoapMessage;

    public P24SoapRequest() {
        soapHelper = new SoapHelper();
    }

    /**
     * Build the SOAP message
     *
     * @return
     */
    @Override
    public SOAPMessage buildSoapMessage(boolean isSandbox) {

        this.mSoapMessage = null;

        // Initialize the SOAP message with filled envelope
        this.mSoapMessage = soapHelper.buildBaseMsg(isSandbox);

        try {

            if (this.mSoapMessage != null) {
                this.fillSoapMessageBody();
            }

        } catch (SOAPException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        LOG.debug(getSOAPMessageAsString(mSoapMessage));
        return this.mSoapMessage;

    }


    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    protected String toIndentedString(Object o) {

        if (Objects.isNull(o)) {
            return "null";
        }

        return o.toString().replace("\n", "\n    ");

    }

    public String getSOAPMessageAsString(SOAPMessage soapMessage) {
        try {

            TransformerFactory tff = TransformerFactory.newInstance();
            tff.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer tf = tff.newTransformer();

            // Set formatting

            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                    "2");

            Source sc = soapMessage.getSOAPPart().getContent();

            ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(streamOut);
            tf.transform(sc, result);

            return streamOut.toString();
        } catch (Exception e) {
            LOG.warn("Exception in getSOAPMessageAsString {}", e.getMessage());
            return "";
        }


    }
}