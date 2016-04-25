package com.minimize.android.routineplan.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.Firebase;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.ActivityTasksBinding;

/**
 * Created by ahmedrizwan on 25/04/2016.
 */
public class TasksActivity extends BaseActivity {
  ActivityTasksBinding mBinding;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);

    final String routine = getIntent().getStringExtra("Routine");
    final Firebase tasksRef = new Firebase("https://routineplan.firebaseio.com/routines/" + android_id + "/" + routine);

    mBinding.fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        new MaterialDialog.Builder(TasksActivity.this).title("Create a new Task")
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input("Tasks's Name", null, new MaterialDialog.InputCallback() {
              @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                // Do something
                if (input.length() > 0) {
                  tasksRef.child(input.toString().trim()).setValue(input.toString().trim());
                }
              }
            })
            .show();
      }
    });
  }
}
