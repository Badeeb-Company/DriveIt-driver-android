package com.badeeb.driveit.driver.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripDetailsFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = TripDetailsFragment.class.getSimpleName();

    // Class Attributes
    private Trip mtrip;
    private AppSettings settings;

    public TripDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        this.mtrip = Parcels.unwrap(getArguments().getParcelable("trip"));

        // Initialize text fields with their correct values
        RoundedImageView driverPhoto = view.findViewById(R.id.iProfileImage);
        TextView tDriverName = view.findViewById(R.id.tDriverName);
        TextView tDriverPhone = view.findViewById(R.id.tDriverPhone);
        TextView tTimeToArrive = view.findViewById(R.id.tTimeToArrive);
        TextView tDriverDistance = view.findViewById(R.id.tDriverDistance);

        Glide.with(getContext())
                .load(mtrip.getClient_image_url())
                .placeholder(R.drawable.def_usr_img)
                .into(driverPhoto);

        tDriverName.setText(mtrip.getClient_name());
        tDriverPhone.setText(mtrip.getClient_phone());
        tTimeToArrive.setText((int)(mtrip.getTime_to_arrive()/60) + " minutes");
        tDriverDistance.setText(mtrip.getDistance_to_arrive()/1000 + " kilometers");

        // Setup Listeners
        setupListeners(view);

        settings = AppSettings.getInstance();

        // Refresh menu toolbar
        ((MainActivity) getActivity()).enbleNavigationView();

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        Button bCall = view.findViewById(R.id.bCall);

        bCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - bCall_setOnClickListener - Start");

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String mobileNumber = String.valueOf(mtrip.getClient_phone());
                callIntent.setData(Uri.parse("tel:" + mobileNumber));

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // Show Alert to enable location service
                    Toast.makeText(getContext(), getContext().getResources().getText(R.string.enable_phone_call), Toast.LENGTH_SHORT).show();

                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 0);
                }else {
                    getContext().startActivity(callIntent);
                }

                Log.d(TAG, "setupListeners - bCall_setOnClickListener - End");
            }
        });

        TextView tvRestart = view.findViewById(R.id.tvRestart);
        tvRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripComplete();
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void tripComplete() {
        String url = AppPreferences.BASE_URL + "/trip/" + mtrip.getId() + "/complete";

        Log.d(TAG, "tripComplete - Start");

        try {

            JsonRequestTrip request = new JsonRequestTrip();
            request.setTripId(mtrip.getId());

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "tripComplete - Json Request"+ gson.toJson(request));

            // Call user tripComplete service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "tripComplete - onResponse - Start");

                            Log.d(TAG, "tripComplete - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                            Log.d(TAG, "tripComplete - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "tripComplete - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success tripComplete

                                ((MainActivity) getActivity()).getDriver().setAvailable();
                                settings.saveUser(((MainActivity) getActivity()).getDriver());
                                settings.clearTripInfo();

                                goToAvailabilityFragment();


                            }
                            else {
                                // Invalid tripComplete
                                Toast.makeText(getContext(), getString(R.string.tripComplete_error), Toast.LENGTH_SHORT).show();
                            }

                            Log.d(TAG, "tripComplete - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "tripComplete - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                                Log.d(TAG, "tripComplete - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "tripComplete - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                Toast.makeText(getContext(), jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
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
                    headers.put("Authorization", "Token token=" + ((MainActivity) getActivity()).getDriver().getToken());
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "tripComplete - End");

    }

    private void goToAvailabilityFragment() {
        AvailabilityFragment availabilityFragment = new AvailabilityFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, availabilityFragment, availabilityFragment.TAG);
//                                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

}
