package com.minimize.android.routineplan;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new WidgetFactory(this.getApplicationContext(), intent);
    }
}