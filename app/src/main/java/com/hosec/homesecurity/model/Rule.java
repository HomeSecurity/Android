package com.hosec.homesecurity.model;

import android.content.res.Resources;

import com.hosec.homesecurity.remote.RemoteAlarmSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by D062572 on 29.05.2017.
 */

public class Rule implements Serializable {

    private static final long INVALID_ID = -1;
    private long mID;
    private ArrayList<Device> mSensors;
    private ArrayList<Device> mActors;
    private String mName;
    private boolean mActive;

    public Rule(JSONObject json, List<Device> devices) throws JSONException {

        long id = json.getLong("id");
        String name = json.getString("name");
        boolean active = json.getBoolean("active");
        JSONObject output = json.getJSONObject("output");
        JSONObject input = json.getJSONObject("input");


        ArrayList<Device> sensors = new ArrayList<>();
        Iterator<String> iter = input.keys();
        while (iter.hasNext()) {
            Device device = getDeviceById(devices, Long.parseLong(iter.next()));
            if (device != null) {
                sensors.add(device);
            }
        }

        ArrayList<Device> actors = new ArrayList<>();
        iter = output.keys();
        while (iter.hasNext()) {
            Device device = getDeviceById(devices, Long.parseLong(iter.next()));
            if (device != null) {
                actors.add(device);
            }
        }

        mID = id;
        mSensors = sensors;
        mActors = actors;
        mActive = active;
        mName = name;

    }

    public Rule(boolean active) {
        this(INVALID_ID, active, new ArrayList<Device>(), new ArrayList<Device>(),
                Resources.getSystem().getString(android.R.string.untitled));
    }

    public Rule(long id, boolean active, ArrayList<Device> sensors, ArrayList<Device> actors, String name) {
        mID = id;
        mSensors = sensors;
        mActors = actors;
        mActive = active;
        mName = name;
    }

    private Device getDeviceById(List<Device> devices, long id) {
        Device device = null;

        for (Device d : devices) {
            if (d.getID() == id) {
                device = d;
                break;
            }
        }

        return device;
    }

    public void setID(long id) {
        mID = id;
    }

    public long getID() {
        return mID;
    }

    public ArrayList<Device> getSensors() {
        return mSensors;
    }

    public ArrayList<Device> getActors() {
        return mActors;
    }

    public void setActors(ArrayList<Device> actors) {
        mActors = actors;
    }

    public void setSensors(ArrayList<Device> sensors) {
        mSensors = sensors;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public boolean active() {
        return mActive;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray ary = new JSONArray();
        for(Device d : mSensors){
            ary.put(d.getID());
        }
        for(Device d : mActors){
            ary.put(d.getID());
        }

        obj.put("id", mID);
        obj.put("name",mName);
        obj.put("active", mActive);
        obj.put("components", ary);

        return obj;

    }

}
