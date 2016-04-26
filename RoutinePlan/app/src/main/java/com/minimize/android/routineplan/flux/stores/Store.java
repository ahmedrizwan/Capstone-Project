package com.minimize.android.routineplan.flux.stores;

import com.minimize.android.routineplan.flux.actions.Action;
import com.minimize.android.routineplan.flux.dispatcher.Dispatcher;

public abstract class Store {

  final Dispatcher dispatcher;

  protected Store(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  void emitStoreChange(StoreChangeEvent storeChangeEvent) {
    dispatcher.emitChange(storeChangeEvent);
  }

  public abstract void onAction(Action action);

  public interface StoreChangeEvent {
  }
}