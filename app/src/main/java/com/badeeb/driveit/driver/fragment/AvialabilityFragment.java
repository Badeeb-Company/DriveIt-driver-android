package com.badeeb.driveit.driver.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.badeeb.driveit.driver.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AvialabilityFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = LoginFragment.class.getSimpleName();

    // Class Attributes

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

        // Refresh menu toolbar
        setHasOptionsMenu(true);

        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        Button tvSetStatus = view.findViewById(R.id.tvSetStatus);

        tvSetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - tvSetStatus_onClick - Start");



                Log.d(TAG, "setupListeners - tvSetStatus_onClick - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void setDriverOnline() {
        Log.d(TAG, "setDriverOnline - Start");
        
        Log.d(TAG, "setDriverOnline - End");
    }


    private void setDriverOffline() {
        Log.d(TAG, "setDriverOffline - Start");

        Log.d(TAG, "setDriverOffline - End");
    }
}
