package com.badeeb.driveit.driver.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.badeeb.driveit.driver.MainActivity;
import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.model.Trip;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.badeeb.driveit.driver.shared.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.List;

import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;

/**
 * A simple {@link Fragment} subclass.
 */
public class AvialabilityFragment extends Fragment implements LocationListener, GpsStatus.Listener {

    // Logging Purpose
    public static final String TAG = AvialabilityFragment.class.getSimpleName();

    // Class Attributes
    private final int DIALOG_RESULT = 200;
    private final int LOCATION_PERMISSION = 100;
    private Location mcurrentLocation;
    private LocationManager mlocationManager;
    private RequestDialogFragment mrequestDialogFragment;
    private ValueEventListener mtripEventListener;
    private boolean paused;
    private boolean needsToShowDialog;
    private boolean needsToDismissDialog;

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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu - Start");

        super.onPrepareOptionsMenu(menu);

        MenuItem logout = menu.findItem(R.id.nav_logout);
        logout.setVisible(true);

        Log.d(TAG, "onPrepareOptionsMenu - End");
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Initiate firebase realtime - database
        this.mDatabase = new FirebaseManager();

        mlocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (AppPreferences.isOnline) {
            setDriverUIOnline(view);
        }
        else {
            setDriverUIOffline(view);
        }

        // Refresh menu toolbar
        setHasOptionsMenu(true);

        setupListeners(view);

        mcurrentLocation = getCurrentLocation();

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Location permission granted
            Log.d(TAG, "init - Request Location updates");
            mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, AppPreferences.UPDATE_TIME, AppPreferences.UPDATE_DISTANCE, this);

        }

        Log.d(TAG, "init - End");
    }

    public void setupListeners(final View view) {
        Log.d(TAG, "setupListeners - Start");

        TextView tvSetStatusOnline = view.findViewById(R.id.tvSetStatusOnline);

        tvSetStatusOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View bview) {
                Log.d(TAG, "setupListeners - tvSetStatusOnline_onClick - Start");

                setDriverOnline();

                // Change image to online
                setDriverUIOnline(view);

                AppPreferences.isOnline = true;

                Log.d(TAG, "setupListeners - tvSetStatusOnline_onClick - End");
            }
        });

        TextView tvSetStatusOffline = view.findViewById(R.id.tvSetStatusOffline);

        tvSetStatusOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View bview) {
                Log.d(TAG, "setupListeners - tvSetStatusOffline_onClick - Start");

                setDriverOffline();

                // Change image to offline
                setDriverUIOffline(view);

                AppPreferences.isOnline = false;

                Log.d(TAG, "setupListeners - tvSetStatusOffline_onClick - End");
            }
        });

        // Setup listener for GPS enable or disable
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mlocationManager.addGpsStatusListener(this);
        }

        // Set Firebase database Listener for trip
        // Create listener on firebase realtime -
        mtripEventListener = createValueEventListener();

        Log.d(TAG, "setupListeners - End");
    }

    private void setDriverUIOffline(View view) {
        Log.d(TAG, "setDriverUIOffline - Start");

        ImageView ivOffline = view.findViewById(R.id.ivOffline);
        ivOffline.setVisibility(View.VISIBLE);

        ImageView ivOnline = view.findViewById(R.id.ivOnline);
        ivOnline.setVisibility(View.GONE);

        // Change text
        TextView tvOfflineStatusText = view.findViewById(R.id.tvOfflineStatusText);
        tvOfflineStatusText.setVisibility(View.VISIBLE);

        TextView tvOnlineStatusText = view.findViewById(R.id.tvOnlineStatusText);
        tvOnlineStatusText.setVisibility(View.GONE);

        // Change Button text
        TextView tvSetStatusOffline = view.findViewById(R.id.tvSetStatusOffline);
        tvSetStatusOffline.setVisibility(View.GONE);

        TextView tvSetStatusOnline = view.findViewById(R.id.tvSetStatusOnline);
        tvSetStatusOnline.setVisibility(View.VISIBLE);

        Log.d(TAG, "setDriverUIOffline - End");
    }

    private void setDriverUIOnline(View view) {
        Log.d(TAG, "setDriverUIOnline - Start");

        ImageView ivOffline = view.findViewById(R.id.ivOffline);
        ivOffline.setVisibility(View.GONE);

        ImageView ivOnline = view.findViewById(R.id.ivOnline);
        ivOnline.setVisibility(View.VISIBLE);

        // Change text
        TextView tvOfflineStatusText = view.findViewById(R.id.tvOfflineStatusText);
        tvOfflineStatusText.setVisibility(View.GONE);

        TextView tvOnlineStatusText = view.findViewById(R.id.tvOnlineStatusText);
        tvOnlineStatusText.setVisibility(View.VISIBLE);

        // Change Button text
        TextView tvSetStatusOffline = view.findViewById(R.id.tvSetStatusOffline);
        tvSetStatusOffline.setVisibility(View.VISIBLE);

        TextView tvSetStatusOnline = view.findViewById(R.id.tvSetStatusOnline);
        tvSetStatusOnline.setVisibility(View.GONE);

        Log.d(TAG, "setDriverUIOnline - End");
    }

    private void setDriverOnline() {
        Log.d(TAG, "setDriverOnline - Start");

        // Put Driver under firebase realtime database
        mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"", "state").setValue("available");

        DatabaseReference mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"", "trip");

        // Start Listening for Firebase
        mtripEventListener = createValueEventListener();
        mRef.addValueEventListener(mtripEventListener);

        Log.d(TAG, "setDriverOnline - End");
    }


    private void setDriverOffline() {
        Log.d(TAG, "setDriverOffline - Start");

        // Stop listening
        DatabaseReference mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"", "trip");
        mRef.removeEventListener(mtripEventListener);

        // Remove Driver from firebase realtime database
        mRef = mDatabase.createChildReference("drivers", MainActivity.mdriver.getId()+"");
        mRef.removeValue();

        DatabaseReference locationReference = mDatabase.createChildReference("locations", "drivers",
                String.valueOf(MainActivity.mdriver.getId()));
        locationReference.removeValue();

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

    // Current Location Inquiry
    private Location getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation - Start");

        Location currentLocation = null;
        // Check if GPS is granted or not
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission is required
            Log.d(TAG, "getCurrentLocation - Permission is not granted");
            // Show Alert to enable location service
            Toast.makeText(getContext(), "Grant location permission to APP", Toast.LENGTH_SHORT).show();
            // TODO: Show dialog to grant permissions and reload data after that
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        } else {
            // Permission is granted now
            Log.d(TAG, "getCurrentLocation - Permission is granted");
            boolean isGPSEnabled = mlocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                List<String> providers = mlocationManager.getProviders(true);
                for (String provider : providers) {
                    mlocationManager.requestLocationUpdates(provider, 0, 0, this);
                    Location l = mlocationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (currentLocation == null || l.getAccuracy() < currentLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        currentLocation = l;
                    }
                }
                if (currentLocation == null) {
                    Log.d(TAG, "getCurrentLocation - CurrentLocation is null");
                    Toast.makeText(getContext(), getResources().getText(R.string.enable_gps), Toast.LENGTH_SHORT).show();
                }

                Log.d(TAG, "getCurrentLocation - Request Location updates");
                mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, AppPreferences.UPDATE_TIME, AppPreferences.UPDATE_DISTANCE, this);

            } else {
                showSettingsDialog();

                return null;
            }

        }

        Log.d(TAG, "getCurrentLocation - End");

        return currentLocation;
    }

    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        alertDialog.setTitle("GPS Disabled");
        alertDialog.setMessage("Please enable GPS to continue");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "You need to enable GPS first", Toast.LENGTH_LONG).show();
                showSettingsDialog();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult - Start");

        if (requestCode == LOCATION_PERMISSION) {
            Log.d(TAG, "onRequestPermissionsResult - LOCATION_PERMISSION - Start");

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult - LOCATION_PERMISSION - Permission Granted");
                getCurrentLocation();
            }

            Log.d(TAG, "onRequestPermissionsResult - LOCATION_PERMISSION - End");
        }

        Log.d(TAG, "onRequestPermissionsResult - End");
    }



    // ---------------------------------------------------------------
    // Location interface methods
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged - Start");

        mcurrentLocation = location;

        // Put firebase realtime database with current location
        DatabaseReference mRef = mDatabase.createChildReference("locations");
        mRef.child("drivers").child(MainActivity.mdriver.getId()+"").child("lat").setValue(mcurrentLocation.getLatitude());
        mRef.child("drivers").child(MainActivity.mdriver.getId()+"").child("long").setValue(mcurrentLocation.getLongitude());

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

    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "onGpsStatusChanged - Start");

        if (event == GPS_EVENT_STARTED) {
            Log.d(TAG, "onGpsStatusChanged - GPS Started");
            getCurrentLocation();
        } else if (event == GPS_EVENT_STOPPED) {
            showSettingsDialog();
        }

        Log.d(TAG, "onGpsStatusChanged - End");
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

}
