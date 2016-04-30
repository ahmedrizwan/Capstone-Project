package com.minimize.android.routineplan.itemhelper;

/**
 * Created by ahmedrizwan on 30/04/2016.
 */

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.minimize.android.routineplan.R;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>
    implements ItemTouchHelperAdapter {

  private final List<String> mItems = new ArrayList<>();
  private final Callable mOnItemClick;
  private final Callable mOnItemLongClick;

  public RecyclerListAdapter(List<String> items, Callable onItemClick, Callable onItemLongClick) {
    mOnItemClick = onItemClick;
    mOnItemLongClick = onItemLongClick;
    mItems.addAll(items);
  }

  @Override public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
    ItemViewHolder itemViewHolder = new ItemViewHolder(view);
    return itemViewHolder;
  }

  @Override public void onBindViewHolder(final ItemViewHolder holder, int position) {
    holder.textView.setText(mItems.get(position));
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        try {
          mOnItemClick.call();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override public void onItemDismiss(int position) {
    mItems.remove(position);
    notifyItemRemoved(position);
  }

  @Override public boolean onItemMove(int fromPosition, int toPosition) {
    String prev = mItems.remove(fromPosition);
    mItems.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
    notifyItemMoved(fromPosition, toPosition);
    return false;
  }

  public void updateDataSet(List<String> items) {
    mItems.clear();
    mItems.addAll(items);
  }

  @Override public int getItemCount() {
    return mItems.size();
  }

  public static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

    public final TextView textView;

    public ItemViewHolder(View itemView) {
      super(itemView);
      textView = (TextView) itemView.findViewById(R.id.textView);
    }

    @Override public void onItemSelected() {
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override public void onItemClear() {
      itemView.setBackgroundColor(0);
    }
  }
}