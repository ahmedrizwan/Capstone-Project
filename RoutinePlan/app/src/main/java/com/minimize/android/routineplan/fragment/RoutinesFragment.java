package com.minimize.android.routineplan.fragment;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.materialdialogs.MaterialDialog;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.MyService;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.activity.TasksActivity;
import com.minimize.android.routineplan.data.DbContract;
import com.minimize.android.routineplan.databinding.FragmentRoutinesBinding;
import com.minimize.android.routineplan.databinding.ItemRoutineBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.stores.RoutinesStore;
import com.minimize.android.routineplan.models.Routine;
import com.minimize.android.routineplan.widget.WidgetProvider;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.SimpleViewHolder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.util.Collections;
import java.util.List;
import org.parceler.Parcels;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class RoutinesFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  FragmentRoutinesBinding mBinding;

  RoutinesStore mRoutinesStore;
  RxDataSource<Routine> rxDataSource;
  private List<Routine> mRoutines;

  MyService mService;
  Bus mBus = new Bus(Bus.DEFAULT_IDENTIFIER);


  @Nullable @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
    getActivity().getSupportLoaderManager().initLoader(0, null, this);

    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_routines, container, false);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(getContext()));

    mRoutinesStore = RoutinesStore.get(getContext(), mDispatcher);

    App application = (App) getActivity().getApplication();
    if (application.isServiceBound()) {
      mService = application.getServiceInstance();
    }

    rxDataSource = new RxDataSource<>(Collections.<Routine>emptyList());
    rxDataSource.<ItemRoutineBinding>bindRecyclerView(mBinding.recyclerViewRoutines, R.layout.item_routine).subscribe(
        new Action1<SimpleViewHolder<Routine, ItemRoutineBinding>>() {
          @Override public void call(final SimpleViewHolder<Routine, ItemRoutineBinding> viewHolder) {
            final ItemRoutineBinding viewDataBinding = viewHolder.getViewDataBinding();
            final Routine item = viewHolder.getItem();

            viewDataBinding.getRoot().setHasTransientState(true);

            if (mService != null && mService.getRoutineState(item.getName()) == MyService.PLAYING) {
              viewDataBinding.textViewRoutineName.setText(item.getName() + " - Playing!");
            } else if (mService != null && mService.getRoutineState(item.getName()) == MyService.PAUSED) {
              viewDataBinding.textViewRoutineName.setText(item.getName() + " - Paused!");
            } else {
              viewDataBinding.textViewRoutineName.setText(item.getName());
            }

            if (item.getTotalMinutes() > 0) {
              viewDataBinding.textViewTaskTime.setText(TasksActivity.convertMinutesToString(item.getTotalMinutes()));
            }

            int totalTasks = item.getTotalTasks();
            if (totalTasks > 0) {
              viewDataBinding.textViewTotalTasks.setText(totalTasks == 1 ? +totalTasks + " Task" : totalTasks + " Tasks");
            }

            viewDataBinding.delete.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                mActionsCreator.deleteRoutine(item.getName());
              }
            });

            viewDataBinding.rename.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                new MaterialDialog.Builder(getContext()).title("Rename " + item.getName() + " Routine")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("Routine's Name", null, new MaterialDialog.InputCallback() {
                      @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        if (input.length() > 0) {
                          mActionsCreator.renameRoutine(item.getName(), input.toString().trim());
                          viewDataBinding.swipeRoutineLayout.close(true);
                        }
                      }
                    })
                    .show();
              }
            });

            viewDataBinding.routineItem.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                Intent intent = new Intent(getContext(), TasksActivity.class);
                intent.putExtra(Keys.ROUTINE, Parcels.wrap(item));
                startActivity(intent);
              }
            });
          }
        });

    mBinding.fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        //mFirebaseRef.child("Routines").child("dummyUserId").child("name").setValue("Programming");
        new MaterialDialog.Builder(getContext()).title("New Routine")
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input("Routine's Name", null, new MaterialDialog.InputCallback() {
              @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                // Do something
                if (input.length() > 0) {
                  mActionsCreator.createRoutine(input.toString().trim(), mRoutines.size());
                }
              }
            })
            .show();
      }
    });

    return mBinding.getRoot();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  @Override public void onResume() {
    super.onResume();
    mDispatcher.register(this);
    mDispatcher.register(mRoutinesStore);
    mActionsCreator.getRoutines();
    mBus.register(this);
    if (mService != null) {
      mService.setBus(mBus);
    }
  }

  @Subscribe public void onRoutinePause(MyService.RoutinePause routinePause){
    rxDataSource.getRxAdapter().notifyDataSetChanged();
  }

  @Subscribe public void onRoutineResume(MyService.RoutineResume routineResume) {
    rxDataSource.getRxAdapter().notifyDataSetChanged();
  }

  @Subscribe public void onRoutineStop(MyService.RoutineStop routineStop) {
    rxDataSource.getRxAdapter().notifyDataSetChanged();
  }

  @Override public void onPause() {
    super.onPause();
    mBus.unregister(this);
    mDispatcher.unregister(this);
    mDispatcher.unregister(mRoutinesStore);
  }

  @Subscribe public void onRoutinesRetrieved(RoutinesStore.RoutinesEvent routinesEvent) {
    mRoutines = routinesEvent.routinesList;
    rxDataSource.updateDataSet(mRoutines).updateAdapter();
    if (mRoutines.size() == 0) {
      mBinding.recyclerViewRoutines.setVisibility(View.GONE);
      mBinding.emptyView.setVisibility(View.VISIBLE);
    } else {
      mBinding.recyclerViewRoutines.setVisibility(View.VISIBLE);
      mBinding.emptyView.setVisibility(View.GONE);
    }

    Intent intent = new Intent(getContext(), WidgetProvider.class);
    intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
    int ids[] = AppWidgetManager.getInstance(getContext()).getAppWidgetIds(new ComponentName(getContext(), WidgetProvider.class));
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    getContext().sendBroadcast(intent);
  }

  @Subscribe public void onRoutinesError(RoutinesStore.RoutinesError routinesError) {
    Timber.e("onRoutinesError : " + routinesError.errorMessage);
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(getContext(), DbContract.Routine.CONTENT_URI, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    try {
      if (data != null) {

        data.moveToFirst();
        do {
          Timber.e("Routine Name : " + data.getString(data.getColumnIndex(DbContract.Routine.COLUMN_NAME)));
          Timber.e("Routine Time : " + data.getString(data.getColumnIndex(DbContract.Routine.COLUMN_TIME)));
        } while (data.moveToNext());
      }
    } catch (Exception e) {

    }
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {

  }
}
