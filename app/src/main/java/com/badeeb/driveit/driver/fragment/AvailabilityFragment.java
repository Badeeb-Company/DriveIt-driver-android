package com.badeeb.driveit.driver.fragment;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.badeeb.driveit.driver.model.JsonDriverStatus;
import com.badeeb.driveit.driver.network.MyVolley;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.badeeb.driveit.driver.shared.AppSettings;
import com.badeeb.driveit.driver.shared.FirebaseManager;
import com.badeeb.driveit.driver.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.driver.shared.PermissionsChecker;
import com.badeeb.driveit.driver.shared.UiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.support.v7.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AvailabilityFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = AvailabilityFragment.class.getSimpleName();

    // Constants
    private final int PERM_LOCATION_RQST_CODE = 100;
    private final String ONLINE = "ONLINE";
    private final String OFFLINE = "OFFLINE";

    private ImageView ivOffline;
    private ImageView ivOnline;

    private Location currentLocation;
    private LocationManager locationManager;
    private boolean paused;
    private InvitationStatus invitationStatus;
    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private AlertDialog locationDisabledWarningDialog;
    private LocationChangeReceiver locationChangeReceiver;

    private AppSettings appSettings;

    // Firebase database reference
    private FirebaseManager firebaseManager;
    private MainActivity mactivity;

    public AvailabilityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        View view = inflater.inflate(R.layout.fragment_avialability, container, false);
        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        firebaseManager = new FirebaseManager();
        ivOffline = view.findViewById(R.id.ivOffline);
        ivOnline = view.findViewById(R.id.ivOnline);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        mactivity = (MainActivity) getActivity();
        onLocationPermissionGrantedHandler = createOnLocationPermissionGrantedHandler();
        locationChangeReceiver = new LocationChangeReceiver();
        appSettings = AppSettings.getInstance();
        invitationStatus = InvitationStatus.NONE;

        mactivity.enbleNavigationView();

        setupListeners();


        if (mactivity.getDriver().isOnline()) {
            goOnline();
        } else {
            changeUiToOffline();
        }

        Log.d(TAG, "init - End");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private OnPermissionsGrantedHandler createOnLocationPermissionGrantedHandler() {
        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                if (checkLocationService()) {
                    Log.d(TAG, "Location - onPermissionsGranted - Set Driver Online");
                    showGoOnlineDialog();
                }
            }
        };
    }

    private boolean checkLocationService() {
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            if (locationDisabledWarningDialog == null || !locationDisabledWarningDialog.isShowing()) {
                showGPSDisabledWarningDialog();
                getActivity().registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } else {
            if (locationDisabledWarningDialog != null && locationDisabledWarningDialog.isShowing()) {
                locationDisabledWarningDialog.dismiss();
            }
        }
        return gpsEnabled;
    }

    private void showGPSDisabledWarningDialog() {

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        locationDisabledWarningDialog = UiUtils.showDialog(getContext(), R.style.DialogTheme,
                R.string.GPS_disabled_warning_title, R.string.GPS_disabled_warning_msg,
                R.string.ok_btn_dialog, positiveListener);

    }


    public void setupListeners() {
        Log.d(TAG, "setupListeners - Start");

        ivOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View cview) {
                Log.d(TAG, "setupListeners - ivOffline_onClick - Start");

                // Check if location permission is granted or not
                PermissionsChecker.checkPermissions(AvailabilityFragment.this, onLocationPermissionGrantedHandler,
                        PERM_LOCATION_RQST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);


                Log.d(TAG, "setupListeners - ivOffline_onClick - End");
            }
        });

        ivOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View cview) {
                Log.d(TAG, "setupListeners - ivOnline_onClick - Start");

                showGoOfflineDialog();

                Log.d(TAG, "setupListeners - ivOnline_onClick - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERM_LOCATION_RQST_CODE:
                if(PermissionsChecker.permissionsGranted(grantResults)){
                    onLocationPermissionGrantedHandler.onPermissionsGranted();
                }
                break;
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void showGoOnlineDialog() {
        Log.d(TAG, "showGoOnlineDialog - Start");

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goOnline();
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO
            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.online_msg, R.string.online_des_msg,
                R.string.yes_msg, positiveListener, R.string.no_msg, negativeListener);


        Log.d(TAG, "showGoOnlineDialog - End");
    }

    private void showGoOfflineDialog() {
        Log.d(TAG, "showGoOfflineDialog - Start");
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goOffline();
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO
            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.offline_msg, R.string.offline_des_msg,
                R.string.yes_msg, positiveListener, R.string.no_msg, negativeListener);


        Log.d(TAG, "showGoOfflineDialog - End");
    }

    private void goOffline() {
        mactivity.removeFirebaseListener();
        changeUiToOffline();
        mactivity.getDriver().setOffline();
        AppSettings appSettings = AppSettings.getInstance();
        appSettings.saveUser(mactivity.getDriver());
        mactivity.stopForegroundOnlineService();
        callOfflineApi();
        mactivity.disconnectGoogleApiClient();
    }

    private void goOnline() {
        mactivity.addFirebaseListener();
        changeUiToOnline();
        mactivity.getDriver().setOnline();
        appSettings.saveUser(mactivity.getDriver());
        mactivity.startForegroundOnlineService();
        callOnlineApi();
        mactivity.connectGoogleApiClient();
    }

    private void changeUiToOffline() {
        ivOffline.setVisibility(View.VISIBLE);
        ivOnline.setVisibility(View.GONE);
    }

    private void changeUiToOnline() {
        ivOffline.setVisibility(View.GONE);
        ivOnline.setVisibility(View.VISIBLE);
    }

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "LocationChangeReceiver - onReceive - Start");
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                checkLocationService();
                getActivity().unregisterReceiver(this);
            }
            Log.d(TAG, "LocationChangeReceiver - onReceive - End");
        }
    }

    private void callOnlineApi() {
        Log.d(TAG, "callOnlineApi - Start");
        try {
            String url = AppPreferences.BASE_URL + "/driver";

            JsonDriverStatus request = new JsonDriverStatus();
            request.setAvilability(ONLINE);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "callOnlineApi - Json Request" + gson.toJson(request));

            // Call user login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "callOnlineApi - onResponse - Start");

                            Log.d(TAG, "callOnlineApi - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                            Log.d(TAG, "callOnlineApi - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "callOnlineApi - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                Toast.makeText(getContext(), "You are now online", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Something wrong happened, please try again", Toast.LENGTH_SHORT).show();
                            }

                            Log.d(TAG, "callOnlineApi - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "callOnlineApi - onErrorResponse: " + error.toString());

                            if (error instanceof AuthFailureError && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(getContext(), R.style.DialogTheme,
                                        R.string.login_error, R.string.ok_btn_dialog, null);
                                goToLogin();

                            } else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                                Log.d(TAG, "callOnlineApi - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "callOnlineApi - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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
                    headers.put("Authorization", "Token token=" + mactivity.getDriver().getToken());

                    Log.d(TAG, "callOnlineApi - Json Header - " + "Token token=" + mactivity.getDriver().getToken());
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "callOnlineApi - End");
    }

    private void callOfflineApi() {

        Log.d(TAG, "callOfflineApi - Start");

        try {

            String url = AppPreferences.BASE_URL + "/driver";

            JsonDriverStatus request = new JsonDriverStatus();
            request.setAvilability(OFFLINE);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "callOfflineApi - Json Request" + gson.toJson(request));

            // Call offline service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "callOfflineApi - onResponse - Start");

                            Log.d(TAG, "callOfflineApi - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                            Log.d(TAG, "callOfflineApi - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "callOfflineApi - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                Toast.makeText(getContext(), "You are now offline", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Something wrong happened, please try again", Toast.LENGTH_SHORT).show();
                            }

                            Log.d(TAG, "callOfflineApi - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "callOfflineApi - onErrorResponse: " + error.toString());

                            if (error instanceof AuthFailureError && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(getContext(), R.style.DialogTheme,
                                        R.string.login_error, R.string.ok_btn_dialog, null);
                                goToLogin();

                            } else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                                Log.d(TAG, "callOfflineApi - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "callOfflineApi - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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
                    headers.put("Authorization", "Token token=" + mactivity.getDriver().getToken());

                    Log.d(TAG, "callOfflineApi - Json Header - " + "Token token=" + mactivity.getDriver().getToken());

                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "callOfflineApi - End");
    }

    private enum InvitationStatus {NONE, AVAILABLE, CANCELLED}

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }

}
