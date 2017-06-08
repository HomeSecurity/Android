package com.hosec.homesecurity.model;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.activities.DeviceDetailActivity;
import com.hosec.homesecurity.activities.HomeActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D062572 on 07.06.2017.
 */

public class DeviceItemInformation extends ListItemInformation {

    private Device mDevice;

    public DeviceItemInformation(Device device){
        super();
        mDevice = device;
    }

    public Device getDevice(){
        return mDevice;
    }

    @Override
    public String getTitle() {
        return mDevice.getName();
    }

    @Override
    public String getSubtitle() {
        return mDevice.getState().toString();
    }

    @Override
    public int getImageId() {
        return R.mipmap.ic_chip;
    }


    @Override
    public void onClick(View v) {
        Activity activity = (Activity) v.getContext();
        Intent intent  = new Intent(activity, DeviceDetailActivity.class);
        intent.putExtra(DeviceDetailActivity.DEVICE_KEY, mDevice);
        activity.startActivityForResult(intent,HomeActivity.REQUEST_CODE_DETAIL);
    }

    public static List<DeviceItemInformation> createDeviceItemInformation(List<Device> devices){
        List<DeviceItemInformation> newList = new ArrayList<>(devices.size());
        for(Device d : devices){
            newList.add(new DeviceItemInformation(d));
        }
        return newList;
    }
}
