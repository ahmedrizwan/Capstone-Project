package com.minimize.android.routineplan.flux.actions;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.minimize.android.routineplan.Task;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import timber.log.Timber;

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

  @Override public void getRoutines(String user) {
    Timber.e("getRoutines : YOOOO");

    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/routines/" + user);
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

  @Override public void getTasks(String user, String routine) {
    final Firebase tasksRef = new Firebase("https://routineplan.firebaseio.com/routines/" + user + "/" + routine);
    tasksRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
          List<Task> tasks = new ArrayList<>();
          for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            tasks.add(new Task(snapshot.getKey().toString(),snapshot.getValue().toString()));
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

  @Override public void createRoutine(String user, String name) {
    final Firebase routinesRef = new Firebase("https://routineplan.firebaseio.com/routines/" + user);
    routinesRef.child(name).setValue(0);
  }
}