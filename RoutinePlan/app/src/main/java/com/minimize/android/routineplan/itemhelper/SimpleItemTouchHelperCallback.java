package com.minimize.android.routineplan.itemhelper;

/**
 * Created by ahmedrizwan on 30/04/2016.
 */

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

  private final ItemTouchHelperAdapter mAdapter;

  public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
    mAdapter = adapter;
  }

  @Override public boolean isLongPressDragEnabled() {
    return true;
  }

  @Override public boolean isItemViewSwipeEnabled() {
    return true;
  }

  @Override public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    return makeMovementFlags(dragFlags, 0);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
    mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
    return true;
  }

  @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }

  @Override public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
      itemViewHolder.onItemSelected();
    }

    super.onSelectedChanged(viewHolder, actionState);
  }

  @Override public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    super.clearView(recyclerView, viewHolder);

    ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
    itemViewHolder.onItemClear();
  }
}