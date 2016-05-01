package com.minimize.android.routineplan.flux.actions;

import com.minimize.android.routineplan.Task;
import java.util.List;

/**
 * Created by ahmedrizwan on 04/03/2016.
 */
public interface MyActions {

  String GET_ROUTINES = "get-routines";
  String RENAME_ROUTINE = "rename-routine";
  String DELETE_ROUTINE = "delete-routine";
  String CREATE_ROUTINE = "create-routine";

  void getRoutines();

  void renameRoutine(String oldName, String newName);

  void deleteRoutine(String routine);

  void createRoutine(String name);

  String GET_TASKS = "get-tasks";
  String CREATE_TASK = "create-task";
  String UPDATE_TASKS = "update-tasks";

  void getTasks(String routine);

  void createTask(String routine, Task task);

  void updateTasks(String routine, List<Task> tasks);
}