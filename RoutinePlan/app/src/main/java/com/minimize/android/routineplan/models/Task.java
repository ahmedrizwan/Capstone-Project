package com.minimize.android.routineplan.models;

/**
 * Created by ahmedrizwan on 26/04/2016.
 */
public class Task {
  String name;

  public String getName() {

    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMinutes() {
    return minutes;
  }

  public void setMinutes(int minutes) {
    this.minutes = minutes;
  }

  int minutes;

  public Task(String name, int minutes) {
    this.name = name;
    this.minutes = minutes;
  }
}
