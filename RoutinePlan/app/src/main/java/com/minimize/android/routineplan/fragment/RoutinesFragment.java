package com.minimize.android.routineplan.fragment;

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
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.FragmentRoutinesBinding;
import com.minimize.android.routineplan.databinding.ItemRoutineBinding;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.SimpleViewHolder;
import java.util.ArrayList;
import java.util.List;
import rx.functions.Action1;

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
    List<String> strings = new ArrayList<>();
    strings.add("hello");
    RxDataSource<String> rxDataSource = new RxDataSource<>(strings);
    rxDataSource.repeat(10).<ItemRoutineBinding>bindRecyclerView(mBinding.recyclerViewRoutines,
        R.layout.item_routine).subscribe(new Action1<SimpleViewHolder<String, ItemRoutineBinding>>() {
      @Override public void call(SimpleViewHolder<String, ItemRoutineBinding> viewHolder) {

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
                if (input.length() > 0) mFirebaseRef.child("Routines").child(android_id).child("name").setValue
                    (input.toString().trim());
              }
            })
            .show();
      }
    });

    return mBinding.getRoot();
  }
}
