package com.minimize.android.routineplan.models;

/**
 * Created by ahmedrizwan on 06/05/2016.
 */
public class TimeAndInfo {
  public String getDateAndTime() {
    return dateAndTime;
  }

  public void setDateAndTime(String dateAndTime) {
    this.dateAndTime = dateAndTime;
  }

  String dateAndTime;
  long time;
  String routine, taskName;

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getRoutine() {
    return routine;
  }

  public void setRoutine(String routine) {
    this.routine = routine;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }
}
