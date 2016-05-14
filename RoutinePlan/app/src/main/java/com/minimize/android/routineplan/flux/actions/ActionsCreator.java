package com.minimize.android.routineplan.flux.actions;

import android.content.ContentValues;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.models.Task;
import com.minimize.android.routineplan.models.TimeAndInfo;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import timber.log.Timber;

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
  public static String getRoutineUrl(String user, String routine) {
    return getRoutinesUrl(user) + "/" + routine;
  }

  public static String getRoutinesUrl(String user) {
    return BASE_URL + "/" + user + "/Routines/";
  }

  @Override public void deleteTask(String routine, Task task) {
    String user = Prefs.getString(App.USER, null);
    String tasksUrl = getTasksUrl(user, routine);
    Timber.e("deleteTask : " + tasksUrl);
    final Firebase taskRef = new Firebase(tasksUrl);
    taskRef.child(task.getName()).removeValue();
  }

  @Override public void getRoutines() {
    String user = Prefs.getString(App.USER, null);
    Timber.e("getRoutines : " + user);
    final ContentValues contentValues = new ContentValues();

    final Firebase routinesRef = new Firebase(getRoutinesUrl(user));
    routinesRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        List<Routine> routines = new ArrayList<Routine>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          int totalMinutes = 0;
          int breakInterval = 5;
          HashMap values = (HashMap) snapshot.getValue();
          breakInterval = ((Long) values.get("Break")).intValue();

          int totalTasks = 0;
          try {
            HashMap tasks = (HashMap) values.get("Tasks");
            totalTasks = tasks.size();
            for (Object minutes : tasks.values()) {
              totalMinutes += ((Long) minutes).intValue();
            }
          } catch (NullPointerException e) {
            //do nothing
          }

          Routine routine = new Routine(snapshot.getKey(), totalMinutes, breakInterval);
          routine.setTotalTasks(totalTasks);

          routines.add(routine);

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

  @Override public void login(final String userEmail) {
    final String user = Prefs.getString(App.USER, null);

    final Firebase userRef = new Firebase(BASE_URL + "/" + user);
    final Firebase newUserRef = new Firebase(BASE_URL + "/");
    userRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        userRef.removeEventListener(this);
        newUserRef.child(userEmail).setValue(dataSnapshot.getValue());
        userRef.removeValue();
        Prefs.putString(App.USER, userEmail);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {

      }
    });
  }

  @Override public void saveHistory(String routine, Task task, String date, String time) {
    final String user = Prefs.getString(App.USER, null);
    final Firebase historyRef = new Firebase(BASE_URL + "/" + user + "/History");
    historyRef.child(date + " " + time).child(routine).setValue(task.getName());
    historyRef.child(date + " " + time).child("Time").setValue(task.getMinutes());
  }

  @Override public void getHistory() {
    final String user = Prefs.getString(App.USER, null);
    final Firebase historyRef = new Firebase(BASE_URL + "/" + user + "/History");
    historyRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        List<TimeAndInfo> timeAndInfos = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          TimeAndInfo timeAndInfo = new TimeAndInfo();

          timeAndInfo.setDateAndTime(snapshot.getKey());

          for (DataSnapshot info : snapshot.getChildren()) {
              if (info.getKey().equals("Time")) {
                timeAndInfo.setTime((long) info.getValue());
                Timber.e("onDataChange : "+timeAndInfo.getTime());
              } else {
                timeAndInfo.setRoutine(info.getKey());
                timeAndInfo.setTaskName((String) info.getValue());
                Timber.e("onDataChange : "+timeAndInfo.getRoutine());
              }

          }
          timeAndInfos.add(timeAndInfo);


        }
        Timber.e("onDataChange : " + timeAndInfos.size());

        dispatcher.dispatch(MyActions.GET_HISTORY, Keys.HISTORY, timeAndInfos);
      }

      @Override public void onCancelled(FirebaseError firebaseError) {

      }
    });
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