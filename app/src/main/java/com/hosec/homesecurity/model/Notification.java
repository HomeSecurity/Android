package com.hosec.homesecurity.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by D062572 on 29.05.2017.
 */

public class Notification implements Serializable{

    private Date mDate;
    private boolean mIsTriggered;
    private Rule mRule;
    private Bitmap mBitmap;


    public Notification(JSONObject object, Map<Long,Rule> ruleMap) throws JSONException {
        this.mDate = new Date(object.getLong("date"));
        this.mIsTriggered =  object.getBoolean("triggered");
        this.mRule = ruleMap.get(object.getLong("ruleid"));
        String image = object.getString("image");
        mBitmap = image != null ? BitmapFactory.decodeByteArray(image.getBytes(),0,image.getBytes().length)
                                : null;
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isTriggered(){ return mIsTriggered; }

    public Rule getRule(){
        return mRule;
    }

    public Bitmap getBitmap(){
        return mBitmap;
    }


}
