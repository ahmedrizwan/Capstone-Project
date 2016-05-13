package com.minimize.android.routineplan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent containterActivityIntent = new Intent(this, ContainerActivity.class);
    Intent intent = getIntent();
    if (intent != null) {
      if (intent.getStringExtra("Routine") != null) {
        Intent taskActivityIntent = new Intent(this, TasksActivity.class);
        taskActivityIntent.putExtra("Routine", intent.getStringExtra("Routine"));
        Intent[] intents = new Intent[] { containterActivityIntent, taskActivityIntent };
        startActivities(intents);
        finish();
        return;
      }
    }
    startActivity(containterActivityIntent);
    finish();
  }
}
