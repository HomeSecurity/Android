package com.hosec.homesecurity.model;

import android.graphics.Color;
import android.view.View;

import com.hosec.homesecurity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D062572 on 07.06.2017.
 */

public class DetailDeviceItemInfo extends DeviceItemInformation {

    private boolean mIsSelected;

    public DetailDeviceItemInfo(Device device) {
        super(device);
        mIsSelected = false;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    @Override
    public boolean animate() {
        return false;
    }

    @Override
    public void onClick(View v) {
        if (mIsSelected) {
            mIsSelected = false;
            v.setBackgroundColor(Color.WHITE);
        } else {
            mIsSelected = true;
            v.setBackgroundColor(0xffcccccc);
        }
    }

    @Override
    public void customizeListItemTemplate(View listItemView) {
        listItemView.findViewById(R.id.itemInfo).setVisibility(View.GONE);
    }

    public static List<DetailDeviceItemInfo> createDetailDeviceItemInformation(List<Device> devices){
        List<DetailDeviceItemInfo> newList = new ArrayList<>(devices.size());
        for(Device d : devices){
            newList.add(new DetailDeviceItemInfo(d));
        }
        return newList;
    }
}
