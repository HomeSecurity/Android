package com.hosec.homesecurity.remote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

        public static final int COUNTER_MAX = 2;
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
                    mCounter.set(0);
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
    private Map<Long, Device> mDeviceMap;
    private Map<Date, Notification> mNotificationMap;
    private Map<Long, Rule> mRuleMap;
    private String mHost;
    private Context mContext;
    private RequestQueue mRequestQueue;


    public static synchronized RemoteAlarmSystem getInstance(Context context) {
        if (msRemoteAlarmSystem == null) {
            msRemoteAlarmSystem = new RemoteAlarmSystem(context);
            CookieHandler.setDefault(new CookieManager());
        }
        msRemoteAlarmSystem.mContext = context;
        return msRemoteAlarmSystem;
    }

    private RemoteAlarmSystem(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mHost = "127.0.0.1";
        mRuleMap = new HashMap<>();
        mDeviceMap = new HashMap<>();
        mNotificationMap = new TreeMap<Date, Notification>(new Comparator<Date>() {
            @Override
            public int compare(Date d1, Date d2) {
                return d1.after(d2) ? 1 : -1;
            }
        });

    }

    public synchronized List<Notification> getNotificationList() {
        return new ArrayList<Notification>(mNotificationMap.values());
    }

    public synchronized List<Device> getDeviceList() {
        return new ArrayList<Device>(mDeviceMap.values());
    }

    private synchronized void setDeviceMap(Map<Long, Device> deviceMap) {
        mDeviceMap = deviceMap;
    }

    public synchronized List<Rule> getRuleList() {
        return new ArrayList<>(mRuleMap.values());
    }

    private synchronized void setRuleMap(Map<Long, Rule> ruleMap) {
        mRuleMap = ruleMap;
    }

    private synchronized void setmNotificationMap(Map<Date, Notification> notificationMap) {
        mNotificationMap = notificationMap;
    }

    private void parseDevices(JSONObject json) throws JSONException {
        Map<Long, Device> devices = new HashMap();
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            try {
                Device device = new Device(json.getJSONObject(iter.next()));
                devices.put(device.getID(), device);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        setDeviceMap(devices);
    }

    private void parseRules(JSONObject json) throws JSONException {
        Map<Long, Rule> rules = new HashMap();
        List<Device> devices = getDeviceList();
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            Rule rule = new Rule(json.getJSONObject(iter.next()), devices);
            rules.put(rule.getID(), rule);
        }
        setRuleMap(rules);
    }

    private void parseNotifications(JSONObject json) throws JSONException {
        Map<Date, Notification> notifications = new HashMap();
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            Notification notification = new Notification(json.getJSONObject(iter.next()), mRuleMap);
            notifications.put(notification.getDate(), notification);
        }
        setmNotificationMap(notifications);
    }

    private String buildUrl(String path) {
        return "http://" + mHost + ":8080" + path;
    }

    public void loadData(final RemoteAlarmSystem.ResultListener resultListener) {
        final AtomicInteger counter = new AtomicInteger();
        counter.set(0);
        final JsonObjectRequest requestDevices, requestRules, requestNotifications;

        LoadResponseListener listenerNotifications = new LoadResponseListener(counter, resultListener) {
            @Override
            public void handleJSONObject(JSONObject response) throws JSONException {
                parseNotifications(response);
            }
        };

        requestNotifications = new JsonObjectRequest(Request.Method.GET, buildUrl("/notificationlist"),
                null, listenerNotifications, listenerNotifications);

        LoadResponseListener listenerRules = new LoadResponseListener(counter, resultListener) {
            @Override
            public void handleJSONObject(JSONObject response) throws JSONException {
                parseRules(response);
                mRequestQueue.add(requestNotifications);
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
        mHost = host;

        Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean success = response.getBoolean("output");
                    resultListener.onResult(new Result(success, success ?
                            mContext.getString(R.string.Successful_Login) :
                            mContext.getString(R.string.Failed_Login)));
                } catch (JSONException e) {
                    resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resultListener.onResult(new Result(false, error.getMessage()));
            }
        };


        try {
            object.put("username", username);
            object.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, buildUrl("/login"),
                object, respListener, errorListener);

        mRequestQueue.add(jsObjRequest);


    }

    private void callRuleApi(final Rule rule, final RemoteAlarmSystem.ResultListener resultListener,
                             int method, String path) {
        try {
            JSONObject obj = rule.toJSON();

            Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mRuleMap.put(rule.getID(), rule);
                    resultListener.onResult(new Result(true, mContext.getString(R.string.success)));
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    resultListener.onResult(new Result(false, error.getMessage()));
                }
            };

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(method, buildUrl(path),
                    obj, respListener, errorListener);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
        }
    }

    public void addRule(Rule rule, final RemoteAlarmSystem.ResultListener resultListener) {

        callRuleApi(rule, resultListener, Request.Method.POST, "/addrule");
    }

    public void updateRule(Rule rule, final RemoteAlarmSystem.ResultListener resultListener) {
        callRuleApi(rule, resultListener, Request.Method.PUT, "/updaterule");
    }

    public void updateDevice(final Device device, final RemoteAlarmSystem.ResultListener resultListener) {
        try {
            JSONObject obj = device.toJSON();

            Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mDeviceMap.put(device.getID(), device);
                    resultListener.onResult(new Result(true, mContext.getString(R.string.success)));
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    resultListener.onResult(new Result(false, error.getMessage()));
                }
            };

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.PUT, buildUrl("/updatecomponent"),
                    obj, respListener, errorListener);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
        }
    }

    public void setMessagingToken(String token) {

        final String TAG = mContext.getString(R.string.HOME_SECURITY_LOG);
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", token);


            Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, "New token was refreshed successfully");
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, error.getMessage() + " " + mContext.getString(R.string.Token_Fail));
                }
            };

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, buildUrl("/settoken"),
                    obj, respListener, errorListener);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            Log.i(TAG, mContext.getString(R.string.Token_Fail));
        }
    }

    public void startRegistrationMode() {
        final String TAG = mContext.getString(R.string.HOME_SECURITY_LOG);
        Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Registration Mode was started successfully");
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Failed to start registration mode");
            }
        };

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, buildUrl("/registration"),
                null, respListener, errorListener);

        mRequestQueue.add(jsObjRequest);
    }

    public void resetAlarm(final RemoteAlarmSystem.ResultListener resultListener) {
        final String TAG = mContext.getString(R.string.HOME_SECURITY_LOG);
        Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Reset alarm successfully");
                resultListener.onResult(new Result(true,"Reset alarm successfully"));
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, error.getMessage());
                resultListener.onResult(new Result(false, "Failed to reset alarm"));
            }
        };

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, buildUrl("/resetalarm"),
                null, respListener, errorListener);

        mRequestQueue.add(jsObjRequest);
    }

}
