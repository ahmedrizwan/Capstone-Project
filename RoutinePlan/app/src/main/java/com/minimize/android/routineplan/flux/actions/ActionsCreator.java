package com.minimize.android.routineplan.flux.actions;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.Task;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.List;

public class ActionsCreator implements MyActions {

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

  @Override public void getRoutines() {
    String user = Prefs.getString(App.USER, null);

    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user);
    routinesRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
          List<String> routines = new ArrayList<String>();
          for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            routines.add(snapshot.getKey());
          }
          dispatcher.dispatch(MyActions.GET_ROUTINES, Keys.ROUTINES, routines);
          //rxDataSource.updateDataSet(routines).updateAdapter();
        } catch (NullPointerException e) {
          dispatcher.dispatch(MyActions.GET_ROUTINES, Keys.ERROR, e);
        }
      }

      @Override public void onCancelled(FirebaseError firebaseError) {
        dispatcher.dispatch(MyActions.GET_ROUTINES, Keys.ERROR, firebaseError);
      }
    });
  }

  @Override public void getTasks(String routine) {
    String user = Prefs.getString(App.USER, null);
    final Firebase tasksRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + routine + "/Tasks");
    tasksRef.orderByPriority().addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
          List<Task> tasks = new ArrayList<>();
          for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            tasks.add(new Task(snapshot.getKey().toString(), snapshot.getValue().toString()));
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
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user);
    routinesRef.child(routine).child("Tasks").child(task.getName()).setValue(task.getTime());
  }

  @Override public void updateTasks(String routine, List<Task> tasks) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + routine);
    for (int i = 0; i < tasks.size(); i++) {
      routinesRef.child(tasks.get(i).getName()).setPriority(i + 1);
    }
  }

  @Override public void createRoutine(String name, int priority) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user);
    routinesRef.child(name).child("Break").setValue(5);
  }

  @Override public void updateBreakInterval(String routine, int interval) {
    String user = Prefs.getString(App.USER, null);
    final Firebase breakRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + routine + "/Break");
    breakRef.setValue(interval);
  }

  @Override public void getBreakInterval(String routine) {
    String user = Prefs.getString(App.USER, null);
    final Firebase breakRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + routine + "/Break");
    breakRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        String breakInterval = (String) String.valueOf( dataSnapshot.getValue());
        dispatcher.dispatch(MyActions.GET_BREAK_INTERVAL, Keys.BREAK, breakInterval);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {

      }
    });
  }

  @Override public void renameRoutine(final String oldName, final String newName) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user);
    final Firebase oldTaskRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + oldName);
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
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + routine);
    routinesRef.removeValue();
  }
}