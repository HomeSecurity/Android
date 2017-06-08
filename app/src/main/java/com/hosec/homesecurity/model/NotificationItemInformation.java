package com.hosec.homesecurity.model;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D062572 on 07.06.2017.
 */

public class NotificationItemInformation extends ListItemInformation {

    private Notification mNotification;

    public NotificationItemInformation(Notification notification) {
        super();
        mNotification = notification;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public int getImageId() {
        return 0;
    }


    @Override
    public void onClick(View v) {

    }

    public static List<NotificationItemInformation> createNotificationItemInformation(List<Notification> notifications) {
        List<NotificationItemInformation> newList = new ArrayList<>(notifications.size());
        for (Notification n : notifications) {
            newList.add(new NotificationItemInformation(n));
        }
        return newList;
    }
}
