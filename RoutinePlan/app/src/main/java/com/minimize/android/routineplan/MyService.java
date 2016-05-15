package com.minimize.android.routineplan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import com.minimize.android.routineplan.activity.PlayActivity;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.itemhelper.CountDownTimerPausable;
import com.minimize.android.routineplan.itemhelper.OnTaskCompleted;
import com.minimize.android.routineplan.itemhelper.OnTaskStarted;
import com.minimize.android.routineplan.itemhelper.OnTimeTick;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.models.Task;
import com.squareup.otto.Bus;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import timber.log.Timber;

public class MyService extends Service {
  //State Related Stuff
  @RoutineState int mCurrentState = STOPPED;
  private NotificationManager mNotificationManager;
  private NotificationCompat.Builder mNotifyBuilder;
  public Routine mRoutine;

  public final int REQUEST_CODE = 101;

  public String getCurrentTime() {
    return currentTime;
  }

  private String currentTime;

  public void setRoutine(Routine routine) {
    mRoutine = routine;
  }

  public Routine getRoutine() {
    return mRoutine;
  }

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
    if (mTasks != null) mTasks.clear();
    if (mCountDownTimer != null) {
      mCountDownTimer.cancel();
    }
    routine = "";
    mNotificationManager.cancel(notifyID);
  }

  @IntDef({ PLAYING, PAUSED, STOPPED })

  @Retention(RetentionPolicy.SOURCE) public @interface RoutineState {
  }

  public static final int STOPPED = 0;
  public static final int PLAYING = 1;
  public static final int PAUSED = 2;

  @RoutineState public int getRoutineState(String routineName) {
    if (mRoutine != null && mRoutine.getName().equals(routineName)) {
      return mCurrentState;
    } else {
      return STOPPED;
    }
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

  private OnTaskCompleted mOnTaskCompleted;

  public void setOnTaskCompleted(OnTaskCompleted onTaskCompleted) {
    mOnTaskCompleted = onTaskCompleted;
  }

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

      mNotifyBuilder.mActions.clear();

      Intent resumeIntent = new Intent(this, MyService.class);
      resumeIntent.setAction(Keys.RESUME);
      Intent cancelIntent = new Intent(this, MyService.class);
      cancelIntent.setAction(Keys.CANCEL);
      Intent appIntent = new Intent(this, PlayActivity.class);

      mNotifyBuilder.setContentIntent(PendingIntent.getActivity(this, REQUEST_CODE, appIntent, 0));
      mNotifyBuilder.addAction(R.drawable.ic_play, "Resume", PendingIntent.getService(this, REQUEST_CODE, resumeIntent, 0));
      mNotifyBuilder.addAction(R.drawable.ic_cancel, "Cancel", PendingIntent.getService(this, REQUEST_CODE, cancelIntent, 0));

      mNotificationManager.notify(notifyID, mNotifyBuilder.build());
    }
  }

  public void resumeRoutine() {
    if (mCurrentState == PAUSED) {
      mCurrentState = PLAYING;
      mCountDownTimer.start();

      mNotifyBuilder.mActions.clear();

      Intent pauseIntent = new Intent(this, MyService.class);
      pauseIntent.setAction(Keys.PAUSE);

      Intent cancelIntent = new Intent(this, MyService.class);
      cancelIntent.setAction(Keys.CANCEL);

      Intent appIntent = new Intent(this, PlayActivity.class);

      mNotifyBuilder.setContentIntent(PendingIntent.getActivity(this, REQUEST_CODE, appIntent, 0));
      mNotifyBuilder.addAction(R.drawable.ic_pause, "Pause", PendingIntent.getService(this, REQUEST_CODE, pauseIntent, 0));
      mNotifyBuilder.addAction(R.drawable.ic_cancel, "Cancel", PendingIntent.getService(this, REQUEST_CODE, cancelIntent, 0));

      mNotificationManager.notify(notifyID, mNotifyBuilder.build());
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
    } else {
      //stopRoutine();
      //start();
    }
  }

  int notifyID = 1;
  long timeRemaining = 0;
  int hours;
  int minutes, seconds;

  private void countDownTimer(final int taskIndex) {
    if (mTasks.size() > 0) {

      mCountDownTimer = new CountDownTimerPausable(mTasks.get(taskIndex).getMinutes() * 60 * 1000, 1000) {
        @Override public void onTick(long millisUntilFinished) {
          timeRemaining = millisUntilFinished / 1000;
          hours = (int) (timeRemaining / 3600);
          minutes = (int) ((timeRemaining % 3600) / 60);
          seconds = (int) (timeRemaining % 60);

          currentTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
          mOnTimeTick.onTimeTick(currentTime);

          mNotifyBuilder.setContentTitle("Playing " + routine + " Routine");
          mNotifyBuilder.setContentText(mTasks.get(currentTask).getName() + " - " + currentTime);
          mNotificationManager.notify(notifyID, mNotifyBuilder.build());
        }

        @Override public void onFinish() {
          if (mOnTaskCompleted != null) {
            Task task = mTasks.get(currentTask);
            mOnTaskCompleted.onTaskCompleted(routine, task);
          }

          if (taskIndex + 1 < mTasks.size()) {

            currentTask = taskIndex + 1;
            countDownTimer(taskIndex + 1);
          } else {
            //means everything ended
            mOnTaskStarted.onTaskStarted(null, null);
            mCurrentState = STOPPED;
            mNotificationManager.cancel(notifyID);
          }
        }
      };
      mCountDownTimer.start();

      Intent pauseIntent = new Intent(this, MyService.class);
      pauseIntent.setAction(Keys.PAUSE);

      Intent cancelIntent = new Intent(this, MyService.class);
      cancelIntent.setAction(Keys.CANCEL);

      Intent appIntent = new Intent(this, PlayActivity.class);

      mNotifyBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle("Playing Routine")
          .setContentIntent(PendingIntent.getActivity(this, REQUEST_CODE, appIntent, 0))
          .addAction(new NotificationCompat.Action(R.drawable.ic_pause, "Pause", PendingIntent.getService(this, REQUEST_CODE, pauseIntent, 0)))
          .addAction(new NotificationCompat.Action(R.drawable.ic_cancel, "Cancel", PendingIntent.getService(this, REQUEST_CODE, cancelIntent, 0)))
          .setContentText(mTasks.get(currentTask).getName() + " - " + timeRemaining);

      mNotifyBuilder.build();

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
    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Timber.e("Service Created");
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Timber.e("Service Destroyed");
  }

  Bus mBus;

  public void setBus(Bus bus) {
    mBus = bus;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      if (intent.getAction() == Keys.PAUSE) {
        pauseRoutine();
        mBus.post(new RoutinePause());
      } else if (intent.getAction() == Keys.CANCEL) {
        stopRoutine();
        mBus.post(new RoutineStop());
      } else if (intent.getAction() == Keys.RESUME) {
        resumeRoutine();
        mBus.post(new RoutineResume());
      }
    }
    return super.onStartCommand(intent, flags, startId);
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

  public class RoutinePause {
  }

  public class RoutineResume {
  }

  public class RoutineStop {
  }
}