package com.minimize.android.routineplan;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.Settings;
import com.firebase.client.Firebase;
import com.minimize.android.routineplan.flux.actions.ActionsCreator;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.otto.Bus;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class App extends Application {

  public static final String USER = "user";
  private Firebase mFirebaseRef;
  private Dispatcher dispatcher;
  private ActionsCreator actionsCreator;

  public Firebase getFirebaseRef() {
    return mFirebaseRef;
  }

  private MyService mMyService;
  private boolean mServiceBound;

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      MyService.LocalBinder binder = (MyService.LocalBinder) service;
      mMyService = binder.getService();
      mServiceBound = true;
    }

    @Override public void onServiceDisconnected(ComponentName arg0) {
      mServiceBound = false;
    }
  };

  public MyService getServiceInstance() {
    return mMyService;
  }

  public boolean isServiceBound() {
    return mServiceBound;
  }

  @Override public void onCreate() {
    super.onCreate();
    //Bind Service to the App
    Intent intent = new Intent(this, MyService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    Firebase.setAndroidContext(this);
    Firebase.getDefaultConfig().setPersistenceEnabled(true);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");

    Timber.plant(new Timber.DebugTree());
    // Initialize the Prefs class
    new Prefs.Builder().setContext(this)
        .setMode(ContextWrapper.MODE_PRIVATE)
        .setPrefsName(getPackageName())
        .setUseDefaultSharedPreference(true)
        .build();

    String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    Prefs.putString(App.USER, android_id);

    //Init RxFlux
    dispatcher = Dispatcher.get(new Bus());
    actionsCreator = ActionsCreator.get(dispatcher);
  }

  public ActionsCreator getActionsCreator() {
    return actionsCreator;
  }

  public Dispatcher getDispatcher() {
    return dispatcher;
  }
}
