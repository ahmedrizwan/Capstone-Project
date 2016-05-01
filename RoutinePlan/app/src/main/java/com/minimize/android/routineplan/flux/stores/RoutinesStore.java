package com.minimize.android.routineplan.flux.stores;

import com.minimize.android.routineplan.Utility;
import com.minimize.android.routineplan.flux.actions.Action;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.actions.MyActions;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.squareup.otto.Subscribe;
import java.util.List;
import timber.log.Timber;

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
    Timber.e("onAction : Hereeeee!");
    switch (action.getType()) {
      case MyActions.GET_ROUTINES:
        //noinspection unchecked
        String errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          List<String> routines = (List<String>) action.getData().get(Keys.ROUTINES);
          emitStoreChange(new RoutinesEvent(routines));
        } else {
          emitStoreChange(new RoutinesError(errorMessage));
        }
        break;
    }
  }

  public class RoutinesEvent implements StoreChangeEvent {
    public List<String> routinesList;

    public RoutinesEvent(List<String> routinesList) {
      this.routinesList = routinesList;
    }
  }

  public class RoutinesError implements StoreChangeEvent {
    public String errorMessage;

    public RoutinesError(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

}