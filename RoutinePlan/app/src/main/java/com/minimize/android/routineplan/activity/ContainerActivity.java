package com.minimize.android.routineplan.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.minimize.android.routineplan.QuickstartPreferences;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.RegistrationIntentService;
import com.minimize.android.routineplan.fragment.HistoryFragment;
import com.minimize.android.routineplan.fragment.RoutinesFragment;
import com.minimize.android.routineplan.fragment.UserFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class ContainerActivity extends BaseActivity {

  private BottomBar mBottomBar;
  private FragNavController mNavController;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);
    List<Fragment> fragments = new ArrayList<>(5);

    fragments.add(new RoutinesFragment());
    fragments.add(new HistoryFragment());
    fragments.add(new UserFragment());

    mNavController =
        new FragNavController(getSupportFragmentManager(), R.id.fragment_container, fragments);

    mBottomBar = BottomBar.attach(this, savedInstanceState);
    mBottomBar.setItems(new BottomBarTab(R.drawable.routines, "Routines"),
        new BottomBarTab(R.drawable.history, "History"), new BottomBarTab(R.drawable.user, "User"));
    // Listen for tab changes
    mBottomBar.setOnTabClickListener(new OnTabClickListener() {
      @Override
      public void onTabSelected(int position) {
        // The user selected a tab at the specified position
        switch (position) {
          case 0:
            mNavController.switchTab(FragNavController.TAB1);
            break;
          case 1:
            mNavController.switchTab(FragNavController.TAB2);
            break;
          case 2:
            mNavController.switchTab(FragNavController.TAB3);
            break;
        }

      }

      @Override
      public void onTabReSelected(int position) {
        // The user reselected a tab at the specified position!
        mNavController.clearStack();
      }
    });

    mRegistrationBroadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context);
        boolean sentToken = sharedPreferences
            .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
        if (sentToken) {

        } else {

        }
      }
    };

    if (checkPlayServices()) {
      Timber.e("onCreate : PlayServices!");
      // Start IntentService to register this application with GCM.
      Intent intent = new Intent(this, RegistrationIntentService.class);
      startService(intent);
    }
  }
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  private boolean checkPlayServices() {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (apiAvailability.isUserResolvableError(resultCode)) {
        apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
            .show();
      } else {
        Log.i("GCM", "This device is not supported.");
        finish();
      }
      return false;
    }
    return true;
  }
  private BroadcastReceiver mRegistrationBroadcastReceiver;
  @Override
  protected void onResume() {
    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
        new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
  }

  @Override
  protected void onPause() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    super.onPause();
  }
}
