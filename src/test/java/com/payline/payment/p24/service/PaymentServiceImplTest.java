package com.payline.payment.p24.service;

import com.payline.payment.p24.bean.TestUtils;
import com.payline.payment.p24.errors.P24ValidationException;
import com.payline.payment.p24.utils.P24HttpClient;
import com.payline.payment.p24.utils.P24Path;
import com.payline.payment.p24.utils.RequestUtils;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.service.PaymentService;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
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
    public void paymentRequest() throws P24ValidationException, IOException {
        PaymentRequest paymentRequest = TestUtils.createCompletePaymentRequest();

        Mockito.when(requestUtils.isSandbox(Mockito.eq(paymentRequest))).thenReturn(true);
        Response.Builder builder = new Response.Builder()
                .addHeader("content-type", "application/json");
        Response respHttp = builder.code(200)
                .protocol(Protocol.HTTP_1_0)
                .request(new Request.Builder().url("https://mvnrepository.com").build())
                .message("")
                .build();
        Mockito.when(httpClient.doPost(Mockito.anyString(), Mockito.eq(P24Path.REGISTER), Mockito.anyMap())).thenReturn(respHttp);

        PaymentResponse response = paymentService.paymentRequest(paymentRequest);
        Assert.assertNotNull(response);

    }

}
