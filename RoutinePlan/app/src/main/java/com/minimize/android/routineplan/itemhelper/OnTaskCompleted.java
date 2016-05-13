package com.minimize.android.routineplan.itemhelper;

import com.minimize.android.routineplan.models.Task;

/**
 * Created by ahmedrizwan on 06/05/2016.
 */
public abstract class OnTaskCompleted {
  public abstract void onTaskCompleted(String routine, Task task);
}
