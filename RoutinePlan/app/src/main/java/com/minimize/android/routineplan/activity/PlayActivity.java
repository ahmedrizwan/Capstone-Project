package com.minimize.android.routineplan.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.MyService;
import com.minimize.android.routineplan.OnTaskStarted;
import com.minimize.android.routineplan.OnTimeTick;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.Task;
import com.minimize.android.routineplan.databinding.ActivityPlayBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.stores.TasksStore;
import com.squareup.otto.Subscribe;
import java.util.List;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 03/05/2016.
 */
public class PlayActivity extends BaseActivity {

  ActivityPlayBinding mBinding;
  String mRoutineName;
  TasksStore mTasksStore;
  private MyService mService;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play);
    mRoutineName = getIntent().getStringExtra(Keys.ROUTINE);
    mTasksStore = TasksStore.get(mDispatcher);

    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setTitle("Playing: " + mRoutineName);
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    App application = (App) getApplication();
    if (application.isServiceBound()) {
      mService = application.getServiceInstance();
      if (mService.getRoutineName().equals(mRoutineName)) {
        Timber.e("onCreate : Routine already running");
      } else {
        mActionsCreator.getTasks(mRoutineName);
      }
    } else {
      Timber.e("onCreate : Service not bound bro!");
    }

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
    mService.setRoutine(mRoutineName);
    mService.setTasks(tasks);
    mService.setOnTimeTick(new OnTimeTick() {
      @Override public void onTimeTick(long seconds) {
        mBinding.textViewTimer.setText(seconds + "");
      }
    });

    mService.setOnTaskStarted(new OnTaskStarted() {
      @Override public void onTaskStarted(Task currentTask, Task nextTask) {
        if (nextTask != null) {
          mBinding.textViewNextTask.setText("Next Task: " + nextTask.getName());
        } else {
          mBinding.textViewNextTask.setText("None");
        }
        if (currentTask != null) {
          mBinding.textViewPlayingTask.setText(currentTask.getName());
        } else {
          mBinding.textViewTimer.setText("00:00");
          finish();
        }
      }
    });

    mService.start();
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
