package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.P24HttpClient;
import com.payline.payment.p24.utils.P24Path;
import com.payline.payment.p24.utils.RequestUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.service.PaymentService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URISyntaxException;

public class PaymentServiceImplTest {

    @InjectMocks
    private PaymentService paymentService = new PaymentServiceImpl();

    @Mock
    private P24HttpClient httpClient;

    @Mock
    private RequestUtils requestUtils;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void paymentRequest_200_unparsable() throws P24ValidationException, IOException, URISyntaxException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(TestUtils.createResponse(200, ""));

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) response).getFailureCause());
    }

    @Test
    public void paymentRequest_9000() throws P24ValidationException, IOException, URISyntaxException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(TestUtils.createResponse(9000, ""));

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure) response).getFailureCause());
    }


    @Test
    public void paymentRequest_success() throws P24ValidationException, IOException, URISyntaxException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(TestUtils.createResponse(200, "error=0&token=dummytoken"));

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseRedirect.class, response.getClass());
    }


    @Test
    public void paymentRequest_IOException() throws P24ValidationException, IOException, URISyntaxException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap()))
                .thenThrow(new IOException());

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.INTERNAL_ERROR, ((PaymentResponseFailure) response).getFailureCause());
    }


    @Test
    public void paymentRequest_P24ValidationException() throws P24ValidationException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenThrow(new P24ValidationException(""));

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) response).getFailureCause());
    }

}
