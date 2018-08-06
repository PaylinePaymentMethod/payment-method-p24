package com.payline.payment.p24.bean.soap;

import com.payline.payment.p24.utils.P24Constants;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;


/**
 * Created by Thales on 18/07/2018.
 */
public class P24TrnRefundRequest extends P24SoapRequest {

    private Integer mBatch;
    private Integer mOrderId;
    private Integer mAmount;

    /**
     * Default constructor
     */
    public P24TrnRefundRequest() {
    }

    /**
     * Constructor
     *
     * @param login
     * @param pass
     * @param batch
     * @param orderId
     * @param sessionId
     * @param amount
     */
    public P24TrnRefundRequest(String login,
                               String pass,
                               Integer batch,
                               Integer orderId,
                               String sessionId,
                               Integer amount) {

        this.mLogin = login;
        this.mPass = pass;
        this.mBatch = batch;
        this.mOrderId = orderId;
        this.mSessionId = sessionId;
        this.mAmount = amount;

    }

    public P24TrnRefundRequest login(String login) {
        this.mLogin = login;
        return this;
    }

    public P24TrnRefundRequest pass(String pwd) {
        this.mPass = pwd;
        return this;
    }

    public P24TrnRefundRequest batch(Integer batch) {
        this.mBatch = batch;
        return this;
    }

    public P24TrnRefundRequest orderId(Integer orderId) {
        this.mOrderId = orderId;
        return this;
    }

    public P24TrnRefundRequest sessionId(String sessionId) {
        this.mSessionId = sessionId;
        return this;
    }

    public P24TrnRefundRequest amount(Integer amount) {
        this.mAmount = amount;
        return this;
    }

    /**
     * Fill the SOAP message's body
     *
     * @throws SOAPException
     */
    @Override
    public void fillSoapMessageBody() throws SOAPException {

        // Get the body part from envelope
        SOAPBody soapBody = this.mSoapMessage.getSOAPPart().getEnvelope().getBody();

        //*************************
        // Add elements to the body
        //*************************

        // <ser:TrnRefund>
        SOAPElement soapElementSer = soapBody.addChildElement(P24Constants.TRN_REFUND, P24Constants.SER);
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

        // <batch>
        if (this.mBatch != null) {
            SOAPElement soapElementBatch = soapElementSer.addChildElement(P24Constants.BATCH);
            soapElementBatch.setAttribute(P24Constants.XSI_TYPE, P24Constants.XSD_INT);
            soapElementBatch.addTextNode(String.valueOf(this.mBatch));
        }

        // <list>
        SOAPElement soapElementList = soapElementSer.addChildElement(P24Constants.LIST);
        soapElementList.setAttribute(P24Constants.XSI_TYPE, P24Constants.SER_ARRAY_OF_REFUND);
        soapElementList.setAttribute(P24Constants.SOAP_ENC_ARRAY_TYPE, P24Constants.SER_REFUND);

        // <list>
        //   <Refund>
        SOAPElement soapElementRefund = soapElementList.addChildElement(P24Constants.REFUND);

        // <list>
        //    <Refund>
        //       <orderId>
        SOAPElement soapElementOrderId = soapElementRefund.addChildElement(P24Constants.SOAP_ORDER_ID);
        soapElementOrderId.addTextNode(String.valueOf(this.mOrderId));

        // <list>
        //    <Refund>
        //       <sessionId>
        SOAPElement soapElementSessionId = soapElementRefund.addChildElement(SESSION_ID);
        soapElementSessionId.addTextNode(this.mSessionId);

        // <list>
        //    <Refund>
        //       <amount>
        SOAPElement soapElementAmount = soapElementRefund.addChildElement(P24Constants.AMOUNT);
        soapElementAmount.addTextNode(String.valueOf(this.mAmount));

    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("class P24TrnRefundRequest {\n");
        sb.append("    login: ").append(toIndentedString(mLogin)).append("\n");
        sb.append("    pass: ").append(toIndentedString(mPass)).append("\n");
        sb.append("    batch: ").append(toIndentedString(mBatch)).append("\n");
        sb.append("    orderId: ").append(toIndentedString(mOrderId)).append("\n");
        sb.append("    sessionId: ").append(toIndentedString(mSessionId)).append("\n");
        sb.append("    amount: ").append(toIndentedString(mAmount)).append("\n");
        sb.append("}");

        return sb.toString();

    }

}
