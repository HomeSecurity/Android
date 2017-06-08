package com.hosec.homesecurity.model;

import android.content.Intent;

import java.util.Date;

/**
 * Created by D062572 on 29.05.2017.
 */

public abstract class Notification{

    private Date mDate;
    private String mText;

    public Notification(Date date, String text) {
        this.mDate = date;
        this.mText = text;
    }

    public Date getDate() {
        return mDate;
    }

    public String getText() {
        return mText;
    }

    public abstract Intent getIntentToDetailActivity();

}
