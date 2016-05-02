package com.minimize.android.routineplan.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.Task;
import com.minimize.android.routineplan.databinding.ActivityTasksBinding;
import com.minimize.android.routineplan.flux.stores.TasksStore;
import com.minimize.android.routineplan.itemhelper.OnItemsReordered;
import com.minimize.android.routineplan.itemhelper.RecyclerListAdapter;
import com.minimize.android.routineplan.itemhelper.SimpleItemTouchHelperCallback;
import com.squareup.otto.Subscribe;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 25/04/2016.
 */
public class TasksActivity extends BaseActivity {
  ActivityTasksBinding mBinding;
  TasksStore mTasksStore;
  //RxDataSource<Task> rxDataSource;
  RecyclerListAdapter mRecyclerListAdapter;
  private ItemTouchHelper mItemTouchHelper;
  private List<Task> mTasks;

  private Timer mTimer;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(this));

    mTasksStore = TasksStore.get(mDispatcher);

    ActionBar supportActionBar = getSupportActionBar();
    final String routine = getIntent().getStringExtra("Routine");

    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
      supportActionBar.setTitle(routine + " Tasks");
    }

    mRecyclerListAdapter = new RecyclerListAdapter(Collections.EMPTY_LIST, new Callable() {
      @Override public Object call() throws Exception {

        return null;
      }
    }, new OnItemsReordered() {

      @Override public void onItemsReordered(final List<Task> tasks) {
        if (mTimer != null) {
          mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
          @Override public void run() {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                mActionsCreator.updateTasks(routine, tasks);
              }
            });
          }
        }, 1000);
      }
    });

    mBinding.recyclerViewRoutines.setAdapter(mRecyclerListAdapter);
    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mRecyclerListAdapter);

    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(mBinding.recyclerViewRoutines);

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
                Task task = new Task(taskName, time);
                if (taskName.length() > 0) {
                  mActionsCreator.createTask(routine, task, mTasks.size());
                }
              }
            })
            .show();
      }
    });

    mBinding.radioGroupBreak.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio_five_minutes) {
          mActionsCreator.updateBreakInterval(routine, 5);
        } else if (checkedId == R.id.radio_ten_minutes) {
          mActionsCreator.updateBreakInterval(routine, 10);
        }
      }
    });
    mActionsCreator.getTasks(routine);
    mActionsCreator.getBreakInterval(routine);
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
    mTasks = tasksEvent.mTasks;
    mRecyclerListAdapter.updateDataSet(mTasks);
  }

  @Subscribe public void onTasksError(TasksStore.TasksError tasksError) {
    Timber.e("onTasksError : " + tasksError.mErrorMessage);
  }

  @Subscribe public void onBreakInterval(TasksStore.BreakIntervalEvent breakIntervalEvent) {
    Timber.e("onBreakInterval : " + breakIntervalEvent.breakInterval);
    if (breakIntervalEvent.breakInterval.equals("5")) {
      mBinding.radioFiveMinutes.setChecked(true);
    } else if (breakIntervalEvent.breakInterval.equals("10")) {
      mBinding.radioTenMinutes.setChecked(true);
    }
  }

  @Subscribe public void onBreakError(TasksStore.BreakIntervalError breakIntervalError) {
    Timber.e("onBreakError : " + breakIntervalError.mError);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
    }
    return super.onOptionsItemSelected(item);
  }
}
