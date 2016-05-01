package com.minimize.android.routineplan.itemhelper;

import com.minimize.android.routineplan.Task;
import java.util.List;

/**
 * Created by ahmedrizwan on 30/04/2016.
 */
public abstract class OnItemsReordered {
  public abstract void onItemsReordered(List<Task> tasks);
}
