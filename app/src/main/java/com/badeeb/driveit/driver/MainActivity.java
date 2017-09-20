package com.badeeb.driveit.driver;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.badeeb.driveit.driver.fragment.LoginFragment;

import org.parceler.Parcels;

public class MainActivity extends AppCompatActivity {

    // Logging Purpose
    private final String TAG = MainActivity.class.getSimpleName();

    // Class attributes
    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private MenuItem mlogoutItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        Log.d(TAG, "onCreate - End");
    }

    private void init() {
        Log.d(TAG, "init - Start");

        // Initialize Attributes
        mFragmentManager = getSupportFragmentManager();

        // Toolbar
        this.mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(this.mtoolbar);

        // Load Login Fragment inside Main activity
        // Fragment creation
        LoginFragment loginFragment = new LoginFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);

        fragmentTransaction.commit();


        Log.d(TAG, "init - End");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu - Start");

        getMenuInflater().inflate(R.menu.menu, menu);

        mlogoutItem = menu.findItem(R.id.nav_logout);

        mlogoutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d(TAG, "setupListeners - mlogoutItem_onMenuItemClick - Start");

                logout();

                Log.d(TAG, "setupListeners - mlogoutItem_onMenuItemClick - End");
                return false;
            }
        });

        Log.d(TAG, "onCreateOptionsMenu - End");
        return true;
    }


    private void logout() {
        Log.d(TAG, "logout - Start");

        Log.d(TAG, "logout - End");
    }
}
