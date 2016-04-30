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
import java.util.Calendar;
import java.util.List;

public class ActionsCreator implements MyActions {

  private static ActionsCreator instance;
  final Dispatcher dispatcher;

  final String currentDate;

  private String getDateString() {
    Calendar calendar = Calendar.getInstance();
    return calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH) + "" + calendar.get(Calendar.DAY_OF_MONTH);
  }

  public ActionsCreator(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.currentDate = getDateString();
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
    final Firebase tasksRef = new Firebase("https://routineplan.firebaseio.com/" + user + "/" + routine);
    tasksRef.addValueEventListener(new ValueEventListener() {
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

  @Override public void createTask(String routine, String task, String time) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user);
    routinesRef.child(routine).child(task).setValue(time);
  }

  @Override public void createRoutine(String name) {
    String user = Prefs.getString(App.USER, null);
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/" + user);
    routinesRef.child(name).setValue(0);
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