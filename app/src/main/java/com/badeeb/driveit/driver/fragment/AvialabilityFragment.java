package com.badeeb.driveit.driver.fragment;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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
import com.badeeb.driveit.driver.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.driver.shared.PermissionsChecker;
import com.badeeb.driveit.driver.shared.UiUtils;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class AvialabilityFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = AvialabilityFragment.class.getSimpleName();

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
    private boolean needsToShowDialog;
    private boolean needsToDismissDialog;
    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private AlertDialog locationDisabledWarningDialog;
    private LocationChangeReceiver locationChangeReceiver;
    private ImageView ivOffline;
    private ImageView ivOnline;
    private LocationListener locationListener;

    // Firebase database reference
    private FirebaseManager mDatabase;

    public AvialabilityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_avialability, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Initiate firebase realtime - database
        this.mDatabase = new FirebaseManager();
        ivOffline = view.findViewById(R.id.ivOffline);
        ivOnline = view.findViewById(R.id.ivOnline);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = createLocationListener();

        if (AppPreferences.isOnline) {
            setDriverUIOnline();
        }
        else {
            setDriverUIOffline();
        }

        // Refresh menu toolbar
        ((MainActivity) getActivity()).enbleNavigationView();

        setupListeners();

        onLocationPermissionGrantedHandler = createOnLocationPermissionGrantedHandler();
        locationChangeReceiver = new LocationChangeReceiver();

        if (MainActivity.mdriver.getState().equals(AppPreferences.ONLINE)) {
            setDriverOnline();
        }

        Log.d(TAG, "init - End");
    }

    @SuppressWarnings({"MissingPermission"})
    private OnPermissionsGrantedHandler createOnLocationPermissionGrantedHandler() {
        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                Log.d(TAG, "Location - onPermissionsGranted - Start");
                if(checkLocationService()) {
                    Log.d(TAG, "Location - onPermissionsGranted - Set Driver Online");
                    setDriverOnline();
                    // Get current location
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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
        }
        else {
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
                PermissionsChecker.checkPermissions(AvialabilityFragment.this, onLocationPermissionGrantedHandler,
                        PERM_LOCATION_RQST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);


                Log.d(TAG, "setupListeners - ivOffline_onClick - End");
            }
        });

        ivOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View cview) {
                Log.d(TAG, "setupListeners - ivOnline_onClick - Start");

                setDriverOffline();

                Log.d(TAG, "setupListeners - ivOnline_onClick - End");
            }
        });

        // Set Firebase database Listener for trip
        // Create listener on firebase realtime -
        mtripEventListener = createValueEventListener();

        Log.d(TAG, "setupListeners - End");
    }

    private void setDriverUIOffline() {
        Log.d(TAG, "setDriverUIOffline - Start");

        ivOffline.setVisibility(View.VISIBLE);

        ivOnline.setVisibility(View.GONE);

        Log.d(TAG, "setDriverUIOffline - End");
    }

    private void setDriverUIOnline() {
        Log.d(TAG, "setDriverUIOnline - Start");

        ivOffline.setVisibility(View.GONE);

        ivOnline.setVisibility(View.VISIBLE);

        Log.d(TAG, "setDriverUIOnline - End");
    }

    @SuppressWarnings({"MissingPermission"})
    private void setDriverOnline() {
        Log.d(TAG, "setDriverOnline - Start");

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Put Driver under firebase realtime database
                mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"", "state").setValue("available");

                DatabaseReference mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"", "trip");

                // Start Listening for Firebase
                mtripEventListener = createValueEventListener();
                mRef.addValueEventListener(mtripEventListener);

                // Change image to offline
                setDriverUIOnline();

                AppPreferences.isOnline = true;
                MainActivity.mdriver.setState(AppPreferences.ONLINE);
                AppSettings appSettings = AppSettings.getInstance();
                appSettings.saveUser(MainActivity.mdriver);

                // call online endpoint
                onlineEndpoint();

                if (currentLocation != null) {
                    setFirebaseDriverLocation();
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, AppPreferences.UPDATE_TIME, AppPreferences.UPDATE_DISTANCE, locationListener);
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.online_msg, R.string.online_des_msg,
                R.string.yes_msg, positiveListener, R.string.no_msg, negativeListener);


        Log.d(TAG, "setDriverOnline - End");
    }


    private void setDriverOffline() {
        Log.d(TAG, "setDriverOffline - Start");

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // Stop listening
                DatabaseReference mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"", "trip");
                mRef.removeEventListener(mtripEventListener);

                // Remove D river from firebase realtime database
                mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"");
                mRef.removeValue();

                DatabaseReference locationReference = mDatabase.createChildReference("locations", "drivers",
                        String.valueOf(MainActivity.mdriver.getId()));
                locationReference.removeValue();

                // Change image to offline
                setDriverUIOffline();

                AppPreferences.isOnline = false;
                MainActivity.mdriver.setState(AppPreferences.LOGGED_IN);
                AppSettings appSettings = AppSettings.getInstance();
                appSettings.saveUser(MainActivity.mdriver);

                // call offline endpoint
                offlineEndpoint();
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.offline_msg, R.string.offline_des_msg,
                R.string.yes_msg, positiveListener, R.string.no_msg, negativeListener);


        Log.d(TAG, "setDriverOffline - End");
    }

    public void showRideRejectMessage(boolean isSuccess) {
        Log.d(TAG, "showRideRejectMessage - Start");

        if (isSuccess) {
            Toast.makeText(getContext(), getString(R.string.ride_rejected_success), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), getString(R.string.ride_rejected_error), Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "showRideRejectMessage - End");
    }

    public void showRideAcceptMessage(boolean isSuccess) {
        Log.d(TAG, "showRideAcceptMessage - Start");

        if (isSuccess) {
            Toast.makeText(getContext(), getString(R.string.ride_accepted_success), Toast.LENGTH_SHORT).show();
        }
        else {
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

        DatabaseReference mRef = mDatabase.createChildReference("locations");
        mRef.child("drivers").child(MainActivity.mdriver.getId()+"").child("lat").setValue(currentLocation.getLatitude());
        mRef.child("drivers").child(MainActivity.mdriver.getId()+"").child("long").setValue(currentLocation.getLongitude());

        Log.d(TAG, "setFirebaseDriverLocation - End");
    }

    // ---------------------------------------------------------------
    // Location interface methods
    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged - Start");

                currentLocation = location;

                // Put firebase realtime database with current location
                setFirebaseDriverLocation();

                Log.d(TAG, "onLocationChanged - End");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

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

                    if (fdbTrip.getState().equals(AppPreferences.TRIP_PENDING)) {

                        mrequestDialogFragment = new RequestDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("trip", Parcels.wrap(fdbTrip));
                        mrequestDialogFragment.setArguments(bundle);
                        mrequestDialogFragment.setCancelable(false);

                        mrequestDialogFragment.setTargetFragment(AvialabilityFragment.this, DIALOG_RESULT);
                        showDialog();
                    }
                    else {
                        if (mrequestDialogFragment != null && mrequestDialogFragment.isVisible()) {
                            // close it
                            dismissDialog();
                        }

                    }

                }

                Log.d(TAG, "createValueEventListener - mdatabase_onDataChange - End");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
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
        if(needsToShowDialog){
            FragmentManager fragmentManager = getFragmentManager();
            mrequestDialogFragment.show(fragmentManager, mrequestDialogFragment.TAG);
            needsToShowDialog = false;
        }
        if(needsToDismissDialog){
            mrequestDialogFragment.dismiss();
        }
    }

    private void showDialog(){
        if(paused){
            needsToShowDialog = true;
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            mrequestDialogFragment.show(fragmentManager, mrequestDialogFragment.TAG);
        }
    }

    private void dismissDialog(){
        if(paused){
            needsToDismissDialog = true;
        } else {
            mrequestDialogFragment.dismiss();
        }
    }

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "LocationChangeReceiver - onReceive - Start");
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
                checkLocationService();
                getActivity().unregisterReceiver(this);
            }
            Log.d(TAG, "LocationChangeReceiver - onReceive - End");
        }
    }

    private void onlineEndpoint() {

        Log.d(TAG, "onlineEndpoint - Start");

        try {

            String url = AppPreferences.BASE_URL + "/driver";

            JsonDriverStatus request = new JsonDriverStatus();
            request.setAvilability(ONLINE);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "onlineEndpoint - Json Request"+ gson.toJson(request));

            // Call user login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "onlineEndpoint - onResponse - Start");

                            Log.d(TAG, "onlineEndpoint - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                            Log.d(TAG, "onlineEndpoint - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "onlineEndpoint - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success

                            }
                            else {
                                // Failure
                            }

                            Log.d(TAG, "onlineEndpoint - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "onlineEndpoint - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                                Log.d(TAG, "onlineEndpoint - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "onlineEndpoint - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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
                    headers.put("Authorization", "Token token=" + MainActivity.mdriver.getToken());

                    Log.d(TAG, "onlineEndpoint - Json Header - "+ "Token token=" + MainActivity.mdriver.getToken());
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onlineEndpoint - End");
    }

    private void offlineEndpoint() {

        Log.d(TAG, "offlineEndpoint - Start");

        try {

            String url = AppPreferences.BASE_URL + "/driver";

            JsonDriverStatus request = new JsonDriverStatus();
            request.setAvilability(OFFLINE);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "offlineEndpoint - Json Request"+ gson.toJson(request));

            // Call offline service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "offlineEndpoint - onResponse - Start");

                            Log.d(TAG, "offlineEndpoint - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                            Log.d(TAG, "offlineEndpoint - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "offlineEndpoint - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success

                            }
                            else {
                                // Failure
                            }

                            Log.d(TAG, "offlineEndpoint - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "offlineEndpoint - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonDriverStatus jsonResponse = gson.fromJson(responseData, JsonDriverStatus.class);

                                Log.d(TAG, "offlineEndpoint - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "offlineEndpoint - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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
                    headers.put("Authorization", "Token token=" + MainActivity.mdriver.getToken());

                    Log.d(TAG, "offlineEndpoint - Json Header - "+ "Token token=" + MainActivity.mdriver.getToken());

                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "offlineEndpoint - End");
    }

}
