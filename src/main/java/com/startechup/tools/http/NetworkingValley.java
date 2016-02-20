/**
 *   Copyright (2015) StarTechUp Inc.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

 */

package com.startechup.tools.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.startechup.tools.http.custom.ImageUploadRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a helper class for HTTP requests. This class is using a combination of Volley and Square's
 * OkHttp library. OkHttp is used as the networking layer for our Volley library.
 *
 */
public class NetworkingValley {

    /**
     * Identifier for debugging or logging.
     */
    private static final String TAG = "NetworkingValley";

    /**
     * Access token provided from the API after authentication.
     */
    public static String mAccessToken = "";

    private String mBaseUrl;

    private static RequestQueue mRequestQueue;

    private SSLHttpStack mSSLHttpStack;

    private HashMap<String, String> mHeaders;

    private HashMap<String, String> mBodyParams;

    private String mKeyStorePassword = "";

    private String mClientStorePassword = "";

    public static final int DEFAULT_TIMEOUT_MS = 30000;

    private NetworkingValley(Builder builder) {
        mAccessToken = builder.mAccessToken;
        mBaseUrl = builder.mBaseUrl;
        mRequestQueue = builder.mRequestQueue;
        mSSLHttpStack = builder.mSSLHttpStack;
        mHeaders = builder.mHeaders;
        mBodyParams = builder.mBodyParams;
        mKeyStorePassword = builder.mKeyStorePassword;
        mClientStorePassword = builder.mClientStorePassword;
    }

    public static class Builder {

        private Context mContext;

        private String mAccessToken = "";

        private String mBaseUrl;

        private RequestQueue mRequestQueue;

        private SSLHttpStack mSSLHttpStack;

        private HashMap<String, String> mHeaders;

        private HashMap<String, String> mBodyParams;

        private String mKeyStorePassword = "";

        private String mClientStorePassword = "";

        public Builder(Context context) {
            if(mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(context);
            }

            mContext = context;
        }

        public Builder(Context context, String baseUrl) {
            this(context);

            mBaseUrl = baseUrl;
        }

        public Builder loadBaseUrl(String baseUrl) {
            mBaseUrl = baseUrl;

            return this;
        }

        public Builder loadSSLHttpStack(SSLHttpStack sslHttpStack) {
            mSSLHttpStack = sslHttpStack;

            return this;
        }

        public Builder loadCerts(InputStream inputClientStore, InputStream inputKeyStore) {
            mSSLHttpStack = new SSLHttpStack(inputClientStore, inputKeyStore);

            mRequestQueue = Volley.newRequestQueue(mContext, mSSLHttpStack);

            return this;
        }

        public Builder loadAccessToken(String accessToken) {
            mAccessToken = accessToken;

            return this;
        }

        public Builder loadHeaders(HashMap<String, String> headers) {
            mHeaders = headers;

            return this;
        }

        public Builder loadBodyParams(HashMap<String, String> bodyParams) {
            mBodyParams = bodyParams;

            return this;
        }

        public Builder setKeyStorePassword(String keyStorePassword) {
            mKeyStorePassword = keyStorePassword;

            return this;
        }

        public Builder setClientStorePassword(String clientStorePassword) {
            mClientStorePassword = clientStorePassword;

            return this;
        }

        public NetworkingValley build() {
            return new NetworkingValley(this);
        }
    }

    /**
     * Performs and add the network request with tag to identify this network request.
     *
     * @param request Network request
     * @param tag Tag identifier for this request
     */
    public static <T> void addRequestQueue(Request<T> request, String tag) {
        // Set the default tag to our request
        request.setTag(tag);

        // Add our request to the queue
        mRequestQueue.add(request);
    }

    /**
     * Performs and add the network request.
     *
     * @param request Network request
     */
    public static <T> void addRequestQueue(Request<T> request) {
        // Add our request to the queue
        mRequestQueue.add(request);
    }

    /**
     * Constructs a GET network request that returns JSON object as a response.
     *
     * @param url URL of the API
     * @param apiListener Listener whether network request is successful or not.
     * @return JSON object response from the API
     */
    public static JsonObjectRequest constructGetRequest(String url, final OnAPIListener apiListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                getJsonResponseListener(apiListener), getErrorListener(apiListener)
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + getHeaderParams().toString());
                return getHeaderParams();
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a GET network request that returns JSON object as a response. With this method you can specify
     * your own header parameters.
     *
     * @param url URL of the API
     * @param headers Header parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return JSON object response from the API
     */
    public static JsonObjectRequest constructGetRequest(String url, final HashMap<String, String> headers, final OnAPIListener apiListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                getJsonResponseListener(apiListener), getErrorListener(apiListener)
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + headers);
                return headers;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a GET network request that returns JSON array as a response.
     *
     * @param url URL of the API
     * @param apiListener Listener whether network request is successful or not
     * @return JSON array response from the API
     */
    public static JsonArrayRequest constructGetJsonArrayRequest(String url, final OnAPIListener apiListener) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,
                getJsonArrayResponseListener(apiListener), getErrorListener(apiListener))
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + getHeaderParams().toString());
                return getHeaderParams();
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a POST network request that returns a String format response.
     *
     * @param url URL of the API
     * @param bodyArg Body argument parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructPostRequest(String url,
                                                      final HashMap<String, String> bodyArg,
                                                      final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.POST,
                url,
                getStringResponseListener(apiListener),
                getErrorListener(apiListener)) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + getHeaderParams().toString());

                return getHeaderParams();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.i(TAG, "Params: " + bodyArg.toString());
                return bodyArg;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a POST network request that returns a String format response. With this method you
     * can specify your own header parameters.
     *
     * @param url URL of the API
     * @param headers Header parameters to be included in the network request
     * @param bodyArg Body argument parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructPostRequest(String url,
                                                     final HashMap<String, String> headers,
                                                     final HashMap<String, String> bodyArg,
                                                     final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.POST,
                url,
                getStringResponseListener(apiListener),
                getErrorListener(apiListener)) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + headers);

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.i(TAG, "Params: " + bodyArg.toString());
                return bodyArg;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a PUT network request that returns a String format response.
     *
     * @param url URL of the API
     * @param bodyArg Body argument parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructPutRequest(String url,
                                                     final Map<String, String> bodyArg,
                                                     final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.PUT, url, getStringResponseListener(apiListener), getErrorListener(apiListener)
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + getHeaderParams().toString());
                return getHeaderParams();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.i(TAG, "Params: " + bodyArg.toString());
                return bodyArg;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a PUT network request that returns a String format response. With this method you
     * can specify your own header parameters.
     *
     * @param url URL of the API
     * @param headers Header parameters to be included in the network request
     * @param bodyArg Body argument parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructPutRequest(String url,
                                                    final HashMap<String, String> headers,
                                                    final Map<String, String> bodyArg,
                                                    final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.PUT, url, getStringResponseListener(apiListener), getErrorListener(apiListener)
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + headers);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.i(TAG, "Params: " + bodyArg.toString());
                return bodyArg;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a DELETE network request that returns a String format response.
     *
     * @param url URL of the API
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructDeleteRequest(String url,
                                                        final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.DELETE, url, getStringResponseListener(apiListener), getErrorListener(apiListener)){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + getHeaderParams().toString());
                return getHeaderParams();
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs a DELETE network request that returns a String format response. With this method you
     * can specify your own header parameters.
     *
     * @param url URL of the API
     * @param headers Header parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructDeleteRequest(String url,
                                                       final HashMap<String, String> headers,
                                                       final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.DELETE, url, getStringResponseListener(apiListener), getErrorListener(apiListener)){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Log.i(TAG, "Headers: " + headers);
                return headers;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Constructs an authentication request that returns a String format response.
     *
     * @param url URL of the API
     * @param bodyArg Body argument parameters to be included in the network request
     * @param apiListener Listener whether network request is successful or not
     * @return String response from the API
     */
    public static StringRequest constructAuthRequest(String url, final Map<String, String> bodyArg, final OnAPIListener apiListener) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    apiListener.onSuccess(jsonResponse);
                } catch (JSONException je) {
                    je.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apiListener.onFail(error.getClass().getSimpleName());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<>();
                headers.put("Accept","application/json");
                headers.put("Content-Type", "application/x-www-form-urlencoded");

                Log.i(TAG, "Headers: " + headers.toString());

                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Log.i(TAG, "Params: " + bodyArg.toString());
                return bodyArg;
            }
        };

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    public static ImageUploadRequest constructImageUploadRequest(String url, Bitmap bitmap, final OnAPIListener apiListener) {
        ImageUploadRequest request = new ImageUploadRequest(url, bitmap, new Response.Listener() {

            @Override
            public void onResponse(Object response) {
                apiListener.onSuccess(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apiListener.onFail(error.getMessage());
            }
        });

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    public static ImageUploadRequest constructImageUploadRequest(String url, File fileImage, final OnAPIListener apiListener) {
        ImageUploadRequest request = new ImageUploadRequest(url, fileImage, new Response.Listener() {

            @Override
            public void onResponse(Object response) {
                apiListener.onSuccess(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                apiListener.onFail(error.getMessage());
            }
        });

        request.setRetryPolicy(getHttpRetryPolicy());

        return request;
    }

    /**
     * Returns a successful network response listener from the API. Listener returns a JSON object.
     *
     * @param apiListener Listener to indicate that network request is successful.
     * @return Listener for network response
     */
    private static Response.Listener<JSONObject> getJsonResponseListener(final OnAPIListener apiListener) {
        return new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                apiListener.onSuccess(response);
            }
        };
    }

    /**
     * Returns a successful network response listener from the API. Listener returns a JSON array.
     *
     * @param apiListener Listener to indicate that network request is successful.
     * @return Listener for network response
     */
    private static Response.Listener<JSONArray> getJsonArrayResponseListener(final OnAPIListener apiListener) {
        return new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                apiListener.onSuccess(response);
            }
        };
    }

    /**
     * Returns a successful network response listener. Listener returns a response in String format.
     *
     * @param apiListener Listener to indicate that network request is successful.
     * @return Listener for network response
     */
    private static Response.Listener<String> getStringResponseListener(final OnAPIListener apiListener) {
        return new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    apiListener.onSuccess(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Returns a fail network response listener. Listener returns a {@link VolleyError} response.
     *
     * @param apiListener Listener to indicate that network request failed.
     * @return Error listener for network response
     */
    private static Response.ErrorListener getErrorListener(final OnAPIListener apiListener) {
        return new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                apiListener.onFail(error.getClass().getSimpleName());
            }
        };
    }

    /**
     * Returns an updated/extended timeout policy.
     *
     * @return Updated timeout policy
     */
    private static DefaultRetryPolicy getHttpRetryPolicy() {
        return new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    /**
     * Returns a default header parameters to be included in the network request.
     *
     * @return Header parameters to be included in the network request.
     */
    private static Map<String, String> getHeaderParams() {
        Map<String,String> headers = new HashMap<>();
        headers.put("Accept","application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Bearer " + mAccessToken);

        return headers;
    }
}
