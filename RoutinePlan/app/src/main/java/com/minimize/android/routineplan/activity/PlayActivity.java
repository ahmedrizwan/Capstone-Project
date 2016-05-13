package com.minimize.android.routineplan.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.MyService;
import com.minimize.android.routineplan.itemhelper.OnTaskStarted;
import com.minimize.android.routineplan.itemhelper.OnTimeTick;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.models.Task;
import com.minimize.android.routineplan.databinding.ActivityPlayBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.stores.TasksStore;
import com.minimize.android.routineplan.itemhelper.OnTaskCompleted;
import com.squareup.otto.Subscribe;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 03/05/2016.
 */
public class PlayActivity extends BaseActivity {

  ActivityPlayBinding mBinding;
  Routine mRoutine;
  TasksStore mTasksStore;
  private MyService mService;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play);
    mTasksStore = TasksStore.get(mDispatcher);
    mRoutine = Parcels.unwrap(getIntent().getParcelableExtra(Keys.ROUTINE));

    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setTitle("Playing: " + mRoutine.getName() + " Routine");
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }
    Timber.e("onCreate : Break Interval " + mRoutine.getBreakInterval());

    App application = (App) getApplication();
    if (application.isServiceBound()) {
      mService = application.getServiceInstance();
      if (mService.getRoutineName().equals(mRoutine.getName())) {
        Timber.e("onCreate : Routine already running");
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

        mService.setTextViews(mBinding.textViewPlayingTask, mBinding.textViewNextTask);
      } else {
        mActionsCreator.getTasks(mRoutine.getName());
      }
      mBinding.buttonPause.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (mService.getRoutineState(mRoutine.getName()) == MyService.PLAYING) {
            mService.pauseRoutine();
            mBinding.buttonPause.setText("Resume");
          } else {
            mService.resumeRoutine();
            mBinding.buttonPause.setText("Pause");
          }
        }
      });

      setPauseButtonState();

      mBinding.buttonCancel.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mService.stopRoutine();
          finish();
        }
      });
    } else {
      Timber.e("onCreate : Service not bound bruh!");
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
    mService.setRoutine(mRoutine.getName());
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

    mService.setOnTaskCompleted(new OnTaskCompleted() {
      @Override public void onTaskCompleted(String routine, Task task) {
        //Save history
        //get Date
        Calendar calendar = Calendar.getInstance();
        NumberFormat instance = new DecimalFormat("00");

        String date = instance.format(calendar.get(Calendar.DAY_OF_MONTH)) + "-" +
            instance.format(calendar.get(Calendar.MONTH)) + "-" +
            instance.format(calendar.get(Calendar.YEAR));

        Timber.e("onTaskCompleted : " + date);
        String time = instance.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
            instance.format(calendar.get(Calendar.MINUTE));
        Timber.e("onTaskCompleted : " + time);

        mActionsCreator.saveHistory(routine, task, date, time);
      }
    });

    mService.start();

    setPauseButtonState();
  }

  private void setPauseButtonState() {
    if (mService.getRoutineState(mRoutine.getName()) == MyService.PLAYING) {

      mBinding.buttonPause.setText("Pause");
    } else {
      mBinding.buttonPause.setText("Resume");
    }
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
