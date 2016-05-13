package com.minimize.android.routineplan.flux.stores;

import android.content.ContentValues;
import android.content.Context;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.Utility;
import com.minimize.android.routineplan.activity.TasksActivity;
import com.minimize.android.routineplan.data.DbContract;
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
public class RoutinesStore extends Store {

  private static RoutinesStore instance;
  private final Context mContext;

  public static RoutinesStore get(Context context, Dispatcher dispatcher) {
    if (instance == null) {
      instance = new RoutinesStore(context, dispatcher);
    }
    return instance;
  }

  protected RoutinesStore(Context context, Dispatcher dispatcher) {
    super(dispatcher);
    mContext = context;
  }

  @Subscribe @Override public void onAction(Action action) {
    String errorMessage = "";
    switch (action.getType()) {
      case MyActions.GET_ROUTINES:
        //noinspection unchecked
        errorMessage = Utility.checkForErrorResponse(action);
        if (errorMessage.equals("")) {
          List<Routine> routines = (List<Routine>) action.getData().get(Keys.ROUTINES);

          ContentValues contentValues = new ContentValues();
          for (Routine routine : routines) {
            contentValues.put(DbContract.Routine.COLUMN_NAME, routine.getName());
            String totalMinutes = TasksActivity.convertMinutesToString(routine.getTotalMinutes());
            Timber.e("onAction : "+totalMinutes);
            contentValues.put(DbContract.Routine.COLUMN_TIME, totalMinutes);
            mContext.getContentResolver().insert(DbContract.Routine.CONTENT_URI, contentValues);
          }

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
