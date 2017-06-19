package com.hosec.homesecurity.model;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by D062572 on 29.05.2017.
 */

public class Device implements Serializable {

    public enum State {
        OFFLINE,
        DISCONNECTED,
        CONNECTED
    }

    public enum Type {
        SENSOR,
        ACTOR
    }

    public enum InterfaceType {
        IP,
        UART
    }

    private long mID;
    private String mName;
    private String mDescription;
    private State mState;
    private Type mType;
    private InterfaceType mInterfaceType;
    private URL mUrl;

    public Device(JSONObject json) throws JSONException, MalformedURLException {
        this(json.getLong("id"),
                json.getString("name"),
                json.getString("description"),
                State.valueOf(json.getString("state")),
                Type.valueOf(json.getString("type")),
                InterfaceType.valueOf(json.getString("interface")),
                InterfaceType.valueOf(json.getString("interface")) != InterfaceType.IP ?
                        null : new URL(json.getString("url")));
    }

    public Device(long mID, String mName, String mDescription, State mState, Type mType,
                  InterfaceType interfaceType, URL url) {
        this.mID = mID;
        this.mName = mName;
        this.mDescription = mDescription;
        this.mState = mState;
        this.mType = mType;
        this.mInterfaceType = interfaceType;
        this.mUrl = mInterfaceType == InterfaceType.IP ? url : null;

    }

    public long getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public State getState() {
        return mState;
    }

    public Type getType() {
        return mType;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setState(State state) {
        this.mState = state;
    }

    public InterfaceType getInterfaceType() {
        return mInterfaceType;
    }

    public void setConnectionType(InterfaceType interfaceType) {
        this.mInterfaceType = interfaceType;
    }

    public URL getUrl() {
        return mUrl;
    }

    public void setUrl(URL url) {
        this.mUrl = url;
    }


}
