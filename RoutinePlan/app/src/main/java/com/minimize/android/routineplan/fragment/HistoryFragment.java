package com.minimize.android.routineplan.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.TimeAndInfo;
import com.minimize.android.routineplan.databinding.FragmentHistoryBinding;
import com.minimize.android.routineplan.flux.stores.HistoryStore;
import com.squareup.otto.Subscribe;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class HistoryFragment extends BaseFragment {
  FragmentHistoryBinding mBinding;

  HistoryStore mHistoryStore;

  @Nullable @Override public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
    mHistoryStore = HistoryStore.get(mDispatcher);

    mActionsCreator.getHistory();
    return mBinding.getRoot();
  }

  @Override public void onPause() {
    super.onPause();
    mDispatcher.unregister(this);
    mDispatcher.unregister(mHistoryStore);
  }

  @Override public void onResume() {
    super.onResume();
    mDispatcher.register(this);
    mDispatcher.register(mHistoryStore);
  }

  @Subscribe public void onHistoryRetrieved(HistoryStore.HistoryEvent historyEvent) {
    for (TimeAndInfo history : historyEvent.mHistoryList) {
      Timber.e("onHistoryRetrieved : " + history.getDateAndTime());
      Timber.e("onHistoryRetrieved : "
          + history.getRoutine()
          + " with Task as "
          + history.getTaskName()
          + " with Time "
          + history.getTime());


    }
  }
}
