package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.DeviceItemInformation;
import com.hosec.homesecurity.model.NotificationItemInformation;
import com.hosec.homesecurity.model.Rule;
import com.hosec.homesecurity.model.RuleItemInformation;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;


public class HomeActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_DETAIL = 1;
    private MenuItem mAddButton;
    private GeneralListFragment mGeneralListFragment;
    private BottomNavigationView mBottomNavigation;

    private MenuItem.OnMenuItemClickListener mAddRuleListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent(HomeActivity.this,RuleDetailActivity.class);
            intent.putExtra(RuleDetailActivity.RULE_KEY,new Rule(false));
            intent.putExtra(RuleDetailActivity.NEW_KEY,true);
            startActivityForResult(intent,REQUEST_CODE_DETAIL);
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
            Intent intent = new Intent(HomeActivity.this,SettingsActivity.class);

            startActivity(intent);
            return true;
        }
    };


    private boolean prepareContent(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigation_devices:
                mGeneralListFragment.setData(DeviceItemInformation
                        .createDeviceItemInformation(RemoteAlarmSystem.getAllDevices()));
                mAddButton.setVisible(true);
                mAddButton.setOnMenuItemClickListener(mAddDeviceListener);
                return true;
            case R.id.navigation_rules:
                mGeneralListFragment.setData(RuleItemInformation
                        .createRuleItemInformation(RemoteAlarmSystem.getAllRules()));
                mAddButton.setVisible(true);
                mAddButton.setOnMenuItemClickListener(mAddRuleListener);
                return true;
            case R.id.navigation_notifications:
                mGeneralListFragment.setData(NotificationItemInformation
                        .createNotificationItemInformation(RemoteAlarmSystem.getAllNotifications()));
                mAddButton.setVisible(false);
                return true;
        }
        return false;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem settings = ((Toolbar) findViewById(R.id.home_toolbar)).getMenu().findItem(R.id.action_settings);
        settings.setOnMenuItemClickListener(mSettingsListener);
        mAddButton = ((Toolbar) findViewById(R.id.home_toolbar)).getMenu().findItem(R.id.action_add);
        prepareContent(mBottomNavigation.getMenu().findItem(mBottomNavigation.getSelectedItemId()));

        return true;
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

        String hostname = PreferenceManager.getDefaultSharedPreferences(this).getString(
                ChangeSystemDialog.SYSTEM_PREF_KEY,
                ChangeSystemDialog.DEFAULT_VALUE);

        String username = PreferenceManager.getDefaultSharedPreferences(this).getString(
                ChangeUsernameDialog.KEY,
                ChangeUsernameDialog.DEFAULT_VALUE);

        String password = PreferenceManager.getDefaultSharedPreferences(this).getString(
                ChangePasswordDialog.KEY,
                ChangePasswordDialog.DEFAULT_VALUE);

        if(!RemoteAlarmSystem.checkSystem(hostname,username,password)){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        mGeneralListFragment = (GeneralListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ruleList);

        mBottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {

                        return prepareContent(item);

                    }

                });

    }
}
