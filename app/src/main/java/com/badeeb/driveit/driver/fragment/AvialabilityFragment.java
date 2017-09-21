package com.badeeb.driveit.driver.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.badeeb.driveit.driver.MainActivity;
import com.badeeb.driveit.driver.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 */
public class AvialabilityFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = LoginFragment.class.getSimpleName();

    // Class Attributes

    // Firebase database reference
    private DatabaseReference mDatabase;

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
        this.mDatabase = FirebaseDatabase.getInstance().getReference();

        // Refresh menu toolbar
        setHasOptionsMenu(true);

        setupListeners(view);

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
                ImageView ivOffline = view.findViewById(R.id.ivOffline);
                ivOffline.setVisibility(View.GONE);

                ImageView ivOnline = view.findViewById(R.id.ivOnline);
                ivOnline.setVisibility(View.VISIBLE);

                // Change text
                TextView tvSetStatusOffline = view.findViewById(R.id.tvSetStatusOffline);
                tvSetStatusOffline.setVisibility(View.VISIBLE);

                TextView tvSetStatusOnline = view.findViewById(R.id.tvSetStatusOnline);
                tvSetStatusOnline.setVisibility(View.GONE);

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
                ImageView ivOffline = view.findViewById(R.id.ivOffline);
                ivOffline.setVisibility(View.VISIBLE);

                ImageView ivOnline = view.findViewById(R.id.ivOnline);
                ivOnline.setVisibility(View.GONE);

                // Change text
                TextView tvSetStatusOffline = view.findViewById(R.id.tvSetStatusOffline);
                tvSetStatusOffline.setVisibility(View.GONE);

                TextView tvSetStatusOnline = view.findViewById(R.id.tvSetStatusOnline);
                tvSetStatusOnline.setVisibility(View.VISIBLE);

                Log.d(TAG, "setupListeners - tvSetStatusOffline_onClick - End");
            }
        });

        // Set Firebase database Listener for trip


        Log.d(TAG, "setupListeners - End");
    }

    private void setDriverOnline() {
        Log.d(TAG, "setDriverOnline - Start");

        // Put Driver under firebase realtime database
        DatabaseReference mRef = mDatabase.getDatabase().getReference("drivers");
        mRef.child(MainActivity.mdriver.getId()+"").child("state").setValue("available");

        RequestDialogFragment requestDialogFragment = new RequestDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("trip", Parcels.wrap(trip));
//        requestDialogFragment.setArguments(bundle);
        requestDialogFragment.setCancelable(false);

        FragmentManager fragmentManager = getFragmentManager();
        requestDialogFragment.show(fragmentManager, requestDialogFragment.TAG);

        Log.d(TAG, "setDriverOnline - End");
    }


    private void setDriverOffline() {
        Log.d(TAG, "setDriverOffline - Start");

        // Remove Driver from firebase realtime database
        DatabaseReference mRef = mDatabase.getDatabase().getReference("drivers");
        mRef.child(MainActivity.mdriver.getId()+"").removeValue();

        Log.d(TAG, "setDriverOffline - End");
    }
}
