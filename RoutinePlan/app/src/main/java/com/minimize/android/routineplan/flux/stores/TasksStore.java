package com.minimize.android.routineplan.flux.stores;

import com.minimize.android.routineplan.Task;
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
    switch (action.getType()) {
      case MyActions.GET_TASKS:
        String errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          List<Task> tasks = (List<Task>) action.getData().get(Keys.TASKS);
          emitStoreChange(new TasksEvent(tasks));
        }else {
          emitStoreChange(new TasksError(errorMessage));
        }
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
}
