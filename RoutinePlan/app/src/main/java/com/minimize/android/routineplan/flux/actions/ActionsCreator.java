package com.minimize.android.routineplan.flux.actions;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.Routine;
import com.minimize.android.routineplan.Task;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionsCreator implements MyActions {

  public static String BASE_URL = "https://routineplan.firebaseio.com/";

  private static ActionsCreator instance;
  final Dispatcher dispatcher;

  public ActionsCreator(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public static ActionsCreator get(Dispatcher dispatcher) {
    if (instance == null) {
      instance = new ActionsCreator(dispatcher);
    }
    return instance;
  }

  //Routine methods
  private String getRoutineUrl(String user, String routine) {
    return getRoutinesUrl(user) + "/" + routine;
  }

  private String getRoutinesUrl(String user) {
    return BASE_URL + "/" + user + "/Routines/";
  }

  @Override public void getRoutines() {
    String user = Prefs.getString(App.USER, null);

    final Firebase routinesRef = new Firebase(getRoutinesUrl(user));
    routinesRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        List<Routine> routines = new ArrayList<Routine>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          int totalMinutes = 0;
          int breakInterval = 5;
          HashMap values = (HashMap) snapshot.getValue();
          breakInterval = ((Long) values.get("Break")).intValue();

          try {
            HashMap tasks = (HashMap) values.get("Tasks");
            for (Object minutes : tasks.values()) {
              totalMinutes += ((Long) minutes).intValue();
            }
          } catch (NullPointerException e) {
            //do nothing
          }
          routines.add(new Routine(snapshot.getKey(), totalMinutes, breakInterval));
        }
        dispatcher.dispatch(MyActions.GET_ROUTINES, Keys.ROUTINES, routines);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {
        dispatcher.dispatch(MyActions.GET_ROUTINES, Keys.ERROR, firebaseError);
      }
    });
  }

  @Override public void createRoutine(String name, int priority) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase(getRoutinesUrl(user));
    routinesRef.child(name).child("Break").setValue(5);
  }

  @Override public void renameRoutine(final String oldName, final String newName) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase(getRoutinesUrl(user));
    final Firebase oldTaskRef = new Firebase(getRoutinesUrl(user) + "/" + oldName);
    oldTaskRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        oldTaskRef.removeEventListener(this);
        routinesRef.child(newName).setValue(dataSnapshot.getValue());
        oldTaskRef.removeValue();
        dispatcher.dispatch(MyActions.RENAME_ROUTINE, Keys.ROUTINE, newName);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {

      }
    });
  }

  @Override public void deleteRoutine(String routine) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase(getRoutineUrl(user, routine));
    routinesRef.removeValue();
  }

  //Task methods
  @Override public void getTasks(String routine) {
    String user = Prefs.getString(App.USER, null);
    final Firebase tasksRef = new Firebase(getRoutineUrl(user, routine) + "/Tasks");
    tasksRef.orderByPriority().addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
          List<Task> tasks = new ArrayList<>();
          for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            tasks.add(new Task(snapshot.getKey().toString(), ((Long) snapshot.getValue()).intValue()));
          }
          dispatcher.dispatch(MyActions.GET_TASKS, Keys.TASKS, tasks);
          //rxDataSource.updateDataSet(routines).updateAdapter();
        } catch (NullPointerException e) {

          dispatcher.dispatch(MyActions.GET_TASKS, Keys.ERROR, e);
        }
      }

      @Override public void onCancelled(FirebaseError firebaseError) {
        dispatcher.dispatch(MyActions.GET_TASKS, Keys.ERROR, firebaseError);
      }
    });
  }

  @Override public void createTask(String routine, Task task, int priority) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase(getRoutinesUrl(user));
    routinesRef.child(routine).child("Tasks").child(task.getName()).setValue(task.getMinutes());
  }

  @Override public void updateTasks(String routine, List<Task> tasks) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase(getTasksUrl(user, routine));

    for (int i = 0; i < tasks.size(); i++) {
      routinesRef.child(tasks.get(i).getName()).setPriority(i + 1);
    }
  }

  private String getTasksUrl(String user, String routine) {
    return getRoutinesUrl(user) + "/" + routine + "/Tasks";
  }

  @Override public void updateTask(String routine, Task oldTask, Task newTask, int position) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase(getTasksUrl(user, routine));

    if (oldTask.getName().equals(newTask.getName())) {
      if (oldTask.getMinutes() == newTask.getMinutes()) {
        return;
      } else {
        routinesRef.child(oldTask.getName()).setValue(newTask.getMinutes(), position + 1);
      }
    } else {
      routinesRef.child(oldTask.getName()).removeValue();
      routinesRef.child(newTask.getName()).setValue(newTask.getMinutes(), position + 1);
    }
    dispatcher.dispatch(MyActions.UPDATE_TASK, Keys.TASK, newTask);
  }

  //Break methods
  @Override public void updateBreakInterval(String routine, int interval) {
    String user = Prefs.getString(App.USER, null);
    final Firebase breakRef = new Firebase(getBreakUrl(user, routine));
    breakRef.setValue(interval);
  }

  private String getBreakUrl(String user, String routine) {
    return getRoutinesUrl(user) + "/" + routine + "/Break";
  }

  @Override public void getBreakInterval(String routine) {
    String user = Prefs.getString(App.USER, null);
    final Firebase breakRef = new Firebase(getBreakUrl(user, routine));
    breakRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        String breakInterval = String.valueOf(dataSnapshot.getValue());
        dispatcher.dispatch(MyActions.GET_BREAK_INTERVAL, Keys.BREAK, breakInterval);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {

      }
    });
  }

}