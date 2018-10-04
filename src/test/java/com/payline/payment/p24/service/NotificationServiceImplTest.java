package com.payline.payment.p24.service;

import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.service.NotificationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.HashMap;

public class NotificationServiceImplTest {

    @InjectMocks
    private NotificationService notificationService = new NotificationServiceImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void parse_null() {
        NotificationResponse response = notificationService.parse(null);
        Assert.assertNotNull(response);
        Assert.assertEquals(IgnoreNotificationResponse.class, response.getClass());
    }

    @Test
    public void parse_notNull() {
        NotificationRequest request = NotificationRequest.NotificationRequestBuilder
                .aNotificationRequest()
                .withHttpMethod("GET")
                .withPathInfo("")
                .withContent(new InputStream() {
                    @Override
                    public int read() {
                        return 0;
                    }
                })
                .withHeaderInfos(new HashMap<>())
                .build();
        NotificationResponse response = notificationService.parse(request);
        Assert.assertNotNull(response);
        Assert.assertEquals(IgnoreNotificationResponse.class, response.getClass());
    }

}
