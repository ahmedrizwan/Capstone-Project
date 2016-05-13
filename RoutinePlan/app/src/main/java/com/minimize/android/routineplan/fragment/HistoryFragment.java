package com.minimize.android.routineplan.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.models.TimeAndInfo;
import com.minimize.android.routineplan.databinding.FragmentHistoryBinding;
import com.minimize.android.routineplan.databinding.ItemHistoryHeadingBinding;
import com.minimize.android.routineplan.databinding.ItemHistoryItemBinding;
import com.minimize.android.routineplan.flux.stores.HistoryStore;
import com.minimize.android.rxrecycleradapter.OnGetItemViewType;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.TypesViewHolder;
import com.minimize.android.rxrecycleradapter.ViewHolderInfo;
import com.squareup.otto.Subscribe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.functions.Action1;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class HistoryFragment extends BaseFragment {
  private static final int HEADING = 0;
  private static final int ITEM = 1;

  FragmentHistoryBinding mBinding;

  RxDataSource<Item> mRxDataSource;
  List<ViewHolderInfo> viewHolderInfos;
  HistoryStore mHistoryStore;

  @Nullable @Override public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);
    mHistoryStore = HistoryStore.get(mDispatcher);
    mRxDataSource = new RxDataSource<Item>(Collections.<Item>emptyList());
    ViewHolderInfo viewHolderInfo = new ViewHolderInfo(R.layout.item_history_heading, HEADING);
    ViewHolderInfo viewHolderInfoItem = new ViewHolderInfo(R.layout.item_history_item, ITEM);
    viewHolderInfos = new ArrayList<>();
    viewHolderInfos.add(viewHolderInfo);
    viewHolderInfos.add(viewHolderInfoItem);

    mActionsCreator.getHistory();

    mBinding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));

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
    HashMap<String, Integer> counts = new HashMap<>();

    HashMap<String, List<Info>> categoryMapper = new HashMap<>();

    //Create a list of items that are categorized
    for (TimeAndInfo history : historyEvent.mHistoryList) {
      String date = history.getDateAndTime().split(" ")[0];
      List<Info> infos = categoryMapper.get(date);
      if (infos == null) {
        infos = new ArrayList<>();
      }
      infos.add(new Info(history.getRoutine(), history.getTaskName(), history.getDateAndTime().split(" ")[1]));
      categoryMapper.put(date, infos);
    }

    final List<Item> myItems = new ArrayList<>();
    for (Map.Entry<String, List<Info>> stringListEntry : categoryMapper.entrySet()) {
      if (!myItems.contains(stringListEntry.getKey())) {
        myItems.add(new Heading(stringListEntry.getKey()));
      }

      List<Info> value = stringListEntry.getValue();
      for (Info info : value) {
        myItems.add(info);
      }
    }

    mRxDataSource.bindRecyclerView(mBinding.recyclerViewHistory, viewHolderInfos, new OnGetItemViewType() {
      @Override public int getItemViewType(int position) {
        if (myItems.get(position) instanceof Heading) {
          return HEADING;
        } else {
          return ITEM;
        }
      }
    }).subscribe(new Action1<TypesViewHolder<Item>>() {
      @Override public void call(TypesViewHolder<Item> itemTypesViewHolder) {
        Item item = itemTypesViewHolder.getItem();
        if (item instanceof Heading) {
          ((ItemHistoryHeadingBinding) itemTypesViewHolder.getViewDataBinding()).heading.setText(((Heading) item).date);
        } else {
          Info info = ((Info) item);
          ItemHistoryItemBinding viewDataBinding = (ItemHistoryItemBinding) itemTypesViewHolder.getViewDataBinding();
          viewDataBinding.routineTask.setText(info.routine + " - " + info.task);
          viewDataBinding.time.setText(info.time);
        }
      }
    });

    mRxDataSource.updateDataSet(myItems).updateAdapter();
  }

  public static class Item {

  }

  public static class Heading extends Item {
    public String date;

    public Heading(String date) {
      this.date = date;
    }
  }

  public static class Info extends Item {
    public String routine;
    public String task;
    public String time;

    public Info(String routine, String task, String time) {
      this.routine = routine;
      this.task = task;
      this.time = time;
    }
  }
}
