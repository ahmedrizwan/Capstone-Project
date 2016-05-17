package com.minimize.android.routineplan.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.MyService;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.ActivityPlayBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.stores.TasksStore;
import com.minimize.android.routineplan.gcm.GCMRequest;
import com.minimize.android.routineplan.itemhelper.OnTaskCompleted;
import com.minimize.android.routineplan.itemhelper.OnTaskStarted;
import com.minimize.android.routineplan.itemhelper.OnTimeTick;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.models.Task;
import com.squareup.otto.Bus;
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
  Bus mBus;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBus = new Bus(Bus.DEFAULT_IDENTIFIER);

    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play);
    setSupportActionBar(mBinding.mainToolbar);
    mTasksStore = TasksStore.get(mDispatcher);

    App application = (App) getApplication();
    if (application.isServiceBound()) {
      mService = application.getServiceInstance();
      mRoutine = Parcels.unwrap(getIntent().getParcelableExtra(Keys.ROUTINE));
      if (mRoutine == null) {
        mRoutine = mService.getRoutine();
        mBinding.textViewTimer.setText(mService.getCurrentTime());
      }
      ActionBar supportActionBar = getSupportActionBar();
      if (supportActionBar != null) {
        supportActionBar.setTitle("Playing: " + mRoutine.getName() + " Routine");
        supportActionBar.setDisplayHomeAsUpEnabled(true);
      }

      if (mService.getRoutineName().equals(mRoutine.getName())) {
        Timber.e("onCreate : Routine already running");
        mService.setOnTimeTick(new OnTimeTick() {
          @Override public void onTimeTick(String time) {
            mBinding.textViewTimer.setText(time);
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

      setPauseButtonState();

      mBinding.cancel.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          mService.stopRoutine();
          //start tasks activity
          Intent intent = new Intent(PlayActivity.this, TasksActivity.class);
          intent.putExtra(Keys.ROUTINE, Parcels.wrap(mRoutine));
          startActivity(intent);

          finish();
        }
      });
      mBinding.pause.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (mService.getRoutineState(mRoutine.getName()) == MyService.PLAYING) {
            mService.pauseRoutine();
            mBinding.pause.setImageDrawable(ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_play));
          } else {
            mService.resumeRoutine();
            mBinding.pause.setImageDrawable(ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_pause));
          }
        }
      });
    }
  }

  @Override protected void onResume() {
    super.onResume();
    mDispatcher.register(this);
    mDispatcher.register(mTasksStore);
    mBus.register(this);
    mService.setBus(mBus);
    mService.setRoutine(mRoutine);
  }

  @Override protected void onPause() {
    super.onPause();
    mDispatcher.unregister(this);
    mDispatcher.unregister(mTasksStore);
    mBus.unregister(this);
  }

  @Subscribe public void onRoutinePause(MyService.RoutinePause routinePause) {
    setPauseButtonState();
  }

  @Subscribe public void onRoutineResume(MyService.RoutineResume routineResume) {
    setPauseButtonState();
  }

  @Subscribe public void onRoutineStop(MyService.RoutineStop routineStop) {
    finish();
  }

  @Subscribe public void onTasksRetrieved(TasksStore.TasksEvent tasksEvent) {

    List<Task> tasks = tasksEvent.mTasks;
    mService.setRoutine(mRoutine.getName());
    if (tasks.size() > 1) {
      for (int i = 0; i < tasks.size(); i++) {
        if (i + 1 < tasks.size() && !tasks.get(i).getName().equals("Break")) {
          tasks.add(i + 1, new Task("Break", mRoutine.getBreakInterval()));
        }
      }
    }

    mService.setTasks(tasks);
    mService.setOnTimeTick(new OnTimeTick() {
      @Override public void onTimeTick(String time) {
        mBinding.textViewTimer.setText(time);
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
        //TODO: Send start notification to topic
        new GCMRequest().execute("Start");
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

        //start break if there's no task ahead

      }
    });

    mService.start();

    setPauseButtonState();
  }

  private void setPauseButtonState() {
    if (mService.getRoutineState(mRoutine.getName()) == MyService.PLAYING) {
      mBinding.pause.setImageDrawable(ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_pause));
    } else {
      mBinding.pause.setImageDrawable(ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_play));
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
