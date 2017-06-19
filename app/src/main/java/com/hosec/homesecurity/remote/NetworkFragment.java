package com.hosec.homesecurity.remote;

import android.app.Activity;
import android.content.Context;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by D062572 on 12.06.2017.
 */

public class NetworkFragment extends Fragment {

    private static CookieManager msCookieManager = new CookieManager();
    private RequestCallback<Result> mCallback;
    private RequestTask mRequestTask;



    public void setCallback(RequestCallback<Result> callback) {
        mCallback = callback;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Host Activity will handle callbacks from task.
        mCallback = (RequestCallback<Result>) activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
        mCallback = (RequestCallback<Result>) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity to avoid memory leak.
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelRequest();
        super.onDestroy();
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    public void request(String method, String host, String path) {
        cancelRequest();
        mRequestTask = new RequestTask(mCallback, method, host, path, null);
        mRequestTask.execute();
    }

    public void request(String method, String host, String path, String body) {
        cancelRequest();
        mRequestTask = new RequestTask(mCallback, method, host, path, body);
        mRequestTask.execute();
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    public void cancelRequest() {
        if (mRequestTask != null) {
            mRequestTask.cancel(true);
        }

    }

    /**
     * Wrapper class that serves as a union of a result value and an exception. When the download
     * task has completed, either the result value or exception can be a non-null value.
     * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
     */
    public static class Result {
        public boolean mError;
        public Exception mException;
        public String mBody;
        public int mResponseCode;
        public Map<String, List<String>> mHeaderMap;

        public Result(int code, String body, Map<String, List<String>> headerMap) {
            mBody = body;
            mResponseCode = code;
            mHeaderMap = headerMap;
            mError = false;
        }

        public Result(Exception exception) {
            mException = exception;
            mError = true;
        }
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private static class RequestTask extends AsyncTask<Void, Integer, Result> {

        public static final String PORT = "8080";
        private static final String COOKIES_HEADER = "Set-Cookie";
        private RequestCallback<Result> mCallback;
        private String mURLString;
        private String mMethod;
        private String mBody;

        RequestTask(RequestCallback<Result> callback, String method, String host, String path, String body) {
            mMethod = method;
            mURLString = "http://" + host + ":" + PORT + path;
            mBody = body;
            setCallback(callback);
        }

        void setCallback(RequestCallback<Result> callback) {
            mCallback = callback;
        }


        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (mCallback != null) {
                NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
                /*if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    mCallback.updateFromRequest(null);
                    cancel(true);
                }*/
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected Result doInBackground(Void... params) {
            Result result = null;
            if (!isCancelled()) {
                try {
                    URL url = new URL(mURLString);
                    result = sendRequest(mMethod, url);
                } catch (Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Updates the RequestCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && mCallback != null) {

                mCallback.onResult(result);
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
        }

        private Result sendRequest(String method, URL url) throws IOException {

            final int TIMEOUT = 3000;

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection = null;
            Result result = null;

            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                connection.setRequestMethod(method);
                connection.setDoInput(true);
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    connection.setRequestProperty("Cookie",
                            TextUtils.join(";",  msCookieManager.getCookieStore().getCookies()));
                }

                if (mBody != null) {
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    outputStream = connection.getOutputStream();
                    outputStream.write(mBody.getBytes("UTF-8"));
                    outputStream.close();
                    outputStream = null;

                }
                connection.connect();
                publishProgress(RequestCallback.Progress.CONNECT_SUCCESS);
                if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    if (inputStream != null) {
                        publishProgress(RequestCallback.Progress.GET_INPUT_STREAM_SUCCESS);
                        Map<String, List<String>> headerFields = connection.getHeaderFields();
                        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                        if (cookiesHeader != null) {
                            for (String cookie : cookiesHeader) {
                                msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                            }
                        }

                        result = new Result(connection.getResponseCode(),
                                getBody(inputStream),
                                connection.getHeaderFields());
                    }
                }else{
                    result = new Result(connection.getResponseCode(), "", connection.getHeaderFields());
                }

            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

                if (connection != null) {
                    connection.disconnect();
                }

            }

            return result;
        }
        private String getBody(InputStream in) throws IOException {
            int nRead;
            byte[] data = new byte[4096];
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            while ((nRead = in.read(data, 0, data.length)) != -1) {
                byteStream.write(data, 0, nRead);
            }

            return new String(byteStream.toByteArray());
        }
    }




}
