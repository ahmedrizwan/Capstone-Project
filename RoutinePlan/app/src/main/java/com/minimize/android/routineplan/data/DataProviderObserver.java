package com.minimize.android.routineplan.data;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.ContentObserver;
import android.os.Handler;
import com.minimize.android.routineplan.R;
import timber.log.Timber;

public class DataProviderObserver extends ContentObserver {
  private AppWidgetManager mAppWidgetManager;
  private ComponentName mComponentName;

  public DataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
    super(h);
    mAppWidgetManager = mgr;
    mComponentName = cn;
  }

  @Override public void onChange(boolean selfChange) {
    super.onChange(selfChange);  //NOTE: Have to call this. dwy.
    // The data has changed, so notify the widget that the collection view needs to be updated.
    // In response, the factory's onDataSetChanged() will be called which will requery the
    // cursor for the new data.
    Timber.e("onChange : PresenterWidgetProvider--------------------Date Changed");
    mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.routines_list);
  }
}
