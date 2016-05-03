package com.minimize.android.routineplan.flux.stores;

import com.minimize.android.routineplan.Routine;
import com.minimize.android.routineplan.Utility;
import com.minimize.android.routineplan.flux.actions.Action;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.actions.MyActions;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.squareup.otto.Subscribe;
import java.util.List;

/**
 * Created by ahmedrizwan on 21/04/2016.
 */
public class RoutinesStore extends Store{

  private static RoutinesStore instance;

  public static RoutinesStore get(Dispatcher dispatcher) {
    if (instance == null) {
      instance = new RoutinesStore(dispatcher);
    }
    return instance;
  }

  protected RoutinesStore(Dispatcher dispatcher) {
    super(dispatcher);
  }

  @Subscribe @Override public void onAction(Action action) {
    String errorMessage = "";
    switch (action.getType()) {
      case MyActions.GET_ROUTINES:
        //noinspection unchecked
        errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          List<Routine> routines = (List<Routine>) action.getData().get(Keys.ROUTINES);
          emitStoreChange(new RoutinesEvent(routines));
        } else {
          emitStoreChange(new RoutinesError(errorMessage));
        }
        break;
      case MyActions.RENAME_ROUTINE:
        errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          String newName = (String) action.getData().get(Keys.ROUTINE);
          emitStoreChange(new RoutineRenameEvent(newName));
        } else {

        }
        break;
    }
  }

  public class RoutinesEvent implements StoreChangeEvent {
    public List<Routine> routinesList;

    public RoutinesEvent(List<Routine> routinesList) {
      this.routinesList = routinesList;
    }
  }

  public class RoutinesError implements StoreChangeEvent {
    public String errorMessage;

    public RoutinesError(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public class RoutineRenameEvent implements StoreChangeEvent {
    public String mNewName;

    public RoutineRenameEvent(String newName) {
      mNewName = newName;
    }
  }
}
