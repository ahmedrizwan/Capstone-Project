package com.minimize.android.routineplan.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.activity.SplashActivity;

public class WidgetProvider extends AppWidgetProvider {

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

        Intent launchActivity = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 230, launchActivity,
            PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.routines_list, pendingIntent);

        views.setEmptyView(R.id.routines_list, R.id.emptyView);
        appWidgetManager.updateAppWidget(appWidgetId, views);
      }
    }
}