package com.minimize.android.routineplan.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.firebase.client.Firebase;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.flux.actions.ActionsCreator;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class BaseFragment extends Fragment {

  public Firebase mFirebaseRef;
  String android_id;
  public Dispatcher mDispatcher;
  public ActionsCreator mActionsCreator;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");
    android_id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    App application = (App) getActivity().getApplication();
    mDispatcher = application.getDispatcher();
    mActionsCreator = application.getActionsCreator();
  }
}
