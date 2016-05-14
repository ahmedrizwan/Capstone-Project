package com.minimize.android.routineplan.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.ActivityTasksBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.stores.RoutinesStore;
import com.minimize.android.routineplan.flux.stores.TasksStore;
import com.minimize.android.routineplan.itemhelper.OnItemClick;
import com.minimize.android.routineplan.itemhelper.OnItemDelete;
import com.minimize.android.routineplan.itemhelper.OnItemRename;
import com.minimize.android.routineplan.itemhelper.OnItemsReordered;
import com.minimize.android.routineplan.itemhelper.RecyclerListAdapter;
import com.minimize.android.routineplan.itemhelper.SimpleItemTouchHelperCallback;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.models.Task;
import com.squareup.otto.Subscribe;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.parceler.Parcels;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 25/04/2016.
 */
public class TasksActivity extends BaseActivity {
  ActivityTasksBinding mBinding;
  TasksStore mTasksStore;
  RoutinesStore mRoutinesStore;
  //RxDataSource<Task> rxDataSource;
  RecyclerListAdapter mRecyclerListAdapter;
  private ItemTouchHelper mItemTouchHelper;
  private List<Task> mTasks;

  private Timer mTimer;
  private Routine mRoutine;

  int[] mMinutes = { 30, 60, 90, 120, 150, 180 };
  String[] mDisplayValues = new String[mMinutes.length];

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(this));
    mDisplayValues = convertMinutesToStrings(mMinutes);

    mTasksStore = TasksStore.get(mDispatcher);
    mRoutinesStore = RoutinesStore.get(this, mDispatcher);

    ActionBar supportActionBar = getSupportActionBar();
    mRoutine = Parcels.unwrap(getIntent().getParcelableExtra(Keys.ROUTINE));

    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
      supportActionBar.setTitle(mRoutine.getName() + " Tasks");
    }

    mRecyclerListAdapter = new RecyclerListAdapter(Collections.EMPTY_LIST, new OnItemClick<Task>() {
      @Override public void onItemClick(final Task item, final int position) {
        //Launch material dialog
        final EditText editTextTaskName = new EditText(TasksActivity.this);
        editTextTaskName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editTextTaskName.setHint("Task Name");
        editTextTaskName.setText(item.getName());
        editTextTaskName.setSingleLine();

        final NumberPicker numberPicker = new NumberPicker(TasksActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        numberPicker.setLayoutParams(params);

        numberPicker.setDisplayedValues(mDisplayValues);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(mMinutes.length - 1);
        numberPicker.setValue(Arrays.asList(mDisplayValues).indexOf(convertMinutesToString(item.getMinutes())));

        LinearLayout linearLayout = new LinearLayout(TasksActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editTextTaskName);
        linearLayout.addView(numberPicker);

        new MaterialDialog.Builder(TasksActivity.this).title("Edit Task")
            .customView(linearLayout, true)
            .positiveText("Update")
            .negativeText("Cancel")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String time = numberPicker.getDisplayedValues()[numberPicker.getValue()];
                String taskName = editTextTaskName.getText().toString();
                Task task = new Task(taskName, convertStringToMinutes(time));
                if (taskName.length() > 0) {
                  //mActionsCreator.upd(mRoutine, task, mTasks.size());
                  mActionsCreator.updateTask(mRoutine.getName(), item, task, position);
                }
              }
            })
            .show();
      }
    }, new OnItemsReordered() {

      @Override public void onItemsReordered(final List<Task> tasks) {
        if (mTimer != null) {
          mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
          @Override public void run() {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                mActionsCreator.updateTasks(mRoutine.getName(), tasks);
              }
            });
          }
        }, 1000);
      }
    }, new OnItemRename() {
      @Override public void onItemRename(final Task item) {

      }
    }, new OnItemDelete() {
      @Override public void onItemDelete(Task item) {
        //On Item Delete
        mActionsCreator.deleteTask(mRoutine.getName(), item);
      }
    });

    mBinding.recyclerViewRoutines.setAdapter(mRecyclerListAdapter);
    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mRecyclerListAdapter);

    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(mBinding.recyclerViewRoutines);

    //mBinding.fab.setOnClickListener(new View.OnClickListener() {
    //  @Override public void onClick(View v) {
    //    final EditText editTextTaskName = new EditText(TasksActivity.this);
    //    editTextTaskName.setLayoutParams(
    //        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    //    editTextTaskName.setHint("Task Name");
    //
    //    final NumberPicker numberPicker = new NumberPicker(TasksActivity.this);
    //    LinearLayout.LayoutParams params =
    //        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //    params.gravity = Gravity.CENTER;
    //    numberPicker.setLayoutParams(params);
    //
    //    numberPicker.setDisplayedValues(convertMinutesToStrings(mMinutes));
    //
    //    numberPicker.setMinValue(0);
    //    numberPicker.setMaxValue(mMinutes.length - 1);
    //    LinearLayout linearLayout = new LinearLayout(TasksActivity.this);
    //    linearLayout.setOrientation(LinearLayout.VERTICAL);
    //    linearLayout.addView(editTextTaskName);
    //    linearLayout.addView(numberPicker);
    //
    //    new MaterialDialog.Builder(TasksActivity.this).title("Create a new Task")
    //        .customView(linearLayout, true)
    //        .positiveText("Create")
    //        .negativeText("Cancel")
    //        .onPositive(new MaterialDialog.SingleButtonCallback() {
    //          @Override public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
    //            String time = numberPicker.getDisplayedValues()[numberPicker.getValue()];
    //            String taskName = editTextTaskName.getText().toString();
    //            Task task = new Task(taskName, convertStringToMinutes(time));
    //            if (taskName.length() > 0) {
    //              mActionsCreator.createTask(mRoutine, task, mTasks.size());
    //            }
    //          }
    //        })
    //        .show();
    //  }
    //});

    mBinding.play.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        //Start Play Activity passing in Routine item
        Intent intent = new Intent(TasksActivity.this, PlayActivity.class);
        intent.putExtra(Keys.ROUTINE, Parcels.wrap(mRoutine));
        startActivity(intent);
      }
    });

    mBinding.radioGroupBreak.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio_five_minutes) {
          mActionsCreator.updateBreakInterval(mRoutine.getName(), 5);
        } else if (checkedId == R.id.radio_ten_minutes) {
          mActionsCreator.updateBreakInterval(mRoutine.getName(), 10);
        }
      }
    });
    mActionsCreator.getTasks(mRoutine.getName());
    mActionsCreator.getBreakInterval(mRoutine.getName());
  }

  private void startRoutine(Routine item) {
    Intent intent = new Intent(this, PlayActivity.class);
    intent.putExtra(Keys.ROUTINE, Parcels.wrap(item));
    startActivity(intent);
  }

  public static String convertMinutesToString(int minutes) {
    String displayValue = "";
    DecimalFormat decimalFormat = new DecimalFormat("#.#");
    if (minutes < 60) {
      //30 Mins
      displayValue = String.valueOf(minutes) + " Mins";
    } else {
      float value = (float) minutes / 60;
      if (value == 1) {
        displayValue = String.valueOf(decimalFormat.format(value)) + " Hour";
      } else {
        displayValue = String.valueOf(decimalFormat.format(value)) + " Hours";
      }
    }
    return displayValue;
  }

  public int convertStringToMinutes(String time) {
    int index = Arrays.asList(mDisplayValues).indexOf(time);

    return mMinutes[index];
  }

  public String[] convertMinutesToStrings(int[] minutes) {
    String[] valuesAsString = new String[minutes.length];
    DecimalFormat decimalFormat = new DecimalFormat("#.#");
    for (int i = 0; i < minutes.length; i++) {
      if (minutes[i] < 60) {
        //30 Mins
        valuesAsString[i] = String.valueOf(minutes[i]) + " Mins";
      } else {
        float value = (float) minutes[i] / 60;
        if (value == 1) {
          valuesAsString[i] = String.valueOf(decimalFormat.format(value)) + " Hour";
        } else {
          valuesAsString[i] = String.valueOf(decimalFormat.format(value)) + " Hours";
        }
      }
    }
    return valuesAsString;
  }

  @Override protected void onResume() {
    super.onResume();
    mDispatcher.register(this);
    mDispatcher.register(mTasksStore);
    mDispatcher.register(mRoutinesStore);
  }

  @Override protected void onPause() {
    super.onPause();
    mDispatcher.unregister(this);
    mDispatcher.unregister(mTasksStore);
    mDispatcher.unregister(mRoutinesStore);
  }

  @Subscribe public void onTasksRetrieved(TasksStore.TasksEvent tasksEvent) {
    mTasks = tasksEvent.mTasks;
    mRecyclerListAdapter.updateDataSet(mTasks);
    if (mTasks.size() == 0) {
      mBinding.recyclerViewRoutines.setVisibility(View.GONE);
      mBinding.emptyView.setVisibility(View.VISIBLE);
    } else {
      mBinding.recyclerViewRoutines.setVisibility(View.VISIBLE);
      mBinding.emptyView.setVisibility(View.GONE);
    }
  }

  @Subscribe public void onTasksError(TasksStore.TasksError tasksError) {
    Timber.e("onTasksError : " + tasksError.mErrorMessage);
  }

  @Subscribe public void onBreakInterval(TasksStore.BreakIntervalEvent breakIntervalEvent) {
    if (breakIntervalEvent.breakInterval.equals("5")) {
      mBinding.radioFiveMinutes.setChecked(true);
    } else if (breakIntervalEvent.breakInterval.equals("10")) {
      mBinding.radioTenMinutes.setChecked(true);
    }
  }

  @Subscribe public void onBreakError(TasksStore.BreakIntervalError breakIntervalError) {
    Timber.e("onBreakError : " + breakIntervalError.mError);
  }

  @Subscribe public void onTaskUpdate(TasksStore.TaskUpdateEvent taskUpdateEvent) {
    mActionsCreator.getTasks(mRoutine.getName());
  }

  @Subscribe public void onRoutineRename(RoutinesStore.RoutineRenameEvent routineRenameEvent) {
    mRoutine.setName(routineRenameEvent.mNewName);
    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setTitle(routineRenameEvent.mNewName + " Tasks");
    }
    mActionsCreator.getTasks(routineRenameEvent.mNewName);
    mActionsCreator.getBreakInterval(routineRenameEvent.mNewName);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
      case R.id.add:
        final EditText editTextTaskName = new EditText(TasksActivity.this);
        editTextTaskName.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editTextTaskName.setHint("Task Name");
        editTextTaskName.setSingleLine();

        final NumberPicker numberPicker = new NumberPicker(TasksActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        numberPicker.setLayoutParams(params);

        numberPicker.setDisplayedValues(convertMinutesToStrings(mMinutes));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(mMinutes.length - 1);
        LinearLayout linearLayout = new LinearLayout(TasksActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editTextTaskName);
        linearLayout.addView(numberPicker);

        new MaterialDialog.Builder(TasksActivity.this).title("Create a new Task")
            .customView(linearLayout, true)
            .positiveText("Create")
            .negativeText("Cancel")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String time = numberPicker.getDisplayedValues()[numberPicker.getValue()];
                String taskName = editTextTaskName.getText().toString();
                Task task = new Task(taskName, convertStringToMinutes(time));
                if (taskName.length() > 0) {
                  mActionsCreator.createTask(mRoutine.getName(), task, mTasks.size());
                }
              }
            })
            .show();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.task_activity_menu, menu);

    return super.onCreateOptionsMenu(menu);
  }
}
