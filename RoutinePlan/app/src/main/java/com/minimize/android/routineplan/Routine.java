package com.minimize.android.routineplan;

/**
 * Created by ahmedrizwan on 01/05/2016.
 */
public class Routine {
  String name;
  int totalMinutes;

  public Routine(String name, int totalMinutes) {
    this.name = name;
    this.totalMinutes = totalMinutes;
  }

  public int getTotalMinutes() {
    return totalMinutes;
  }

  public void setTotalMinutes(int totalMinutes) {
    this.totalMinutes = totalMinutes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
