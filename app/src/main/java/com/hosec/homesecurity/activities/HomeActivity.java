package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.messaging.MyFirebaseInstanceIDService;
import com.hosec.homesecurity.model.Credentials;
import com.hosec.homesecurity.model.DeviceItemInformation;
import com.hosec.homesecurity.model.ListItemInformation;
import com.hosec.homesecurity.model.Notification;
import com.hosec.homesecurity.model.NotificationItemInformation;
import com.hosec.homesecurity.model.Rule;
import com.hosec.homesecurity.model.RuleItemInformation;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;
import com.hosec.homesecurity.remote.TestRemoteAlarmSystem;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_DETAIL = 1;
    public static final String NOTIFICATION_KEY = "IS_NOTIFICATION";
    private MenuItem mAddButton;
    private MenuItem mRegistrationButton;
    private MenuItem mTurnAlarmOff;
    private GeneralListFragment mGeneralListFragment;
    private BottomNavigationView mBottomNavigation;
    private RemoteAlarmSystem mRemoteAlarmSystem;
    private boolean mAlarmOn;
    private boolean mLoggedIn;




    private MenuItem.OnMenuItemClickListener mAddRuleListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent(HomeActivity.this, RuleDetailActivity.class);
            intent.putExtra(RuleDetailActivity.RULE_KEY, new Rule(false));
            intent.putExtra(RuleDetailActivity.NEW_KEY, true);
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mAddDeviceListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return false;
        }
    };

    private MenuItem.OnMenuItemClickListener mSettingsListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);

            startActivity(intent);
            return true;
        }
    };


    private boolean prepareContent(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigation_devices:

                mGeneralListFragment.setData(DeviceItemInformation
                        .createDeviceItemInformation(mRemoteAlarmSystem.getDeviceList()));
                mAddButton.setVisible(true);
                mRegistrationButton.setVisible(true);
                mTurnAlarmOff.setVisible(false);
                mAddButton.setOnMenuItemClickListener(mAddDeviceListener);
                return true;

            case R.id.navigation_rules:
                mGeneralListFragment.setData(RuleItemInformation
                        .createRuleItemInformation(mRemoteAlarmSystem.getRuleList()));
                mAddButton.setVisible(true);
                mRegistrationButton.setVisible(false);
                mTurnAlarmOff.setVisible(false);
                mAddButton.setOnMenuItemClickListener(mAddRuleListener);
                return true;

            case R.id.navigation_notifications: {

                List<Notification> notifications = mRemoteAlarmSystem.getNotificationList();
                mGeneralListFragment.setData(NotificationItemInformation
                        .createNotificationItemInformation(notifications));

                if (!notifications.isEmpty() && notifications.get(0).isTriggered()) {
                    mAlarmOn = true;
                }

                if (mAlarmOn) {
                    mTurnAlarmOff.setVisible(true);
                }
                mRegistrationButton.setVisible(false);
                mAddButton.setVisible(false);
                return true;
            }
        }
        return false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        prepareContent(mBottomNavigation.getMenu().findItem(mBottomNavigation.getSelectedItemId()));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolBar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolBar);

        Credentials creds = Credentials.getStoredCredentials(this);

        mAlarmOn = false;
        mLoggedIn = false;

        mRemoteAlarmSystem = RemoteAlarmSystem.getInstance(this);
        mRemoteAlarmSystem.logOnToSystem(creds.getHostname(), creds.getUsername(), creds.getPassword(),
                new RemoteAlarmSystem.ResultListener() {
                    @Override
                    public void onResult(RemoteAlarmSystem.Result result) {
                        Toast.makeText(HomeActivity.this, result.message,Toast.LENGTH_SHORT).show();
                        if(result.success){
                            mLoggedIn = true;
                            if(MyFirebaseInstanceIDService.msToken != null){
                                RemoteAlarmSystem.getInstance(HomeActivity.this)
                                        .setMessagingToken(MyFirebaseInstanceIDService.msToken);
                                MyFirebaseInstanceIDService.msToken = null;
                            }

                            loadData();
                        }else{
                            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                            intent.putExtra(LoginActivity.UNABLE_TO_CONNECT_WITH_KNOWN_CREDS_KEY,true);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

        mGeneralListFragment = (GeneralListFragment) getSupportFragmentManager().findFragmentById(R.id.ruleList);

        mBottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);

        if(getIntent().getBooleanExtra(NOTIFICATION_KEY,false)){
            mBottomNavigation.setSelectedItemId(R.id.navigation_notifications);
            mAlarmOn = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mLoggedIn){
            loadData();
        }

        mBottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {

                        return prepareContent(item);

                    }

                });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem settings = ((Toolbar) findViewById(R.id.home_toolbar)).getMenu().findItem(R.id.action_settings);
        settings.setOnMenuItemClickListener(mSettingsListener);

        mAddButton = ((Toolbar) findViewById(R.id.home_toolbar)).getMenu().findItem(R.id.action_add);

        mRegistrationButton = ((Toolbar) findViewById(R.id.home_toolbar)).getMenu().findItem(R.id.registration_mode);
        mRegistrationButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                registerNewDevices();
                return true;
            }
        });

        mTurnAlarmOff = ((Toolbar) findViewById(R.id.home_toolbar)).getMenu().findItem(R.id.turn_alarm_off);
        mTurnAlarmOff.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mRemoteAlarmSystem.resetAlarm(new RemoteAlarmSystem.ResultListener() {
                    @Override
                    public void onResult(RemoteAlarmSystem.Result result) {
                        if(result.success) {
                            mTurnAlarmOff.setVisible(false);
                            mAlarmOn = false;
                            loadData();
                        }
                        Toast.makeText(HomeActivity.this, result.message,Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });

        prepareContent(mBottomNavigation.getMenu().findItem(mBottomNavigation.getSelectedItemId()));

        return true;
    }

    private void loadData(){
        final RemoteAlarmSystem.ResultListener listener = new RemoteAlarmSystem.ResultListener() {
            @Override
            public void onResult(RemoteAlarmSystem.Result result) {
                if(result.success){
                    prepareContent(mBottomNavigation.getMenu().findItem(mBottomNavigation.getSelectedItemId()));
                }else{
                    Toast.makeText(HomeActivity.this, result.message,Toast.LENGTH_SHORT).show();
                }
            }
        };
        mGeneralListFragment.setData(new ArrayList<ListItemInformation>());
        mRemoteAlarmSystem.loadData(listener);

    }

    private void registerNewDevices(){
        mRemoteAlarmSystem.startRegistrationMode();
        Toast.makeText(HomeActivity.this, "Wait for registration mode to finish ...", Toast.LENGTH_SHORT).show();
        final Handler handler = new Handler();
        Runnable r = new Runnable(){
            @Override
            public void run(){
                loadData();
            }
        };

        for(int i = 1; i <= 6; ++i){
            handler.postDelayed(r, 5000*i);
        }


    }




}
