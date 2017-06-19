package com.hosec.homesecurity.remote;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Device;
import com.hosec.homesecurity.model.DeviceItemInformation;
import com.hosec.homesecurity.model.Notification;
import com.hosec.homesecurity.model.Rule;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by D062572 on 19.06.2017.
 */

public class RemoteAlarmSystem {

    /**
     * Other classes and interfaces
     */
    public static class Result {
        public boolean success;
        public String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public interface ResultListener {
        public void onResult(Result result);
    }

    private abstract class LoadResponseListener implements Response.Listener<JSONObject>,
            Response.ErrorListener {

        public static final int COUNTER_MAX = 3;
        private AtomicInteger mCounter;
        private ResultListener mListener;

        public LoadResponseListener(AtomicInteger counter, ResultListener listener) {
            mCounter = counter;
            mListener = listener;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            mListener.onResult(new Result(false, error.getMessage()));
        }

        @Override
        public void onResponse(JSONObject response) {
            int i = 0;
            try {
                handleJSONObject(response);
                i = mCounter.incrementAndGet();
                if (i == COUNTER_MAX) {
                    mListener.onResult(new Result(true, mContext.getString(R.string.Successful_Login)));
                }
            } catch (JSONException e) {
                mListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
            }
        }

        public abstract void handleJSONObject(JSONObject response) throws JSONException;

    }

    /**
     * Remote Alarm System fields
     */
    private static RemoteAlarmSystem msRemoteAlarmSystem;
    private List<Device> mDeviceList;
    private List<Notification> mNotificationList;
    private List<Rule> mRuleList;
    private String mHost;
    private Context mContext;
    private RequestQueue mRequestQueue;


    public static synchronized RemoteAlarmSystem getInstance(Context context, String hostname) {
        if (msRemoteAlarmSystem == null) {
            msRemoteAlarmSystem = new RemoteAlarmSystem(context, hostname);
            CookieHandler.setDefault(new CookieManager());
        }
        msRemoteAlarmSystem.mContext = context;
        return msRemoteAlarmSystem;
    }

    private RemoteAlarmSystem(Context context, String hostname) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mHost = hostname;
        mRuleList = new ArrayList<>();
        mDeviceList = new ArrayList<>();
        mNotificationList = new ArrayList<>();

    }

    public synchronized List<Device> getDeviceList() {
        return new ArrayList<>(mDeviceList);
    }

    private synchronized void setDeviceList(List<Device> deviceList) {
        mDeviceList = deviceList;
    }

    public synchronized List<Rule> getRuleList() {
        return new ArrayList<>(mRuleList);
    }

    private synchronized void setRuleList(List<Rule> ruleList) {
        mRuleList = ruleList;
    }

    private void parseDevices(JSONObject json) throws JSONException {
        List<Device> devices = new ArrayList<Device>();
        int i = 0;
        String index;
        while (json.has(index = i++ + "")) {
            try {
                devices.add(new Device(json.getJSONObject(index)));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        setDeviceList(devices);
    }

    private void parseRules(JSONObject json) throws JSONException {
        List<Rule> rules = new ArrayList<Rule>();
        int i = 0;
        String index;
        while (json.has(index = i++ + "")) {
            rules.add(new Rule(json.getJSONObject(index)));
        }
        setRuleList(rules);
    }

    private String buildUrl(String path) {
        return "http://" + mHost + ":8080" + path;
    }

    public void loadData(final RemoteAlarmSystem.ResultListener resultListener) {
        final AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        final JsonObjectRequest requestDevices, requestRules, requestNotifications;

        LoadResponseListener listenerRules = new LoadResponseListener(counter, resultListener) {
            @Override
            public void handleJSONObject(JSONObject response) throws JSONException {
                parseRules(response);
            }
        };

        requestRules = new JsonObjectRequest(Request.Method.GET, buildUrl("/rulelist"),
                null, listenerRules, listenerRules);

        LoadResponseListener listenerDevices = new LoadResponseListener(counter, resultListener) {
            @Override
            public void handleJSONObject(JSONObject response) throws JSONException {
                parseDevices(response);
                mRequestQueue.add(requestRules);

            }
        };

        requestDevices = new JsonObjectRequest(Request.Method.GET, buildUrl("/componentlist"),
                null, listenerDevices, listenerDevices);

        mRequestQueue.add(requestDevices);

    }


    public void logOnToSystem(String host, String username, String password,
                              final RemoteAlarmSystem.ResultListener resultListener) {

        JSONObject object = new JSONObject();
        try {
            object.put("username", username);
            object.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
            (Request.Method.GET, buildUrl("/login"), object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        resultListener.onResult(new Result(response.getBoolean("output"), mContext.getString(R.string.Successful_Login)));
                    } catch (JSONException e) {
                        resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    resultListener.onResult(new Result(false, error.getMessage()));
                }
            });
        mRequestQueue.add(jsObjRequest);


    }

}
