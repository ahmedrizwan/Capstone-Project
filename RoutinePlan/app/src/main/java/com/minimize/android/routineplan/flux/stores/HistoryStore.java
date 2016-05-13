package com.minimize.android.routineplan.flux.stores;

import com.minimize.android.routineplan.models.TimeAndInfo;
import com.minimize.android.routineplan.Utility;
import com.minimize.android.routineplan.flux.actions.Action;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.actions.MyActions;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.squareup.otto.Subscribe;
import java.util.List;

/**
 * Created by ahmedrizwan on 06/05/2016.
 */
public class HistoryStore extends Store {
  protected HistoryStore(Dispatcher dispatcher) {
    super(dispatcher);
  }

  private static HistoryStore instance;

  public static HistoryStore get(Dispatcher dispatcher) {
    if (instance == null) {
      instance = new HistoryStore(dispatcher);
    }
    return instance;
  }

  @Subscribe @Override public void onAction(Action action) {
    String errorMessage = "";
    switch (action.getType()) {
      case MyActions.GET_HISTORY:
        //noinspection unchecked
        errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          List<TimeAndInfo> histories = (List<TimeAndInfo>) action.getData().get(Keys.HISTORY);
          emitStoreChange(new HistoryEvent(histories));
        } else {
          emitStoreChange(new HistoryError(errorMessage));
        }
        break;
    }
  }

  public class HistoryEvent implements StoreChangeEvent {
    public List<TimeAndInfo> mHistoryList;

    public HistoryEvent(List<TimeAndInfo> historyList) {
      mHistoryList = historyList;
    }
  }

  public class HistoryError implements StoreChangeEvent {
    public String errorMessage;

    public HistoryError(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}
