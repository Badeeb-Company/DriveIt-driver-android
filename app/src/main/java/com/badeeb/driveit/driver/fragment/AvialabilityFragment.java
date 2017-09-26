package com.badeeb.driveit.driver.fragment;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.activity.MainActivity;
import com.badeeb.driveit.driver.model.Trip;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.badeeb.driveit.driver.shared.FirebaseManager;
import com.badeeb.driveit.driver.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.driver.shared.PermissionsChecker;
import com.badeeb.driveit.driver.shared.UiUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import android.support.v7.app.AlertDialog;

import org.parceler.Parcels;

import java.util.List;

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;

/**
 * A simple {@link Fragment} subclass.
 */
public class AvialabilityFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = AvialabilityFragment.class.getSimpleName();
    private static final int PERM_LOCATION_RQST_CODE = 100;

    // Class Attributes
    private final int DIALOG_RESULT = 200;
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

                // Remove Driver from firebase realtime database
                mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"");
                mRef.removeValue();

                DatabaseReference locationReference = mDatabase.createChildReference("locations", "drivers",
                        String.valueOf(MainActivity.mdriver.getId()));
                locationReference.removeValue();

                // Change image to offline
                setDriverUIOffline();

                AppPreferences.isOnline = false;
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
}
