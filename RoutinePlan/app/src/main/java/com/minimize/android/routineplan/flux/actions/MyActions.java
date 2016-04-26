package com.minimize.android.routineplan.flux.actions;

/**
 * Created by ahmedrizwan on 04/03/2016.
 */
public interface MyActions {

  String GET_ROUTINES = "get-routines";

  void getRoutines(String user);

  String GET_TASKS = "get-tasks";

  void getTasks(String user, String routine);

  String CREATE_ROUTINE = "create-routine";

  void createRoutine(String user, String name);





}
