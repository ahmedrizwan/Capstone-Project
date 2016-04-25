package com.minimize.android.routineplan.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.ActivityTasksBinding;
import com.minimize.android.routineplan.databinding.ItemRoutineBinding;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.SimpleViewHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rx.functions.Action1;

/**
 * Created by ahmedrizwan on 25/04/2016.
 */
public class TasksActivity extends BaseActivity {
  ActivityTasksBinding mBinding;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);
    mBinding.recyclerViewRoutines.setLayoutManager(new LinearLayoutManager(this));

    final String routine = getIntent().getStringExtra("Routine");
    final Firebase tasksRef = new Firebase("https://routineplan.firebaseio.com/routines/" + android_id + "/" + routine);
    final RxDataSource<String> rxDataSource = new RxDataSource<>(Collections.<String>emptyList());
    rxDataSource.<ItemRoutineBinding>bindRecyclerView(mBinding.recyclerViewRoutines, R.layout.item_routine).subscribe(
        new Action1<SimpleViewHolder<String, ItemRoutineBinding>>() {
          @Override public void call(final SimpleViewHolder<String, ItemRoutineBinding> viewHolder) {
            final ItemRoutineBinding viewDataBinding = viewHolder.getViewDataBinding();
            final String item = viewHolder.getItem();
            viewDataBinding.textView.setText(item);
            viewDataBinding.getRoot().setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {

              }
            });
          }
        });

    tasksRef.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
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

    mBinding.fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        new MaterialDialog.Builder(TasksActivity.this).title("Create a new Task")
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input("Tasks's Name", null, new MaterialDialog.InputCallback() {
              @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                // Do something
                if (input.length() > 0) {
                  tasksRef.child(input.toString().trim()).setValue(input.toString().trim());
                }
              }
            })
            .show();
      }
    });
  }
}
