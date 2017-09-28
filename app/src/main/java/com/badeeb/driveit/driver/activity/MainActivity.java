package com.badeeb.driveit.driver.activity;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import com.badeeb.driveit.driver.fragment.AvailabilityFragment;
import com.badeeb.driveit.driver.fragment.LoginFragment;
import com.badeeb.driveit.driver.fragment.TripDetailsFragment;
import com.badeeb.driveit.driver.model.JsonLogout;
import com.badeeb.driveit.driver.model.User;
import com.badeeb.driveit.driver.network.MyVolley;
import com.badeeb.driveit.driver.shared.AppPreferences;
import com.badeeb.driveit.driver.shared.AppSettings;
import com.badeeb.driveit.driver.shared.FirebaseManager;
import com.badeeb.driveit.driver.shared.UiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Logging Purpose
    private final String TAG = MainActivity.class.getSimpleName();

    // Class attributes
    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private DrawerLayout mdrawer;
    private ActionBarDrawerToggle mtoggle;
    private NavigationView mnavigationView;
    private AppSettings msettings;

    private User mdriver;
    private GoogleApiClient mGoogleApiClient;
    private LocationListener locationListener;
    private FirebaseManager firebaseManager;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        init();

        Log.d(TAG, "onCreate - End");
    }

    private void init() {
        Log.d(TAG, "init - Start");

        // Initialize Attributes
        mFragmentManager = getSupportFragmentManager();
        msettings = AppSettings.getInstance();
        locationListener = createLocationListener();
        firebaseManager = new FirebaseManager();
        initGoogleApiClient();


        // Toolbar
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);


        mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mtoggle = new ActionBarDrawerToggle(
                this, mdrawer, mtoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mdrawer.setDrawerListener(mtoggle);
        mtoggle.syncState();

        mnavigationView = (NavigationView) findViewById(R.id.nav_view);
        mnavigationView.setNavigationItemSelectedListener(this);

        // Load Login Fragment inside Main activity
        if(msettings.isLoggedIn()){
            mdriver = msettings.getUser();
            if(mdriver.isOnline()){
                connectGoogleApiClient();
                if(mdriver.isInTrip()){
                    gotToTripDetailsFragment();
                } else {
                    goToAvialabilityFragment();
                }
            } else {
                goToAvialabilityFragment();
            }
        } else {
            goToLogin();
        }

        Log.d(TAG, "init - End");
    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        registerLocationUpdate();
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(MainActivity.this, "API client connection suspended", Toast.LENGTH_LONG).show();
                    }

                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "API client connection failed", Toast.LENGTH_LONG).show();
                    }
                })
                .build();
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                setFirebaseDriverLocation();
            }
        };
    }

    private void setFirebaseDriverLocation() {
        Log.d(TAG, "setFirebaseDriverLocation - Start");

        DatabaseReference mRef = firebaseManager.createChildReference("locations");
        mRef.child("drivers").child(mdriver.getId()+"").child("lat").setValue(currentLocation.getLatitude() + new Random().nextInt()%5 * 0.0000001);
        mRef.child("drivers").child(mdriver.getId()+"").child("long").setValue(currentLocation.getLongitude());

        Log.d(TAG, "setFirebaseDriverLocation - End");
    }

    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationUpdate() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(AppPreferences.UPDATE_DISTANCE);
        request.setInterval(AppPreferences.UPDATE_TIME);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, locationListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Log.d(TAG, "onNavigationItemSelected - Start");

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            // Handle the logout action
            Log.d(TAG, "onNavigationItemSelected - Logout - Start");

            if (mdriver.isInTrip()) {
                UiUtils.showDialog(this, R.style.DialogTheme,
                        R.string.logout_error, R.string.ok_btn_dialog, null);
            }
            else {
                msettings.clearUserInfo();
                disconnectGoogleApiClient();
                // TODO remove trip listener
                logout();
                goToLogin();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Log.d(TAG, "onNavigationItemSelected - End");

        return true;
    }

    private void gotToTripDetailsFragment() {
        TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("trip", Parcels.wrap(msettings.getTrip()));
        tripDetailsFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, tripDetailsFragment, tripDetailsFragment.TAG);
        fragmentTransaction.commit();
    }

    private void goToAvialabilityFragment() {
        AvailabilityFragment availabilityFragment = new AvailabilityFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, availabilityFragment, availabilityFragment.TAG);
        fragmentTransaction.commit();
    }

    private void goToLogin(){
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }

    public void disbleNavigationView() {
        mdrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mtoggle.setDrawerIndicatorEnabled(false);
    }

    public void enbleNavigationView() {
        mdrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mtoggle.setDrawerIndicatorEnabled(true);
    }

    public void setDriver(User driver){
       mdriver = driver;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public User getDriver(){
        return mdriver;
    }

    private void logout() {
        Log.d(TAG, "logout - Start");

        String url = AppPreferences.BASE_URL + "/logout";

        try {

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            // Call user login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "logout - onResponse - Start");

                            Log.d(TAG, "logout - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonLogout jsonResponse = gson.fromJson(responseData, JsonLogout.class);

                            Log.d(TAG, "logout - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "logout - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            // Success login
                            // Clear callback stack
//                            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                            // Move to next screen --> Login fragment
//                            goToLogin();



                            Log.d(TAG, "logout - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "logout - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonLogout jsonResponse = gson.fromJson(responseData, JsonLogout.class);

                                Log.d(TAG, "logout - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "logout - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                Toast.makeText(getApplicationContext(), jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
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
                    headers.put("Authorization", "Token token=" + mdriver.getToken());

                    Log.d(TAG, "logout - getHeaders_Authorization: " + "Token token=" + mdriver.getToken());

                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(this).addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "logout - End");
    }

    public void connectGoogleApiClient() {
        if(mGoogleApiClient != null && !mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    public void disconnectGoogleApiClient(){
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }
}
