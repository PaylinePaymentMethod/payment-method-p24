package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.P24HttpClient;
import com.payline.payment.p24.utils.P24Path;
import com.payline.payment.p24.utils.RequestUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.PaymentResponseFailure;
import com.payline.pmapi.service.PaymentService;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

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
    public void paymentRequest_200_unparsable() throws P24ValidationException, IOException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Response.Builder builder = new Response.Builder()
                .addHeader("content-type", "application/json");
        Response respHttp = builder.code(200)
                .protocol(Protocol.HTTP_1_0)
                .request(new Request.Builder().url("https://mvnrepository.com").build())
                .body(ResponseBody.create(MediaType.parse("application/txt"), ""))
                .message("")
                .build();
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(respHttp);

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) response).getFailureCause());

    }

    @Test
    public void paymentRequest_9000() throws P24ValidationException, IOException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Response.Builder builder = new Response.Builder()
                .addHeader("content-type", "application/json");
        Response respHttp = builder.code(9000)
                .protocol(Protocol.HTTP_1_0)
                .request(new Request.Builder().url("https://mvnrepository.com").build())
                .body(ResponseBody.create(MediaType.parse("application/txt"), ""))
                .message("")
                .build();
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(respHttp);

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure) response).getFailureCause());

    }


    @Test
    public void paymentRequest_success() throws P24ValidationException, IOException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Response.Builder builder = new Response.Builder()
                .addHeader("content-type", "application/json");
        Response respHttp = builder.code(200)
                .protocol(Protocol.HTTP_1_0)
                .request(new Request.Builder().url("https://mvnrepository.com").build())
                .body(ResponseBody.create(MediaType.parse("application/txt"), "error=0&token=hfgjfjyj"))
                .message("")
                .build();
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(respHttp);

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(com.payline.pmapi.bean.payment.response.PaymentResponseRedirect.class, response.getClass());

    }


    @Test
    public void paymentRequest_IOException() throws P24ValidationException, IOException {
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
    public void paymentRequest_P24ValidationException() throws P24ValidationException, IOException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenThrow(new P24ValidationException(""));

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);
        Assert.assertEquals(PaymentResponseFailure.class, response.getClass());
        Assert.assertEquals(FailureCause.INVALID_DATA, ((PaymentResponseFailure) response).getFailureCause());

    }


}
