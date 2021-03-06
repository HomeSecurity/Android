package com.hosec.homesecurity.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Device;
import com.hosec.homesecurity.remote.RemoteAlarmSystem.*;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Displays device data
 * TODO: UI could be more beautiful, missing battery status
 */
public class DeviceDetailActivity extends RemoteAPIActivity {

    public static final String DEVICE_KEY = "DEVICE";

    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mDevice = (Device) intent.getSerializableExtra(DEVICE_KEY);

        fillWithDeviceData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.detail_menu, menu);

        //when the tick icon is clicked --> send device data to alarm system
        menu.findItem(R.id.action_save).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                EditText name = (EditText) findViewById(R.id.etName);

                mDevice.setName(name.getText().toString());

                if (mDevice.getInterfaceType() == Device.InterfaceType.IP) {
                    EditText url = (EditText) findViewById(R.id.etUrl);
                    try {
                        mDevice.setUrl(new URL(url.getText().toString()));
                    } catch (MalformedURLException e) {
                        Toast.makeText(DeviceDetailActivity.this, R.string.malformed_url, Toast.LENGTH_SHORT).show();
                    }
                }

                if (mDevice.getState() != Device.State.OFFLINE) {
                    Switch switchView = (Switch) findViewById(R.id.switchConnect);
                    mDevice.setState(switchView.isChecked() ? Device.State.CONNECTED
                            : Device.State.DISCONNECTED);
                }

                mRemoteAlarmSystem.updateDevice(mDevice, new ResultListener() {
                    @Override
                    public void onResult(Result result) {
                        finish();
                    }
                });

                return true;
            }
        });

        return true;
    }


    /**
     * Fills all ui controls with respective device data fields
     */
    private void fillWithDeviceData() {

        ((TextView) findViewById(R.id.tvTypeValue)).setText(mDevice.getType().toString());
        ((TextView) findViewById(R.id.tvStateValue)).setText(mDevice.getState().toString());
        ((TextView) findViewById(R.id.etName)).setText(mDevice.getName());
        ((TextView) findViewById(R.id.tvDescriptionValue)).setText(mDevice.getDescription());
        ((TextView) findViewById(R.id.tvIDValue)).setText(mDevice.getID() + "");
        ((TextView) findViewById(R.id.tvInterfaceValue)).setText(mDevice.getInterfaceType().toString());

        EditText editUrl = ((EditText) findViewById(R.id.etUrl));
        if (mDevice.getInterfaceType() == Device.InterfaceType.IP) {
            editUrl.setText(mDevice.getUrl().toString());
        } else {
            editUrl.setText("");
            editUrl.setEnabled(false);
        }

        Switch switchView = (Switch) findViewById(R.id.switchConnect);

        switch (mDevice.getState()) {
            case CONNECTED:
                switchView.setChecked(true);
                break;
            case DISCONNECTED:
                switchView.setChecked(false);
                break;
            case OFFLINE:
                switchView.setChecked(false);
                switchView.setEnabled(false);
                break;
        }
    }
}
