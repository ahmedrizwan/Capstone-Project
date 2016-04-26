package com.minimize.android.routineplan;

import android.app.Application;
import com.firebase.client.Firebase;
import com.minimize.android.routineplan.flux.actions.ActionsCreator;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.squareup.otto.Bus;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class App extends Application{
  private Firebase mFirebaseRef;
  private Dispatcher dispatcher;
  private ActionsCreator actionsCreator;

  public Firebase getFirebaseRef() {
    return mFirebaseRef;
  }

  @Override public void onCreate() {
    super.onCreate();
    Firebase.setAndroidContext(this);
    Firebase.getDefaultConfig().setPersistenceEnabled(true);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");
    Timber.plant(new Timber.DebugTree());
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
