package com.minimize.android.routineplan.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.firebase.client.Firebase;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.flux.actions.ActionsCreator;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class BaseActivity extends AppCompatActivity{
  public Firebase mFirebaseRef;
  public Dispatcher mDispatcher;
  public ActionsCreator mActionsCreator;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    setTheme(Prefs.getInt(Keys.THEME, R.style.AppTheme));
    super.onCreate(savedInstanceState);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");

    App application = (App) getApplication();
    mDispatcher = application.getDispatcher();
    mActionsCreator = application.getActionsCreator();
  }
}
