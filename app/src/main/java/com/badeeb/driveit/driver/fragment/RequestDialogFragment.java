package com.badeeb.driveit.driver.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.badeeb.driveit.driver.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestDialogFragment extends DialogFragment {

    // Logging Purpose
    public static final String TAG = RequestDialogFragment.class.getSimpleName();

    public RequestDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_dialog, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Setup Listeners
        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        Button bAccept = view.findViewById(R.id.bAccept);

        bAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - bAccept_onClick - Start");

                acceptRide();

                Log.d(TAG, "setupListeners - bAccept_onClick - End");
            }
        });

        Button bReject = view.findViewById(R.id.bReject);
        bReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - bReject_onClick - Start");

                rejectRide();

                Log.d(TAG, "setupListeners - bReject_onClick - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void acceptRide() {
        Log.d(TAG, "acceptRide - Start");

        RequestDialogFragment.this.dismiss();

        Log.d(TAG, "acceptRide - End");
    }

    private void rejectRide() {
        Log.d(TAG, "rejectRide - Start");

        RequestDialogFragment.this.dismiss();

        Log.d(TAG, "rejectRide - End");
    }
}
