package com.minimize.android.routineplan;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Created by ahmedrizwan on 01/05/2016.
 */
@Parcel(value = Parcel.Serialization.BEAN, analyze = { Routine.class })
public class Routine {
  String name;
  int totalMinutes;
  int breakInterval;

  public void setBreakInterval(int breakInterval) {
    this.breakInterval = breakInterval;
  }

  public int getBreakInterval() {
    return breakInterval;
  }

  @ParcelConstructor
  public Routine(String name, int totalMinutes, int breakInterval) {
    this.breakInterval = breakInterval;
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
