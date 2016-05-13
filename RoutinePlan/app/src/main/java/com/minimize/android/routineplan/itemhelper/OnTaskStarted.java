package com.minimize.android.routineplan.itemhelper;

import com.minimize.android.routineplan.models.Task;

/**
 * Created by ahmedrizwan on 03/05/2016.
 */
public abstract class OnTaskStarted {
  public abstract void onTaskStarted(Task currentTask, Task nextTask);
}
