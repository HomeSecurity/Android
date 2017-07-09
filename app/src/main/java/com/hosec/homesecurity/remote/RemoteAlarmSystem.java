package com.hosec.homesecurity.remote;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hosec.homesecurity.R;
import com.hosec.homesecurity.model.Credentials;
import com.hosec.homesecurity.model.Device;
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
 * API-Wrapper to communicate with the alarm system
 * This wrapper encapsulates all Web-Service calls and provides listener interfaces for each call.
 * It was implemented using the singleton pattern.
 */
public class RemoteAlarmSystem {

    /**
     * An instance of this class will be passed to every result listener after an API call was sent
     */
    public static class Result {
        public boolean success;
        public String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /**
     * the ResultListener interface that a client must implement to register callbacks for API-calls
     */
    public interface ResultListener {
        public void onResult(Result result);
    }

    /**
     * helper class to simplify the code for loading device data, rule data and notification data
     */
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
            mListener.onResult(new Result(false, mContext.getString(R.string.failed_to_load_data)));
        }

        @Override
        public void onResponse(JSONObject response) {
            int i = 0;
            try {
                handleJSONObject(response);
                i = mCounter.incrementAndGet();
                if (i == COUNTER_MAX) {
                    mListener.onResult(new Result(true, mContext.getString(R.string.retrieved_data)));
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
    private static final String CANCELABLE_TAG = "CANCELABLE";
    private static final String NOT_CANCELABLE_TAG = "NOT_CANCELABLE";
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
        }); //sort notifications by date

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

    private synchronized void setNotificationMap(Map<Date, Notification> notificationMap) {
        mNotificationMap = notificationMap;
    }

    /**
     * Constructs device object from json data
     * @param json
     * @throws JSONException
     */
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

    /**
     * Constructs rule objects from json data
     * @param json
     * @throws JSONException
     */
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

    /**
     * Construts notification objects from json data
     * @param json
     * @throws JSONException
     */
    private void parseNotifications(JSONObject json) throws JSONException {
        Map<Date, Notification> notifications = new HashMap();
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            Notification notification = new Notification(json.getJSONObject(iter.next()), mRuleMap);
            notifications.put(notification.getDate(), notification);
        }
        setNotificationMap(notifications);
    }

    private String buildUrl(String path) {
        return "http://" + mHost + ":8080" + path;
    }

    /**
     * loads device, rule and notification data at the same time to ease the maintenance of consistency
     * first all device information are loaded, second all rule information which get linked to the respective devices
     * third notification information are loaded
     * @param resultListener
     */
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
        requestNotifications.setTag(CANCELABLE_TAG);

        LoadResponseListener listenerRules = new LoadResponseListener(counter, resultListener) {
            @Override
            public void handleJSONObject(JSONObject response) throws JSONException {
                parseRules(response);
                mRequestQueue.add(requestNotifications);
            }
        };

        requestRules = new JsonObjectRequest(Request.Method.GET, buildUrl("/rulelist"),
                null, listenerRules, listenerRules);
        requestRules.setTag(CANCELABLE_TAG);


        LoadResponseListener listenerDevices = new LoadResponseListener(counter, resultListener) {
            @Override
            public void handleJSONObject(JSONObject response) throws JSONException {
                parseDevices(response);
                mRequestQueue.add(requestRules);

            }
        };

        requestDevices = new JsonObjectRequest(Request.Method.GET, buildUrl("/componentlist"),
                null, listenerDevices, listenerDevices);
        requestDevices.setTag(CANCELABLE_TAG);

        mRequestQueue.add(requestDevices);

    }


    /**
     * Logs on to the alarm system and stores the session cookie returned in the cookie manager for future requests
     * @param host
     * @param username
     * @param password
     * @param resultListener
     */
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
                resultListener.onResult(new Result(false,  mContext.getString(R.string.Failed_Login)));
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
        jsObjRequest.setTag(CANCELABLE_TAG);

        mRequestQueue.add(jsObjRequest);


    }

    /**
     * helper to avoid code duplication
     */
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
            jsObjRequest.setTag(CANCELABLE_TAG);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
        }
    }

    /**
     * adds a rule
     * @param rule
     * @param resultListener
     */
    public void addRule(Rule rule, final RemoteAlarmSystem.ResultListener resultListener) {

        callRuleApi(rule, resultListener, Request.Method.POST, "/addrule");
    }

    /**
     * updates a rule
     * @param rule
     * @param resultListener
     */
    public void updateRule(Rule rule, final RemoteAlarmSystem.ResultListener resultListener) {
        callRuleApi(rule, resultListener, Request.Method.PUT, "/updaterule");
    }

    /**
     * uodates device
     * @param device
     * @param resultListener
     */
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
            jsObjRequest.setTag(CANCELABLE_TAG);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
        }
    }

    /**
     * sends the fcm token to the alarm system in order to receive push notifications from it
     * @param token
     * @param listener
     */
    public void setMessagingToken(String token, @Nullable final ResultListener listener) {

        final String TAG = mContext.getString(R.string.HOME_SECURITY_LOG);
        try {
            JSONObject obj = new JSONObject();
            obj.put("token", token);


            Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, mContext.getString(R.string.successful_token_refresh));
                    if(listener != null){
                        listener.onResult(new Result(true, mContext.getString(R.string.successful_token_refresh)));
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, error.getMessage() + " " + mContext.getString(R.string.Token_Fail));
                    if(listener != null){
                        listener.onResult(new Result(false, error.getMessage()));
                    }
                }
            };

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, buildUrl("/settoken"),
                    obj, respListener, errorListener);
            jsObjRequest.setTag(NOT_CANCELABLE_TAG);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            Log.i(TAG, mContext.getString(R.string.Token_Fail));
        }
    }

    /**
     * starts the registration mode of the alarm system which lasts for 60 seconds.
     */
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
        jsObjRequest.setTag(NOT_CANCELABLE_TAG);

        mRequestQueue.add(jsObjRequest);
    }

    /**
     * if the alarm of the remote system is triggered, this method will turn it off
     * @param resultListener
     */
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
        jsObjRequest.setTag(CANCELABLE_TAG);

        mRequestQueue.add(jsObjRequest);
    }

    /**
     * updates user credentials on the alarm system
     * @param creds
     * @param resultListener
     */
    public void updateCredentials(Credentials creds, final RemoteAlarmSystem.ResultListener resultListener) {
        final String TAG = mContext.getString(R.string.HOME_SECURITY_LOG);
        try {
            JSONObject obj = new JSONObject();
            obj.put("username", creds.getUsername());
            obj.put("password", creds.getPassword());


            Response.Listener<JSONObject> respListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        resultListener.onResult(new Result(response.getBoolean("output"),""));
                    } catch (JSONException e) {
                        resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    resultListener.onResult(new Result(false,error.getMessage()));
                }
            };

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, buildUrl("/changecredentials"),
                    obj, respListener, errorListener);
            jsObjRequest.setTag(CANCELABLE_TAG);

            mRequestQueue.add(jsObjRequest);

        } catch (JSONException e) {
            resultListener.onResult(new Result(false, mContext.getString(R.string.invalid_json)));
        }
    }

    /**
     * cancels all cancelable requests
     */
    public void cancelAll(){
        mRequestQueue.cancelAll(CANCELABLE_TAG);
    }

}
