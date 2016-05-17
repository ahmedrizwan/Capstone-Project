package com.minimize.android.routineplan.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.data.DbContract;
import com.minimize.android.routineplan.flux.actions.Keys;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

  private final Context mContext;
  private final Intent mIntent;
  private final ContentResolver mContentResolver;
  private Cursor mCursor;

  public WidgetFactory(Context context, Intent intent) {
    mContext = context;
    mIntent = intent;
    mContentResolver = context.getContentResolver();
  }

  @Override public void onCreate() {
    mCursor = mContentResolver.query(DbContract.Routine.CONTENT_URI, null, null, null, null);
  }

  @Override public void onDataSetChanged() {
  }

  @Override public void onDestroy() {
  }

  @Override public int getCount() {
    mCursor = mContentResolver.query(DbContract.Routine.CONTENT_URI, null, null, null, null);
    return mCursor != null ? mCursor.getCount() : 0;
  }

  @Override public RemoteViews getViewAt(final int position) {

    Log.e("Here", String.valueOf(position));
    mCursor.moveToPosition(position);
    // Create a new Remote Views object using the appropriate // item layout
    RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget);

    final String routineName = mCursor.getString(mCursor.getColumnIndex(DbContract.Routine.COLUMN_NAME));
    final String totalMinutes = mCursor.getString(mCursor.getColumnIndex(DbContract.Routine.COLUMN_TIME));

    if (!totalMinutes.equals("0 Mins")) {
      rv.setTextViewText(R.id.text_view_routine_name, routineName + " - " + totalMinutes);
    } else {
      rv.setTextViewText(R.id.text_view_routine_name, routineName);
    }

    Intent intent = new Intent();
    intent.putExtra(Keys.ROUTINE, routineName);
    rv.setOnClickFillInIntent(R.id.item_routine, intent);

    return rv;
  }

  @Override public RemoteViews getLoadingView() {
    return null;
  }

  @Override public int getViewTypeCount() {
    return 1;
  }

  @Override public long getItemId(final int position) {
    return 0;
  }

  @Override public boolean hasStableIds() {
    return false;
  }
}