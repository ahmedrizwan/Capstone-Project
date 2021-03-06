package com.minimize.android.routineplan.flux.stores;

import com.minimize.android.routineplan.models.Task;
import com.minimize.android.routineplan.Utility;
import com.minimize.android.routineplan.flux.actions.Action;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.actions.MyActions;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.squareup.otto.Subscribe;
import java.util.List;

/**
 * Created by ahmedrizwan on 26/04/2016.
 */
public class TasksStore extends Store{

  protected TasksStore(Dispatcher dispatcher) {
    super(dispatcher);
  }

  private static TasksStore instance;

  public static TasksStore get(Dispatcher dispatcher) {
    if (instance == null) {
      instance = new TasksStore(dispatcher);
    }
    return instance;
  }

  @Subscribe @Override public void onAction(Action action) {
    String errorMessage = "";
    switch (action.getType()) {
      case MyActions.GET_TASKS:
        errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          List<Task> tasks = (List<Task>) action.getData().get(Keys.TASKS);
          emitStoreChange(new TasksEvent(tasks));
        }else {
          emitStoreChange(new TasksError(errorMessage));
        }
        break;
      case MyActions.GET_BREAK_INTERVAL:
       errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          String breakInterval = (String) action.getData().get(Keys.BREAK);
          emitStoreChange(new BreakIntervalEvent(breakInterval));
        } else {
          emitStoreChange(new BreakIntervalError(errorMessage));
        }
        break;
      case MyActions.UPDATE_TASK:
        Task newTask = (Task) action.getData().get(Keys.TASK);
        emitStoreChange(new TaskUpdateEvent(newTask));
        break;
    }
  }

  public class TasksEvent implements StoreChangeEvent {
    public TasksEvent(List<Task> tasks) {
      mTasks = tasks;
    }

    public List<Task> mTasks;

  }

  public class TasksError implements StoreChangeEvent {
    public String mErrorMessage;

    public TasksError(String errorMessage) {

      mErrorMessage = errorMessage;
    }
  }

  public class BreakIntervalEvent implements StoreChangeEvent {
    public String breakInterval;

    public BreakIntervalEvent(String breakInterval) {
      this.breakInterval = breakInterval;
    }
  }

  public class BreakIntervalError implements StoreChangeEvent {
    public String mError;

    public BreakIntervalError(String error) {
      mError = error;
    }
  }

  public class TaskUpdateEvent implements StoreChangeEvent {
    public Task mNewTask;

    public TaskUpdateEvent(Task newTask) {
      mNewTask = newTask;
    }
  }
}
