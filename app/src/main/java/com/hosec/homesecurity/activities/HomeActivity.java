package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.messaging.FCMToken;
import com.hosec.homesecurity.model.DeviceItemInformation;
import com.hosec.homesecurity.model.ListItemInformation;
import com.hosec.homesecurity.model.Notification;
import com.hosec.homesecurity.model.NotificationItemInformation;
import com.hosec.homesecurity.model.Rule;
import com.hosec.homesecurity.model.RuleItemInformation;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends RemoteAPIActivity {

    public static final int REQUEST_CODE_DETAIL = 1;
    public static final String NOTIFICATION_KEY = "IS_NOTIFICATION"; //to handle push-notifications
    private MenuItem mAddButton;
    private MenuItem mRegistrationButton;
    private MenuItem mTurnAlarmOff;
    private GeneralListFragment mGeneralListFragment;
    private BottomNavigationView mBottomNavigation;
    private boolean mAlarmOn;

    //for asynchronous refreshes while running the registration mode
    private Handler mHandler;
    private Runnable mCallback;


    /**
     * listeners which are called when a toolbar menu gets clicked
     */

    /**
     * called if the add button is pressed while displaying rule information
     */
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

    /**
     * called if the add button is pressed while displaying device information
     * TODO: needs to be implemented ;)
     */
    private MenuItem.OnMenuItemClickListener mAddDeviceListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return false;
        }
    };

    /**
     * called if the settings button is pressed
     */
    private MenuItem.OnMenuItemClickListener mSettingsListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);

            startActivity(intent);
            return true;
        }
    };


    /**
     * Prepares the contents of the list view depending on the selected navigation menu option
     * either device or rule or notification data will be displayed.
     * In addition the menu buttons in the toolbar change because there are different options
     * for each type of content
     */
    private boolean prepareContent(MenuItem item) {

        boolean returnValue = false;

        switch (item.getItemId()) {
            case R.id.navigation_devices:

                mGeneralListFragment.setData(DeviceItemInformation
                        .createDeviceItemInformation(mRemoteAlarmSystem.getDeviceList()));
                mAddButton.setVisible(false); //TODO: set visibility to true after camera handling was generalized
                mRegistrationButton.setVisible(true);
                mTurnAlarmOff.setVisible(false);
                mAddButton.setOnMenuItemClickListener(mAddDeviceListener);
                returnValue = true;
                break;

            case R.id.navigation_rules:
                mGeneralListFragment.setData(RuleItemInformation
                        .createRuleItemInformation(mRemoteAlarmSystem.getRuleList()));
                mAddButton.setVisible(true);
                mRegistrationButton.setVisible(false);
                mTurnAlarmOff.setVisible(false);
                mAddButton.setOnMenuItemClickListener(mAddRuleListener);
                returnValue = true;
                break;

            case R.id.navigation_notifications: {

                List<Notification> notifications = mRemoteAlarmSystem.getNotificationList();
                mGeneralListFragment.setData(NotificationItemInformation
                        .createNotificationItemInformation(notifications));

                if (isOneNotificationTriggered(notifications)) {
                    mAlarmOn = true;
                }

                if (mAlarmOn) {
                    mTurnAlarmOff.setVisible(true);
                }
                mRegistrationButton.setVisible(false);
                mAddButton.setVisible(false);
                returnValue = true;
            }

            break;
        }
        return returnValue;
    }

    /**
     * if previous activity was a *-DetailActivity call prepareContent for the respective navigation menu, e.g.
     * if RuleDetailActivity is destroyed and HomeActivity comes into foreground then show the rule list
     */
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

        mAlarmOn = false;

        mGeneralListFragment = (GeneralListFragment) getSupportFragmentManager().findFragmentById(R.id.ruleList);

        mBottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        return prepareContent(item);
                    }
                });

        if (getIntent().getBooleanExtra(NOTIFICATION_KEY, false)) {
            mBottomNavigation.setSelectedItemId(R.id.navigation_notifications);
            mAlarmOn = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                        if (result.success) {
                            mTurnAlarmOff.setVisible(false);
                            mAlarmOn = false;
                            loadData();
                        }
                        Toast.makeText(HomeActivity.this, result.message, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });

        prepareContent(mBottomNavigation.getMenu().findItem(mBottomNavigation.getSelectedItemId()));

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHandler != null) {
            mHandler.removeCallbacks(mCallback);//stop running tasks which were started upon running the registration mode
            mHandler = null;
        }
    }
    //END OF LIFECYCLE METHODS


    /**
     *
     */
    private void loadData() {
        final RemoteAlarmSystem.ResultListener listener = new RemoteAlarmSystem.ResultListener() {
            @Override
            public void onResult(RemoteAlarmSystem.Result result) {
                if (result.success) {
                    prepareContent(mBottomNavigation.getMenu().findItem(mBottomNavigation.getSelectedItemId()));
                } else {
                    //Toast.makeText(HomeActivity.this, result.message, Toast.LENGTH_SHORT).show();
                }
            }
        };
        mGeneralListFragment.setData(new ArrayList<ListItemInformation>());
        mRemoteAlarmSystem.loadData(listener);

    }

    private void registerNewDevices() {
        mRemoteAlarmSystem.startRegistrationMode();
        Toast.makeText(HomeActivity.this, "Wait for registration mode to finish ...", Toast.LENGTH_SHORT).show();
        mHandler = new Handler();
        mCallback = new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        };

        for (int i = 1; i <= 6; ++i) {
            mHandler.postDelayed(mCallback, 5000 * i); //refresh every 5 seconds
            // TODO: let this only affect the list contents if the device menu is actually selected
        }

    }

    private boolean isOneNotificationTriggered(List<Notification> notifications) {

        boolean isTriggered = false;
        for (Notification notification : notifications) {
            if (notification.isTriggered()) {
                isTriggered = true;
                break;
            }
        }
        return isTriggered;

    }


}
