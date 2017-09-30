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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.badeeb.driveit.driver.model.Trip;
import com.badeeb.driveit.driver.model.JsonDriverStatus;
import com.badeeb.driveit.driver.network.MyVolley;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.badeeb.driveit.driver.shared.AppSettings;
import com.badeeb.driveit.driver.shared.FirebaseManager;
import com.badeeb.driveit.driver.shared.NotificationsManager;
import com.badeeb.driveit.driver.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.driver.shared.PermissionsChecker;
import com.badeeb.driveit.driver.shared.UiUtils;
import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.support.v7.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class AvailabilityFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = AvailabilityFragment.class.getSimpleName();

    // Constants
    private final int DIALOG_RESULT = 200;
    private final int PERM_LOCATION_RQST_CODE = 100;
    private final String ONLINE = "ONLINE";
    private final String OFFLINE = "OFFLINE";

    // Class Attribute
    private Location currentLocation;
    private LocationManager locationManager;
    private RequestDialogFragment mrequestDialogFragment;
    private ValueEventListener mtripEventListener;
    private boolean paused;
    private InvitationStatus invitationStatus;
    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private AlertDialog locationDisabledWarningDialog;
    private LocationChangeReceiver locationChangeReceiver;
    private ImageView ivOffline;
    private ImageView ivOnline;
    private NotificationsManager notificationsManager;
    private DatabaseReference mRefTrip;
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
        notificationsManager = NotificationsManager.getInstance();
        mactivity = (MainActivity) getActivity();
        mRefTrip = firebaseManager.createChildReference("drivers", mactivity.getDriver().getId() + "", "trip");
        mtripEventListener = createValueEventListener();
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

    @SuppressWarnings({"MissingPermission"})
    private OnPermissionsGrantedHandler createOnLocationPermissionGrantedHandler() {
        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                Log.d(TAG, "Location - onPermissionsGranted - Start");
                if (checkLocationService()) {
                    Log.d(TAG, "Location - onPermissionsGranted - Set Driver Online");
                    showGoOnlineDialog();
                }
                Log.d(TAG, "Location - onPermissionsGranted - End");
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

        // Set Firebase database Listener for trip
        // Create listener on firebase realtime -
        mtripEventListener = createValueEventListener();

        Log.d(TAG, "setupListeners - End");
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
        removeFirebaseListener();
        changeUiToOffline();
        mactivity.getDriver().setOffline();
        AppSettings appSettings = AppSettings.getInstance();
        appSettings.saveUser(mactivity.getDriver());
        mactivity.stopForegroundOnlineService();
        callOfflineApi();
        mactivity.disconnectGoogleApiClient();
    }

    private void goOnline() {
        mRefTrip.addValueEventListener(mtripEventListener);
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

    public void removeFirebaseListener() {
        mRefTrip.removeEventListener(mtripEventListener);
    }

    public void showRideRejectMessage(boolean isSuccess) {
        Log.d(TAG, "showRideRejectMessage - Start");

        if (isSuccess) {
            Toast.makeText(getContext(), getString(R.string.ride_rejected_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.ride_rejected_error), Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "showRideRejectMessage - End");
    }

    public void showRideAcceptMessage(boolean isSuccess) {
        Log.d(TAG, "showRideAcceptMessage - Start");

        if (isSuccess) {
            Toast.makeText(getContext(), getString(R.string.ride_accepted_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.ride_accepted_error), Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "showRideAcceptMessage - End");
    }

    public void displayMessage(String msg) {
        Log.d(TAG, "displayMessage - Start");

        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

        Log.d(TAG, "displayMessage - End");
    }

    private void setFirebaseDriverLocation() {
        Log.d(TAG, "setFirebaseDriverLocation - Start");

        DatabaseReference mRef = firebaseManager.createChildReference("locations");
        mRef.child("drivers").child(mactivity.getDriver().getId() + "").child("lat").setValue(currentLocation.getLatitude() + new Random().nextInt() % 5);
        mRef.child("drivers").child(mactivity.getDriver().getId() + "").child("long").setValue(currentLocation.getLongitude());

        Log.d(TAG, "setFirebaseDriverLocation - End");
    }

    // ---------------------------------------------------------------
    // Location interface methods
    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                setFirebaseDriverLocation();
            }
        };
    }

    private ValueEventListener createValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "createValueEventListener - mdatabase_onDataChange - Start");

                if (dataSnapshot.getValue() != null) {
                    Trip fdbTrip = dataSnapshot.getValue(Trip.class);
                    System.out.println("TRIP STATUS: " + fdbTrip.getState());

                    if (fdbTrip.getState().equals(AppPreferences.TRIP_PENDING)) {

                        mrequestDialogFragment = new RequestDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("trip", Parcels.wrap(fdbTrip));
                        mrequestDialogFragment.setArguments(bundle);
                        mrequestDialogFragment.setCancelable(false);

                        mrequestDialogFragment.setTargetFragment(AvailabilityFragment.this, DIALOG_RESULT);
                        showDialog();

                        sendNotification();
                    } else {
                        dismissDialog();
                    }

                }

                Log.d(TAG, "createValueEventListener - mdatabase_onDataChange - End");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void sendNotification() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        notificationsManager.createNotification(getContext(), getText(R.string.notification_title_ride).toString(),
                getText(R.string.notification_msg_ride).toString(), intent, getResources());
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        switch (invitationStatus) {
            case AVAILABLE:
                FragmentManager fragmentManager = getFragmentManager();
                mrequestDialogFragment.show(fragmentManager, mrequestDialogFragment.TAG);
                break;
            case CANCELLED:
                if (mrequestDialogFragment != null && mrequestDialogFragment.isVisible()) {
                    mrequestDialogFragment.dismiss();
                }
                break;
        }
        invitationStatus = InvitationStatus.NONE;
    }

    private void showDialog() {
        if (paused) {
            invitationStatus = InvitationStatus.AVAILABLE;
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            mrequestDialogFragment.show(fragmentManager, mrequestDialogFragment.TAG);
        }
    }

    private void dismissDialog() {
        if (paused) {
            invitationStatus = InvitationStatus.CANCELLED;
        } else if (mrequestDialogFragment != null && mrequestDialogFragment.isVisible()) {
            mrequestDialogFragment.dismiss();
        }
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
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonObject,

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
                                // Success

                            } else {
                                // Failure
                            }

                            Log.d(TAG, "callOnlineApi - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "callOnlineApi - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
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
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonObject,

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
                                // Success

                            } else {
                                // Failure
                            }

                            Log.d(TAG, "callOfflineApi - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "callOfflineApi - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
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

}
