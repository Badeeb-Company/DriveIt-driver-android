package com.badeeb.driveit.driver.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.badeeb.driveit.driver.shared.UiUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.badeeb.driveit.driver.R.id.tvRestart;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripDetailsFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = TripDetailsFragment.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1221;

    // Class Attributes
    private Trip mtrip;
    private AppSettings settings;
    private MainActivity mactivity;

    private TextView tvDeliveryDistance;
    private TextView tvDeliveryPrice;

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
        mactivity = (MainActivity) getActivity();

        // Initialize text fields with their correct values
        RoundedImageView driverPhoto = view.findViewById(R.id.iProfileImage);
        TextView tDriverName = view.findViewById(R.id.tDriverName);
        TextView tDriverPhone = view.findViewById(R.id.tDriverPhone);
        TextView tTimeToArrive = view.findViewById(R.id.tTimeToArrive);
        TextView tDriverDistance = view.findViewById(R.id.tDriverDistance);

        tvDeliveryDistance = view.findViewById(R.id.tvDeliveryDistance);
        tvDeliveryPrice = view.findViewById(R.id.tvDeliveryPrice);

        Glide.with(getContext())
                .load(mtrip.getClient_image_url())
                .into(driverPhoto);

        tDriverName.setText(mtrip.getClient_name());
        tDriverPhone.setText(mtrip.getClient_phone());
        tTimeToArrive.setText((int) (mtrip.getTime_to_arrive() / 60) + " min");
        tDriverDistance.setText(mtrip.getDistance_to_arrive() / 1000 + " km");

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
                } else {
                    getContext().startActivity(callIntent);
                }

                Log.d(TAG, "setupListeners - bCall_setOnClickListener - End");
            }
        });

        Button bCalculateDeliveryPrice = view.findViewById(R.id.bCalculateDeliveryPrice);
        bCalculateDeliveryPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(mactivity), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Toast.makeText(mactivity, "Please update google play services", Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(mactivity, "Please update google play services", Toast.LENGTH_SHORT).show();
                }

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, mactivity);
                if(place != null && place.getLatLng() != null) {
                    float[] distanceArray = new float[1];
                    Location.distanceBetween(mactivity.getCurrentLocation().getLatitude(),
                            mactivity.getCurrentLocation().getLongitude(),
                            place.getLatLng().latitude, place.getLatLng().longitude, distanceArray);
                    double distanceInKm = Math.floor(distanceArray[0]) / 1000;
                    double price = calculatePrice(distanceInKm);

                    tvDeliveryDistance.setText(String.valueOf(distanceInKm) + " km");
                    tvDeliveryPrice.setText(String.valueOf(price) + "$");

                } else {
                    Toast.makeText(mactivity, "Error while finding location, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private double calculatePrice(double distance){
        if(distance < 1.5){
            return 2.5;
        } else {
            return 2.5 + 1 * Math.ceil(distance - 1.5);
        }
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

            final ProgressDialog progressDialog = UiUtils.createProgressDialog(mactivity);
            progressDialog.show();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "tripComplete - Json Request" + gson.toJson(request));

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

                            progressDialog.dismiss();

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success tripComplete

                                ((MainActivity) getActivity()).getDriver().setAvailable();
                                settings.saveUser(((MainActivity) getActivity()).getDriver());
                                settings.clearTripInfo();

                                goToAvailabilityFragment();


                            } else {
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
                            progressDialog.dismiss();

                            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                deactivateAccount();

                            } else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
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
        fragmentTransaction.replace(R.id.main_frame, availabilityFragment, availabilityFragment.TAG);
//                                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
    }

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }

    private void deactivateAccount() {
        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.account_not_active, R.string.ok_btn_dialog, null);
        mactivity.stopForegroundOnlineService();
        mactivity.removeFirebaseListener();
        mactivity.disconnectGoogleApiClient();
        settings.clearTripInfo();
        settings.clearUserInfo();
        goToLogin();
    }

}
