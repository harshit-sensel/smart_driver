
package com.sensel.driverapp.response.notification;

import com.google.gson.annotations.Expose;

import java.util.List;


public class NotificationResponse {

    @Expose
    private List<Notification> notifications;

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

}
