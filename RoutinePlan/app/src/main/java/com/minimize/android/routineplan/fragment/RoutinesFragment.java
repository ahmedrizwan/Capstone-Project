package com.minimize.android.routineplan.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.materialdialogs.MaterialDialog;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.Routine;
import com.minimize.android.routineplan.activity.PlayActivity;
import com.minimize.android.routineplan.activity.TasksActivity;
import com.minimize.android.routineplan.databinding.FragmentRoutinesBinding;
import com.minimize.android.routineplan.databinding.ItemRoutineBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.minimize.android.routineplan.flux.stores.RoutinesStore;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.SimpleViewHolder;
import com.squareup.otto.Subscribe;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.parceler.Parcels;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class RoutinesFragment extends BaseFragment {

  FragmentRoutinesBinding mBinding;

  RoutinesStore mRoutinesStore;
  RxDataSource<Routine> rxDataSource;
  private List<Routine> mRoutines;

  @Nullable @Override public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {

    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_routines, container, false);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(getContext()));

    mRoutinesStore = RoutinesStore.get(mDispatcher);

    rxDataSource = new RxDataSource<>(Collections.<Routine>emptyList());
    rxDataSource.<ItemRoutineBinding>bindRecyclerView(mBinding.recyclerViewRoutines, R.layout.item_routine).subscribe(
        new Action1<SimpleViewHolder<Routine, ItemRoutineBinding>>() {
          @Override public void call(final SimpleViewHolder<Routine, ItemRoutineBinding> viewHolder) {
            final ItemRoutineBinding viewDataBinding = viewHolder.getViewDataBinding();
            final Routine item = viewHolder.getItem();
            viewDataBinding.textViewRoutineName.setText(item.getName());
            if (item.getTotalMinutes() > 0) {
              viewDataBinding.textViewTaskTime.setText(TasksActivity.convertMinutesToString(item.getTotalMinutes()));
            } else {
              viewDataBinding.imageViewPlayRoutine.setVisibility(View.GONE);
            }

            viewDataBinding.imageViewPlayRoutine.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                startRoutine(item);
              }
            });

            View root = viewDataBinding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                Intent intent = new Intent(getContext(), TasksActivity.class);
                intent.putExtra("Routine", item.getName());
                startActivity(intent);
              }
            });
            root.setOnLongClickListener(new View.OnLongClickListener() {
              @Override public boolean onLongClick(View v) {
                new MaterialDialog.Builder(getContext()).title("Edit Routine")
                    .items(Arrays.asList("Rename"))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                      @Override
                      public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (text.equals("Rename")) {
                          new MaterialDialog.Builder(getContext()).title("Rename Routine")
                              .inputType(InputType.TYPE_CLASS_TEXT)
                              .input("Routine's Name", null, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                                  // Do something
                                  if (input.length() > 0) {
                                    mActionsCreator.renameRoutine(item.getName(), input.toString().trim());
                                  }
                                }
                              })
                              .show();
                        }
                      }
                    })
                    .show();
                return false;
              }
            });
          }
        });

    mBinding.fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        //mFirebaseRef.child("Routines").child("dummyUserId").child("name").setValue("Programming");
        new MaterialDialog.Builder(getContext()).title("Create a new Routine")
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

  private void startRoutine(Routine item) {
    Intent intent = new Intent(getContext(), PlayActivity.class);
    intent.putExtra(Keys.ROUTINE, Parcels.wrap(item));
    startActivity(intent);
  }

  @Override public void onResume() {
    super.onResume();
    mDispatcher.register(this);
    mDispatcher.register(mRoutinesStore);
    mActionsCreator.getRoutines();
  }

  @Override public void onPause() {
    super.onPause();
    mDispatcher.unregister(this);
    mDispatcher.unregister(mRoutinesStore);
  }

  @Subscribe public void onRoutinesRetrieved(RoutinesStore.RoutinesEvent routinesEvent) {
    mRoutines = routinesEvent.routinesList;
    rxDataSource.updateDataSet(mRoutines).updateAdapter();
  }

  @Subscribe public void onRoutinesError(RoutinesStore.RoutinesError routinesError) {
    Timber.e("onRoutinesError : " + routinesError.errorMessage);
  }
}
