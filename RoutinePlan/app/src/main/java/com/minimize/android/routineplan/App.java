package com.minimize.android.routineplan;

import android.app.Application;
import com.firebase.client.Firebase;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class App extends Application{
  private Firebase mFirebaseRef;

  public Firebase getFirebaseRef() {
    return mFirebaseRef;
  }

  @Override public void onCreate() {
    super.onCreate();
    Firebase.setAndroidContext(this);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");
    Timber.plant(new Timber.DebugTree());
  }
}
