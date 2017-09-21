package com.badeeb.driveit.driver.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.driveit.client.model.Trip;
import com.badeeb.driveit.driver.MainActivity;
import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.model.JsonRequestTrip;
import com.badeeb.driveit.driver.network.MyVolley;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestDialogFragment extends DialogFragment {

    // Logging Purpose
    public static final String TAG = RequestDialogFragment.class.getSimpleName();

    // Class Attributes
    private Trip mtrip;

    public RequestDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_dialog, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Initialize Attributes
        this.mtrip = Parcels.unwrap(getArguments().getParcelable("trip"));

        // Setup Listeners
        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        Button bAccept = view.findViewById(R.id.bAccept);

        bAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - bAccept_onClick - Start");

                acceptRide();

                Log.d(TAG, "setupListeners - bAccept_onClick - End");
            }
        });

        Button bReject = view.findViewById(R.id.bReject);
        bReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - bReject_onClick - Start");

                rejectRide();

                Log.d(TAG, "setupListeners - bReject_onClick - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void acceptRide() {
        Log.d(TAG, "acceptRide - Start");

        String url = AppPreferences.BASE_URL + "/trip" + "/" + mtrip.getId() + "/accept ";

        final AvialabilityFragment avialabilityFragment = (AvialabilityFragment) getTargetFragment();;

        try {

            JsonRequestTrip request = new JsonRequestTrip();
            request.setTripId(mtrip.getId());

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "acceptRide - Json Request"+ gson.toJson(request));

            // Call user acceptRide service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "acceptRide - onResponse - Start");

                            Log.d(TAG, "acceptRide - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                            Log.d(TAG, "acceptRide - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "acceptRide - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success Ride Acceptance
                                avialabilityFragment.showRideAcceptMessage(true);
                            }
                            else {
                                // Invalid Ride Acceptance
                                avialabilityFragment.showRideAcceptMessage(false);
                            }


                            Log.d(TAG, "acceptRide - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "acceptRide - onErrorResponse: " + error.toString());


                            if (error instanceof ServerError) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                Log.d(TAG, "acceptRide - Error Data: " + responseData);

                                JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                                Log.d(TAG, "acceptRide - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "acceptRide - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                avialabilityFragment.displayMessage(jsonResponse.getJsonMeta().getMessage());
                            }
                        }
                    }
            ) {

                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Accept", "*");
                    headers.put("Authorization", "Token token=" + MainActivity.mdriver.getToken());
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestDialogFragment.this.dismiss();

        Log.d(TAG, "acceptRide - End");
    }

    private void rejectRide() {
        Log.d(TAG, "rejectRide - Start");

        String url = AppPreferences.BASE_URL + "/trip" + "/" + mtrip.getId() + "/reject ";

        final AvialabilityFragment avialabilityFragment = (AvialabilityFragment) getTargetFragment();

        try {

            JsonRequestTrip request = new JsonRequestTrip();
            request.setTripId(mtrip.getId());

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "rejectRide - Json Request"+ gson.toJson(request));

            // Call user acceptRide service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "rejectRide - onResponse - Start");

                            Log.d(TAG, "rejectRide - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                            Log.d(TAG, "rejectRide - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "rejectRide - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success Ride Acceptance
                                avialabilityFragment.showRideRejectMessage(true);
                            }
                            else {
                                // Invalid Ride Acceptance
                                avialabilityFragment.showRideRejectMessage(false);
                            }


                            Log.d(TAG, "rejectRide - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "rejectRide - onErrorResponse: " + error.toString());


                            if (error instanceof ServerError) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                Log.d(TAG, "rejectRide - Error Data: " + responseData);

                                JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                                Log.d(TAG, "rejectRide - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "rejectRide - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                avialabilityFragment.displayMessage(jsonResponse.getJsonMeta().getMessage());
                            }
                        }
                    }
            ) {

                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Accept", "*");
                    headers.put("Authorization", "Token token=" + MainActivity.mdriver.getToken());
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestDialogFragment.this.dismiss();

        Log.d(TAG, "rejectRide - End");
    }
}
