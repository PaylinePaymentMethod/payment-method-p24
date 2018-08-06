package com.payline.payment.p24.service;

import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.service.NotificationService;

public class NotificationServiceImpl implements NotificationService {

    @Override
    public NotificationResponse parse(NotificationRequest notificationRequest) {
        return new IgnoreNotificationResponse();
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        // Non utilisé par P24.

    }
}
