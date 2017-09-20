package com.badeeb.driveit.driver.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.driveit.driver.MainActivity;
import com.badeeb.driveit.driver.R;
import com.badeeb.driveit.driver.model.JsonLogin;
import com.badeeb.driveit.driver.model.User;
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
public class LoginFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = LoginFragment.class.getSimpleName();

    // Class Attributes
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private Toolbar mToolbar;

    // attributes that will be used for JSON calls
    private String url = AppPreferences.BASE_URL + "/driver/login";

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu - Start");

        super.onPrepareOptionsMenu(menu);

        MenuItem logout = menu.findItem(R.id.nav_logout);
        logout.setVisible(false);

        Log.d(TAG, "onPrepareOptionsMenu - End");
    }


    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Attributes Initialization
        MainActivity.mdriver = new User();
        // Email
        this.mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email);
        // Password
        this.mPasswordView = (EditText) view.findViewById(R.id.password);
        // Progress bar
        this.mProgressView = view.findViewById(R.id.progressBar);

        // Setup listeners
        setupListeners(view);

        // Refresh menu toolbar
        setHasOptionsMenu(true);

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        // Sign_In button listener
        Button signIn = (Button) view.findViewById(R.id.email_sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - signIn_onclick - Start");

                // Enable Progress bar
                mProgressView.setVisibility(View.VISIBLE);

                String userEmail = mEmailView.getText().toString();
                String userPassword = mPasswordView.getText().toString();

                MainActivity.mdriver.setEmail(userEmail);
                MainActivity.mdriver.setPassword(userPassword);

                // Check login using network call
                login();

                Log.d(TAG, "setupListeners - signIn_onclick - Start");
            }
        });

        // Signup button listener
        Button signup = (Button) view.findViewById(R.id.sign_up);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - signup_onclick - Start");

                // Move to Signup Activity
                // Fragment creation
                SignupFragment signupFragment = new SignupFragment();

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.add(R.id.main_frame, signupFragment, signupFragment.TAG);

                fragmentTransaction.addToBackStack(TAG);

                fragmentTransaction.commit();

                Log.d(TAG, "setupListeners - signup_onclick - End");
            }
        });


        Log.d(TAG, "setupListeners - End");
    }

    // Network calls
    private void login() {

        Log.d(TAG, "login - Start");

        try {

            JsonLogin request = new JsonLogin();
            request.setUser(MainActivity.mdriver);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "login - Json Request"+ gson.toJson(request));

            // Call user login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "login - onResponse - Start");

                            Log.d(TAG, "login - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonLogin jsonResponse = gson.fromJson(responseData, JsonLogin.class);

                            Log.d(TAG, "login - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "login - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success login
                                // Move to next screen --> Main Activity
                                MainActivity.mdriver = jsonResponse.getUser();
                            }
                            else {
                                // Invalid login
                                Toast.makeText(getContext(), getString(R.string.login_error), Toast.LENGTH_SHORT).show();
                            }

                            // Disable progress bar
                            mProgressView.setVisibility(View.GONE);

                            Log.d(TAG, "login - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "login - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonLogin jsonResponse = gson.fromJson(responseData, JsonLogin.class);

                                Log.d(TAG, "login - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "login - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                Toast.makeText(getContext(), jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            // Disable progress bar
                            mProgressView.setVisibility(View.GONE);

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
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "login - End");
    }

}
