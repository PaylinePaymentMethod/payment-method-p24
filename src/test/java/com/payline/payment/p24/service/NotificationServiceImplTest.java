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

import java.io.ByteArrayInputStream;
import java.util.Collections;

public class NotificationServiceImplTest {

    @InjectMocks
    private NotificationService notificationService = new NotificationServiceImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void parse_null() {
        NotificationResponse reponse = notificationService.parse(null);
        Assert.assertNotNull(reponse);
        Assert.assertEquals(IgnoreNotificationResponse.class, reponse.getClass());
    }

    @Test
    public void parse_notNull() {
        final NotificationRequest notificationRequest = NotificationRequest.NotificationRequestBuilder.aNotificationRequest()
                .withHttpMethod("POST")
                .withPathInfo("/path")
                .withContent(new ByteArrayInputStream("".getBytes()))
                .withHeaderInfos(Collections.emptyMap())
                .build();
        NotificationResponse reponse = notificationService.parse(notificationRequest);
        Assert.assertNotNull(reponse);
        Assert.assertEquals(IgnoreNotificationResponse.class, reponse.getClass());
    }

}
