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
  String UPDATE_TASKS = "update-tasks";
  String GET_TASKS = "get-tasks";
  String CREATE_TASK = "create-task";
  String UPDATE_TASK = "update-task";
  String UPDATE_BREAK_INTERVAL = "update-break-interval";
  String GET_BREAK_INTERVAL = "get-break-interval";
  String SAVE_HISTORY = "save-history"; //save after each task completes
  String GET_HISTORY = "get-history";
  String LOGIN = "login";

  void getRoutines();

  void renameRoutine(String oldName, String newName);

  void deleteRoutine(String routine);

  void createRoutine(String name, int priority);

  void updateTask(String routine, Task oldTask, Task newTask, int position);

  void updateBreakInterval(String routine, int interval);

  void getBreakInterval(String routine);

  void getTasks(String routine);

  void createTask(String routine, Task task, int priority);

  void updateTasks(String routine, List<Task> tasks);

  void login(String userEmail);

  void saveHistory(String routine, Task task, String date, String time);
}
