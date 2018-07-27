package com.payline.payment.p24.bean.soap;


import com.payline.payment.p24.utils.SoapHelper;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Created by Thales on 18/07/2018.
 */
public abstract class P24SoapRequest implements SoapRequest {

    protected static final String LOGIN = "login";
    protected static final String PASS = "pass";
    protected static final String SESSION_ID = "sessionId";

    protected String mLogin;
    protected String mPass;
    protected String mSessionId;

    protected SOAPMessage mSoapMessage;

    /**
     * Build the SOAP message
     *
     * @return
     */
    @Override
    public SOAPMessage buildSoapMessage() {

        this.mSoapMessage = null;

        // Initialize the SOAP message with filled envelope
        this.mSoapMessage = SoapHelper.buildBaseMsg();

        try {

            if (this.mSoapMessage != null) {
                this.fillSoapMessageBody();
            }

        } catch (SOAPException e) {
            e.printStackTrace();
        }

        return this.mSoapMessage;

    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    protected String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }

        return o.toString().replace("\n", "\n    ");

    }

}