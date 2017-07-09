package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Notification;

import java.io.InputStream;
import java.text.SimpleDateFormat;


/**
 * Displays all details of a notification object
 */
public class NotificationDetailActivity extends AppCompatActivity {

    public static final String NOTIFICATION_KEY = "NOTIFICATION";

    private Notification mNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mNotification = (Notification) intent.getSerializableExtra(NOTIFICATION_KEY);

        fillWithDeviceData();
    }


    private void fillWithDeviceData() {

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm dd.MM.yyyy");

        ((TextView) findViewById(R.id.tvRuleValue)).setText(mNotification.getRule().getName());
        ((TextView) findViewById(R.id.tvStateValue)).setText(mNotification.isTriggered() ? "Triggered" : "Not triggered");
        ((TextView) findViewById(R.id.tvDateValue)).setText(sdf.format(mNotification.getDate()));
        ((ImageView) findViewById(R.id.ivTriggeredImage)).setImageBitmap(mNotification.getBitmap());

    }
}
