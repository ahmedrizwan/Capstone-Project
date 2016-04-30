package com.minimize.android.routineplan.itemhelper;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);


    void onItemDismiss(int position);
}