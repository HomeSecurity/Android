package com.hosec.homesecurity.activities;


import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.DetailDeviceItemInfo;
import com.hosec.homesecurity.model.Device;
import com.hosec.homesecurity.model.ListItemInformation;
import com.hosec.homesecurity.model.Rule;
import com.hosec.homesecurity.remote.TestRemoteAlarmSystem;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class RuleDetailActivity extends AppCompatActivity
        implements AddDeviceToRuleDialog.AddDeviceDialogListener {

    public static final String RULE_KEY = "RULE";
    public static final String NEW_KEY = "NEW";

    private static final String DIALOG_KEY = "ADD_DEVICE";
    private static final String TAB_SENSOR = "TAB_SENSOR";
    private static final int SENSOR_TAB_INDEX = 0;
    private static final String TAB_ACTOR = "TAB_ACTOR";

    private GeneralListFragment mSensorListFragment;
    private GeneralListFragment mActorListFragment;
    private Rule mRule;
    private EditText mEditTextName;
    private Switch mActiveSwitch;
    private TabHost mHost;
    private boolean isNewRule;

    private View.OnClickListener mAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.on_click_to_accent));

            AddDeviceToRuleDialog dialog = new AddDeviceToRuleDialog();
            Bundle bundle = new Bundle();
            GeneralListFragment frag = getCurrentDeviceListFragment();

            bundle.putBoolean(AddDeviceToRuleDialog.SENSOR_KEY, isSensorTabSelected());
            bundle.putSerializable(AddDeviceToRuleDialog.KNOWN_DEVICES_KEY, getListItemsAsDeviceList(frag.getData()));
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), DIALOG_KEY);
        }
    };

    private View.OnClickListener mDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.on_click_to_accent));
            onRemove();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        //prepare tabs
        mHost = (TabHost) findViewById(R.id.tabHost);
        mHost.setup();
        mHost.addTab(mHost.newTabSpec(TAB_SENSOR).setIndicator("Sensors").setContent(R.id.tabSensor));
        mHost.addTab(mHost.newTabSpec(TAB_ACTOR).setIndicator("Actors").setContent(R.id.tabActor));
        mHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String arg0) {
                markSelectedTab();
            }
        });
        markSelectedTab();

        //fill UI
        Intent intent = getIntent();
        isNewRule = intent.getBooleanExtra(NEW_KEY, false);
        mRule = (Rule) intent.getSerializableExtra(RULE_KEY);
        mSensorListFragment = (GeneralListFragment) getSupportFragmentManager().findFragmentById(R.id.sensorList);
        mSensorListFragment.setData(DetailDeviceItemInfo.createDetailDeviceItemInformation(mRule.getSensors()));
        mActorListFragment = (GeneralListFragment) getSupportFragmentManager().findFragmentById(R.id.actorList);
        mActorListFragment.setData(DetailDeviceItemInfo.createDetailDeviceItemInformation(mRule.getActors()));

        mEditTextName = (EditText) findViewById(R.id.etName);
        mEditTextName.setText(mRule.getName());
        mActiveSwitch = (Switch) findViewById(R.id.switchActive);
        mActiveSwitch.setChecked(mRule.active());

        //prepare icon buttons
        findViewById(R.id.ivAdd).setOnClickListener(mAddListener);
        findViewById(R.id.ivDelete).setOnClickListener(mDeleteListener);

    }

    private void onRemove() {
        GeneralListFragment frag = getCurrentDeviceListFragment();

        List<? extends ListItemInformation> list = frag.getData();
        Iterator<? extends ListItemInformation> iter = list.iterator();
        while (iter.hasNext()) {
            DetailDeviceItemInfo info = (DetailDeviceItemInfo) iter.next();
            if (info.isSelected()) {
                iter.remove();
            }
        }
        frag.setData(list);
    }

    private boolean isSensorTabSelected() {
        return mHost.getCurrentTab() == SENSOR_TAB_INDEX;
    }

    private GeneralListFragment getCurrentDeviceListFragment(){
        return isSensorTabSelected() ? mSensorListFragment : mActorListFragment;
    }

    private ArrayList<Device> getListItemsAsDeviceList(List<? extends ListItemInformation> list) {
        ArrayList<Device> devices = new ArrayList<>();
        for (ListItemInformation info : list) {
            devices.add(((DetailDeviceItemInfo) info).getDevice());
        }
        return devices;
    }

    private void markSelectedTab() {
        final int COLOR_SELECTED = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);
        final int COLOR_UNSELECTED = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);

        View view;
        TextView tv;

        for (int i = 0; i < mHost.getTabWidget().getChildCount(); i++) {
            view = mHost.getTabWidget().getChildAt(i);
            view.setBackgroundColor(COLOR_UNSELECTED); // unselected
            tv = (TextView) view.findViewById(android.R.id.title);
            tv.setTextColor(COLOR_SELECTED);

        }
        view = mHost.getTabWidget().getChildAt(mHost.getCurrentTab());
        view.setBackgroundColor(COLOR_SELECTED); // selected
        tv = (TextView) view.findViewById(android.R.id.title);
        tv.setTextColor(COLOR_UNSELECTED);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        menu.findItem(R.id.action_save).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                ArrayList<Device> newSensors = getListItemsAsDeviceList(mSensorListFragment.getData());
                ArrayList<Device> newActors = getListItemsAsDeviceList(mActorListFragment.getData());

                mRule.setName(mEditTextName.getText().toString());
                mRule.setActive(mActiveSwitch.isChecked());

                mRule.setActors(newActors);
                mRule.setSensors(newSensors);
                if (isNewRule) {
                    TestRemoteAlarmSystem.addNewRule(mRule);
                } else {
                    TestRemoteAlarmSystem.updateRuleInformation(mRule);
                }
                finish();
                return true;
            }
        });

        return true;
    }

    @Override
    public void onDialogPositiveClick(AddDeviceToRuleDialog dialog) {
        GeneralListFragment frag = getCurrentDeviceListFragment();
        List<Device> devices = getListItemsAsDeviceList(frag.getData());
        devices.addAll(dialog.getNewSelections());
        frag.setData(DetailDeviceItemInfo.createDetailDeviceItemInformation(devices));
    }

    @Override
    public void onDialogNegativeClick(AddDeviceToRuleDialog dialog) {

    }

    public void testConnection(URL url){

    }
}
