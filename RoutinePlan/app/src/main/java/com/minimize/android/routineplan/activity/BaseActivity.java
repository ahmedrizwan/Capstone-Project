package com.minimize.android.routineplan.activity;

import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.firebase.client.Firebase;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class BaseActivity extends AppCompatActivity{
  public Firebase mFirebaseRef;
  public String android_id;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mFirebaseRef = new Firebase("https://routineplan.firebaseio.com/");
    android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
  }
}
