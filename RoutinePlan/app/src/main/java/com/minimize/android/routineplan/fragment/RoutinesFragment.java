package com.minimize.android.routineplan.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.FragmentRoutinesBinding;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class RoutinesFragment extends BaseFragment{

  FragmentRoutinesBinding mBinding;

  @Nullable @Override public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_routines, container, false);

    return mBinding.getRoot();
  }
}
