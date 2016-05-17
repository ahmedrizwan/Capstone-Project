package com.minimize.android.routineplan.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.activity.ContainerActivity;

public class WidgetProvider extends AppWidgetProvider {
  final int requestCode = 1010;

  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Perform this loop procedure for each App Widget that belongs to this provider
    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
      // Create an Intent to launch ExampleActivity
      Intent intent = new Intent(context, WidgetViewsService.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        views.setRemoteAdapter(R.id.routines_list, intent);
      } else {
        views.setRemoteAdapter(appWidgetId, R.id.routines_list, intent);
      }

      views.setEmptyView(R.id.routines_list, R.id.emptyView);

      Intent startActivityIntent = new Intent(context, ContainerActivity.class);

      PendingIntent startActivityPendingIntent =
          PendingIntent.getActivity(context, requestCode, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      views.setPendingIntentTemplate(R.id.routines_list, startActivityPendingIntent);

      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  @Override public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);

    //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    //ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetFactory.class.getName());
    //int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
    //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.routines_list);
    AppWidgetManager mgr = AppWidgetManager.getInstance(context);
    int[] ids = mgr.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
    mgr.notifyAppWidgetViewDataChanged(ids, R.id.routines_list);
  }

  public static RemoteViews updateWidgetListView(Context context, int appWidgetId) {

    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

    Intent svcIntent = new Intent(context, WidgetViewsService.class);

    svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

    svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

    remoteViews.setRemoteAdapter(appWidgetId, R.id.routines_list, svcIntent);

    remoteViews.setEmptyView(R.id.routines_list, R.id.empty_view);

    updateWidget(context, remoteViews);

    return remoteViews;
  }

  public static void updateWidget(Context context, RemoteViews remoteViews) {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.routines_list);
  }
}