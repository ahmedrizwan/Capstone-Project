package com.minimize.android.routineplan.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.activity.TasksActivity;
import com.minimize.android.routineplan.databinding.FragmentRoutinesBinding;
import com.minimize.android.routineplan.databinding.ItemRoutineBinding;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.SimpleViewHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class RoutinesFragment extends BaseFragment {

  FragmentRoutinesBinding mBinding;

  @Nullable @Override public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {
    final String android_id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_routines, container, false);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(getContext()));

    final RxDataSource<String> rxDataSource = new RxDataSource<>(Collections.<String>emptyList());
    rxDataSource.<ItemRoutineBinding>bindRecyclerView(mBinding.recyclerViewRoutines, R.layout.item_routine).subscribe(
        new Action1<SimpleViewHolder<String, ItemRoutineBinding>>() {
          @Override public void call(SimpleViewHolder<String, ItemRoutineBinding> viewHolder) {
            final ItemRoutineBinding viewDataBinding = viewHolder.getViewDataBinding();
            viewDataBinding.textView.setText(viewHolder.getItem());
            viewDataBinding.getRoot().setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                startActivity(new Intent(getContext(), TasksActivity.class));
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
                  mFirebaseRef.child("Routines").child(android_id).child(input.toString().trim()).setValue("No Tasks");
                }
              }
            })
            .show();
      }
    });

    mFirebaseRef.child("Routines").child(android_id).addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
          Timber.e("onDataChange : " + dataSnapshot.getValue().toString());
          List<String> routines = new ArrayList<String>();
          for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            routines.add(snapshot.getKey());
          }
          rxDataSource.updateDataSet(routines).updateAdapter();
        } catch (NullPointerException e) {

        }
      }

      @Override public void onCancelled(FirebaseError firebaseError) {

      }
    });

    return mBinding.getRoot();
  }
}
