package com.minimize.android.routineplan.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.Firebase;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.Task;
import com.minimize.android.routineplan.databinding.ActivityTasksBinding;
import com.minimize.android.routineplan.databinding.ItemRoutineBinding;
import com.minimize.android.routineplan.flux.stores.TasksStore;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.SimpleViewHolder;
import com.squareup.otto.Subscribe;
import java.util.Collections;
import java.util.List;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 25/04/2016.
 */
public class TasksActivity extends BaseActivity {
  ActivityTasksBinding mBinding;

  TasksStore mTasksStore;
  RxDataSource<Task> rxDataSource;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(this));

    mTasksStore = TasksStore.get(mDispatcher);

    final String routine = getIntent().getStringExtra("Routine");
    final Firebase tasksRef = new Firebase("https://routineplan.firebaseio.com/routines/" + android_id + "/" + routine);

    rxDataSource = new RxDataSource<>(Collections.<Task>emptyList());
    rxDataSource.<ItemRoutineBinding>bindRecyclerView(mBinding.recyclerViewRoutines, R.layout.item_routine).subscribe(
        new Action1<SimpleViewHolder<Task, ItemRoutineBinding>>() {
          @Override public void call(final SimpleViewHolder<Task, ItemRoutineBinding> viewHolder) {
            final ItemRoutineBinding viewDataBinding = viewHolder.getViewDataBinding();
            final Task item = viewHolder.getItem();
            viewDataBinding.textView.setText(item.getName()+" - "+item.getTime());
            viewDataBinding.getRoot().setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {

              }
            });
          }
        });

    mBinding.fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        final EditText editTextTaskName = new EditText(TasksActivity.this);
        editTextTaskName.setLayoutParams(
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editTextTaskName.setHint("Task Name");

        final NumberPicker numberPicker = new NumberPicker(TasksActivity.this);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        numberPicker.setLayoutParams(params);
        String[] displayedValues = { "30 Mins", "1 Hour", "1.5 Hours", "2 Hours", "2.5 Hours", "3 Hours" };
        numberPicker.setDisplayedValues(displayedValues);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayedValues.length - 1);
        LinearLayout linearLayout = new LinearLayout(TasksActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editTextTaskName);
        linearLayout.addView(numberPicker);

        new MaterialDialog.Builder(TasksActivity.this).title("Create a new Task")
            .customView(linearLayout, true)
            .positiveText("Create")
            .negativeText("Cancel")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String time = numberPicker.getDisplayedValues()[numberPicker.getValue()];
                String taskName = editTextTaskName.getText().toString();
                if (taskName.length() > 0) {
                  tasksRef.child(taskName).setValue(time);
                }
              }
            })

            .show();
      }
    });

    mActionsCreator.getTasks(android_id, routine);
  }

  @Override protected void onResume() {
    super.onResume();
    mDispatcher.register(this);
    mDispatcher.register(mTasksStore);

  }

  @Override protected void onPause() {
    super.onPause();
    mDispatcher.unregister(this);
    mDispatcher.unregister(mTasksStore);
  }

  @Subscribe public void onTasksRetrieved(TasksStore.TasksEvent tasksEvent) {
    List<Task> tasks = tasksEvent.mTasks;
    rxDataSource.updateDataSet(tasks).updateAdapter();
  }

  @Subscribe public void onTasksError(TasksStore.TasksError tasksError) {
    Timber.e("onTasksError : " + tasksError.mErrorMessage);
  }
}
