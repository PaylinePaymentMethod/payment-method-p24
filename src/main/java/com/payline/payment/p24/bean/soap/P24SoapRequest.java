package com.payline.payment.p24.bean.soap;


import com.payline.payment.p24.utils.SoapHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
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

}