package com.minimize.android.routineplan;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import timber.log.Timber;

public class MyService extends Service {
  //State Related Stuff
  @RoutineState int mCurrentState = STOPPED;

  public void setTextViews(TextView textViewPlayingTask, TextView textViewNextTask) {
    Task task = mTasks.get(currentTask);
    if (task != null) {
      textViewPlayingTask.setText(task.getName());
    }
    if (currentTask + 1 < mTasks.size()) {
      textViewNextTask.setText(mTasks.get(currentTask + 1).getName());
    }
  }

  public void stopRoutine() {
    mCurrentState = STOPPED;
    mTasks.clear();
    mCountDownTimer.cancel();
    routine = "";
  }

  @IntDef({ PLAYING, PAUSED, STOPPED })

  @Retention(RetentionPolicy.SOURCE) public @interface RoutineState {
  }

  public static final int STOPPED = 0;
  public static final int PLAYING = 1;
  public static final int PAUSED = 2;

  @RoutineState public int getRoutineState(String routineName) {
    return mCurrentState;
  }

  public void setRoutineState(@RoutineState int state) {
    mCurrentState = state;
  }

  private int currentTask = 0;

  private String routine = "";
  //Some Vars
  private static MyService sMyService;

  private List<Task> mTasks;

  //Callbacks
  private OnTimeTick mOnTimeTick;

  private OnTaskStarted mOnTaskStarted;

  public void setOnTaskStarted(OnTaskStarted onTaskStarted) {
    mOnTaskStarted = onTaskStarted;
  }

  public void setOnTimeTick(OnTimeTick onTimeTick) {
    mOnTimeTick = onTimeTick;
  }

  public void pauseRoutine() {
    if (mCurrentState == PLAYING) {
      mCurrentState = PAUSED;
      mCountDownTimer.pause();
    }
  }

  public void resumeRoutine() {
    if (mCurrentState == PAUSED) {
      mCurrentState = PLAYING;
      mCountDownTimer.start();
    }
  }

  private CountDownTimerPausable mCountDownTimer;

  public void setTasks(List<Task> tasks) {
    mTasks = tasks;
  }

  public void setRoutine(String routineName) {
    routine = routineName;
  }

  public String getRoutineName() {
    return routine;
  }


  public void start() {
    if (mCurrentState == STOPPED) {
      mCurrentState = PLAYING;
      countDownTimer(currentTask);
    }
  }

  private void countDownTimer(final int taskIndex) {
    if (mTasks.size() > 0) {
      mCountDownTimer = new CountDownTimerPausable(mTasks.get(taskIndex).getMinutes() * 60 * 1000, 1000) {
        @Override public void onTick(long millisUntilFinished) {
          mOnTimeTick.onTimeTick(millisUntilFinished / 1000);
        }

        @Override public void onFinish() {
          Timber.e("onFinish : Finished");
          if (taskIndex + 1 < mTasks.size()) {
            currentTask = taskIndex + 1;
            countDownTimer(taskIndex + 1);
          } else {
            //means everything ended
            mOnTaskStarted.onTaskStarted(null, null);
            mCurrentState = STOPPED;
          }
        }
      };
      mCountDownTimer.start();
      if (taskIndex + 1 < mTasks.size()) {
        mOnTaskStarted.onTaskStarted(mTasks.get(taskIndex), mTasks.get(taskIndex + 1));
      } else {
        mOnTaskStarted.onTaskStarted(mTasks.get(taskIndex), null);
      }
    }
  }

  public static MyService getInstance() {
    return sMyService;
  }


  @Override public void onCreate() {
    sMyService = this;
    Timber.e("Service Created");
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Timber.e("Service Destroyed");
  }

  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();

  public class LocalBinder extends Binder {
    public MyService getService() {
      return MyService.this;
    }
  }

  @Nullable @Override public IBinder onBind(final Intent intent) {
    return mBinder;
  }
}