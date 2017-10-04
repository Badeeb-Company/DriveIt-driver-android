package com.badeeb.driveit.driver.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.activity.MainActivity;
import com.badeeb.driveit.driver.model.JsonRequestTrip;
import com.badeeb.driveit.driver.model.Trip;
import com.badeeb.driveit.driver.network.MyVolley;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.badeeb.driveit.driver.shared.AppSettings;
import com.badeeb.driveit.driver.shared.UiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

import static com.badeeb.driveit.driver.R.id.tvName;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestDialogFragment extends DialogFragment {

    // Logging Purpose
    public static final String TAG = RequestDialogFragment.class.getSimpleName();

    // Class Attributes
    private Trip mtrip;
    private MainActivity mactivity;
    private FragmentManager fragmentManager;

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
        mactivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        // Publish values into dialog
        TextView tvName = view.findViewById(R.id.tvName);
        tvName.setText(mtrip.getClient_name());

        TextView tvLocation = view.findViewById(R.id.tvLocation);
        tvLocation.setText(mtrip.getClient_address());

        TextView tvTimeToArrive = view.findViewById(R.id.tvTimeToArrive);
        tvTimeToArrive.setText((int)(mtrip.getTime_to_arrive()/60) + " minutes");

        TextView tvDistanceToArrive = view.findViewById(R.id.tvDistanceToArrive);
        tvDistanceToArrive.setText(mtrip.getDistance_to_arrive()/1000 + " kilometers");

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
                acceptRide();
            }
        });

        Button bReject = view.findViewById(R.id.bReject);
        bReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectRide();
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void acceptRide() {
        Log.d(TAG, "acceptRide - Start");

        String url = AppPreferences.BASE_URL + "/trip" + "/" + mtrip.getId() + "/accept ";

        try {

            JsonRequestTrip request = new JsonRequestTrip();
            request.setTripId(mtrip.getId());

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "acceptRide - Json Request" + gson.toJson(request));

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
                                toast(R.string.ride_accepted_success);
                                mactivity.removeFirebaseListener();
                                mactivity.getDriver().setInTrip();
                                AppSettings settings = AppSettings.getInstance();
                                settings.saveUser(mactivity.getDriver());
                                settings.saveTrip(mtrip);

                                gotToTripDetailsFragment();

                            } else if(jsonResponse.getJsonMeta().getStatus().equals("422")){
                                toast("Trip was cancelled by client");
                            } else {
                                toast(R.string.ride_accepted_error);
                            }


                            Log.d(TAG, "acceptRide - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "acceptRide - onErrorResponse: " + error.toString());

                            if (error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.login_error, R.string.ok_btn_dialog, null);

                                goToLogin();

                            } else if (error instanceof ServerError) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                Log.d(TAG, "acceptRide - Error Data: " + responseData);

                                JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                                Log.d(TAG, "acceptRide - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "acceptRide - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                toast(jsonResponse.getJsonMeta().getMessage());
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
                    headers.put("Authorization", "Token token=" + mactivity.getDriver().getToken());
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

        try {

            JsonRequestTrip request = new JsonRequestTrip();
            request.setTripId(mtrip.getId());

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "rejectRide - Json Request" + gson.toJson(request));

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
                                toast(R.string.ride_rejected_success);
                            } else {
                                toast(R.string.ride_rejected_error);
                            }

                            Log.d(TAG, "rejectRide - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "rejectRide - onErrorResponse: " + error.toString());

                            if (error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.login_error, R.string.ok_btn_dialog, null);

                                goToLogin();

                            } else if (error instanceof ServerError) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                Log.d(TAG, "rejectRide - Error Data: " + responseData);

                                JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                                Log.d(TAG, "rejectRide - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "rejectRide - Error Message: " + jsonResponse.getJsonMeta().getMessage());
                                toast(jsonResponse.getJsonMeta().getMessage());
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
                    headers.put("Authorization", "Token token=" + mactivity.getDriver().getToken());
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

    private void toast(int messageId) {
        if (mactivity != null && isAdded()) {
            toast(getString(messageId));
        }
    }

    private void toast(String message) {
        if (mactivity != null && isAdded()){
            Toast.makeText(mactivity, message, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_corner_white));
        }
    }

    private void gotToTripDetailsFragment() {
        TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("trip", Parcels.wrap(mtrip));
        tripDetailsFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, tripDetailsFragment, tripDetailsFragment.TAG);
        fragmentTransaction.commit();
    }

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }
}
