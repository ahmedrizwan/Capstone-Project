package com.minimize.android.routineplan;

/**
 * Created by ahmedrizwan on 26/04/2016.
 */
public class Task {
  String name;

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getName() {

    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  String time;

  public Task(String name, String time) {
    this.name = name;
    this.time = time;
  }
}
