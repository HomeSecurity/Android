package com.hosec.homesecurity.model;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.activities.HomeActivity;
import com.hosec.homesecurity.activities.NotificationDetailActivity;

import java.text.SimpleDateFormat;
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
        return "Rule " + mNotification.getRule().getName() + " was triggered";
    }

    @Override
    public String getSubtitle() {

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd.MM.yyyy");


        return "Triggered at " + sdf.format(mNotification.getDate());
    }

    @Override
    public int getImageId() {
        return mNotification.isTriggered() ? R.mipmap.ic_alarm : R.mipmap.ic_old_alarm;
    }


    @Override
    public void onClick(View v) {
        Activity activity = (Activity) v.getContext();
        Intent intent  = new Intent(activity, NotificationDetailActivity.class);
        intent.putExtra(NotificationDetailActivity.NOTIFICATION_KEY, mNotification);
        activity.startActivityForResult(intent, HomeActivity.REQUEST_CODE_DETAIL);
    }

    public static List<NotificationItemInformation> createNotificationItemInformation(List<Notification> notifications) {
        List<NotificationItemInformation> newList = new ArrayList<>(notifications.size());
        for (Notification n : notifications) {
            newList.add(new NotificationItemInformation(n));
        }
        return newList;
    }
}
