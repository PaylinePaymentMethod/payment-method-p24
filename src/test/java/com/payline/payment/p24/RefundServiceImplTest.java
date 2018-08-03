package com.payline.payment.p24;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.utils.SoapHelper;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RefundServiceImplTest {

    @InjectMocks
    private RefundServiceImpl service;

    @Mock
    private SoapHelper soapHelper;

    @Test
    public void refundRequest() {
        //Mockito.when(SoapHelper.sendSoapMessage(Mockito.any(SOAPMessage.class),Mockito.anyString())).thenReturn(null);
        RefundRequest paymentRequest = TestUtils.createRefundRequest("2");

        RefundResponse response = service.refundRequest(paymentRequest);

        Assert.assertNotNull(response);
//        Assert.assertEquals(RefundResponseSuccess.class, response.getClass());
        Assert.assertEquals(RefundResponseFailure.class, response.getClass());
    }

    @Test
    public void canMultiple() {
        Assert.assertFalse(service.canMultiple());
    }

    @Test
    public void canPartial() {
        Assert.assertFalse(service.canPartial());
    }

    @Test
    public void getRefundResponseFailure() {
        String errorCode = "foo";
        FailureCause failureCause = FailureCause.INVALID_DATA;

        RefundResponseFailure response = service.getRefundResponseFailure(errorCode, failureCause, "0");
        Assert.assertEquals(errorCode, response.getErrorCode());
        Assert.assertEquals(failureCause, response.getFailureCause());
        Assert.assertEquals("0", response.getTransactionId());
    }

}
