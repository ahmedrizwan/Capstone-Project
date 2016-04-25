package com.minimize.android.routineplan.fragment;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.firebase.client.Firebase;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class BaseFragment extends Fragment {

  public Firebase mFirebaseRef;
  String android_id;
  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");
    android_id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
  }

}
