package com.hosec.homesecurity.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Device;
import com.hosec.homesecurity.remote.RemoteAlarmSystem;
import com.hosec.homesecurity.remote.TestRemoteAlarmSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by D062572 on 08.06.2017.
 */

public class AddDeviceToRuleDialog extends AppCompatDialogFragment {

    public interface AddDeviceDialogListener {
        public void onDialogPositiveClick(AddDeviceToRuleDialog dialog);
        public void onDialogNegativeClick(AddDeviceToRuleDialog dialog);
    }


    public static final String SENSOR_KEY = "SENSOR";
    public static final String KNOWN_DEVICES_KEY = "KNOWN_DEVICE";
    private boolean mIsSensor;
    private List<Device> mKnownDevices;
    private AddDeviceDialogListener mListener;
    private List<Device> mNewSelections;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (AddDeviceDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddDeviceDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        Bundle bundle  = getArguments();
        mIsSensor = bundle.getBoolean(SENSOR_KEY);
        mNewSelections = new ArrayList<>();

        mKnownDevices = (ArrayList<Device>) bundle.getSerializable(KNOWN_DEVICES_KEY);
        final List<Device> devicesToBeSelected = getDevicesToBeSelected();
        final boolean[] selectionMask = new boolean[devicesToBeSelected.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mIsSensor ? R.string.add_sensor : R.string.add_actor);
        builder.setMultiChoiceItems(getNameListOfDevices(devicesToBeSelected), null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        selectionMask[which] = isChecked;
                    }
                });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                for(int i = 0; i < selectionMask.length; ++i){
                    if(selectionMask[i]){
                        mNewSelections.add(devicesToBeSelected.get(i));
                    }
                }
                mListener.onDialogPositiveClick(AddDeviceToRuleDialog.this);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogNegativeClick(AddDeviceToRuleDialog.this);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public List<Device> getNewSelections(){
        return mNewSelections;
    }


    private List<Device> getDevicesToBeSelected(){
        List<Device> allDevices = RemoteAlarmSystem.getInstance(getContext()).getDeviceList();
        List<Device> toBeSelected = new ArrayList<>();

        Iterator<Device> iterAll = allDevices.iterator();

        Device all, known;
        while(iterAll.hasNext()){
            all = iterAll.next();

            Iterator<Device> iterKnown = mKnownDevices.iterator();
            while(iterKnown.hasNext()){
                known = iterKnown.next();
                if(all.getID() == known.getID()){
                    iterAll.remove();
                    break;
                }
            }
        }

        for(Device device : allDevices){
            if(device.getType() == Device.Type.SENSOR && mIsSensor){
                toBeSelected.add(device);
            }else if(device.getType() == Device.Type.ACTOR && !mIsSensor){
                toBeSelected.add(device);
            }
        }

        return toBeSelected;
    }

    private String[] getNameListOfDevices(List<Device> devices){
        String[] names = new String[devices.size()];
        for(int i = 0; i< names.length; ++i){
            names[i] = devices.get(i).getName();
        }
        return names;
    }

}
