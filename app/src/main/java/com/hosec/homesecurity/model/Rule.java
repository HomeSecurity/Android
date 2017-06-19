package com.hosec.homesecurity.model;

import android.content.res.Resources;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by D062572 on 29.05.2017.
 */

public class Rule implements Serializable{

    private static final long INVALID_ID = -1;
    private long mID;
    private ArrayList<Device> mSensors;
    private ArrayList<Device> mActors;
    private String mName;
    private boolean mActive;


    public Rule(JSONObject json){

    }

    public Rule(boolean active) {
        this(INVALID_ID,active, new ArrayList<Device>(),new ArrayList<Device>(),
                Resources.getSystem().getString(android.R.string.untitled));
    }

    public Rule(long id, boolean active, ArrayList<Device> sensors, ArrayList<Device> actors, String name) {
        mID = id;
        mSensors = sensors;
        mActors = actors;
        mActive = active;
        mName = name;
    }

    public void setID(long id){mID = id;}

    public long getID(){
        return mID;
    }

    public ArrayList<Device> getSensors() {
        return mSensors;
    }

    public ArrayList<Device> getActors() {
        return mActors;
    }

    public void setActors(ArrayList<Device> actors){
        mActors = actors;
    }

    public void setSensors(ArrayList<Device> sensors){
        mSensors = sensors;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setActive(boolean active){
        mActive = active;
    }

    public boolean active(){
        return mActive;
    }


}
