package com.minimize.android.routineplan;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.view.View;
import com.firebase.client.FirebaseError;
import com.minimize.android.routineplan.flux.actions.Action;
import com.minimize.android.routineplan.flux.actions.Keys;
import java.net.UnknownHostException;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 26/04/2016.
 */
public class Utility {
  public static String checkForErrorResponse(Action action) {
    Object errorMessage = (Throwable) action.getData().get(Keys.ERROR);
    //Timber.e("checkForErrorResponse : "+errorMessage.getClass().getName());
    if (errorMessage instanceof FirebaseError) {
      String string = ((FirebaseError) errorMessage).getMessage();
      return string;
    } else if (errorMessage instanceof UnknownHostException) {
      Timber.e("checkForErrorResponse : Return");
      return ERROR_NO_INTERNET;
    }

    return "";
  }

  /***
   * Helper method for checking if Pre-Lollipop or not
   *
   * @return True if Sdk version Lollipop and above
   */
  public static boolean isVersionLollipopAndAbove() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  /***
   * Helper method that loads fragments with shared elements animation (also considers if the
   * layout
   * is TwoPane or not)
   * Loads the Fragment normally if Pre-Lollipop.
   *
   * @param isTwoPane Tablet with two panes
   * @param fromFragment From fragment
   * @param toFragment To Fragment
   * @param container The id for the container for these Fragments
   * @param views Shared views
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP) public static void launchFragmentWithSharedElements(
      final boolean isTwoPane, final Fragment fromFragment, final Fragment toFragment, final int container,
      final View... views) {
    if (isVersionLollipopAndAbove()) {
      FragmentTransaction fragmentTransaction =
          fromFragment.getActivity().getSupportFragmentManager().beginTransaction();
      if (!isTwoPane) {
        final TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new ChangeImageTransform());
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.addTransition(new ChangeTransform());
        transitionSet.setDuration(500);
        fromFragment.setSharedElementReturnTransition(transitionSet);
        fromFragment.setSharedElementEnterTransition(transitionSet);
        toFragment.setSharedElementEnterTransition(transitionSet);
        toFragment.setSharedElementReturnTransition(transitionSet);
        for (View view : views) {
          fragmentTransaction.addSharedElement(view, view.getTransitionName());
        }

        fragmentTransaction.replace(container, toFragment).addToBackStack(null).commit();
      } else {
        fragmentTransaction.replace(container, toFragment).commit();
      }
    } else {
      if (isTwoPane) {
        fromFragment.getActivity()
            .getSupportFragmentManager()
            .beginTransaction()
            .replace(container, toFragment)
            .commit();
      } else {
        launchFragment(((AppCompatActivity) fromFragment.getActivity()), container, toFragment);
      }
    }
  }

  /***
   * Helper method to launch Fragments
   *
   * @param activity Parent Activity
   * @param containerId The id of the container
   * @param fragment The fragment to load
   */
  public static void launchFragment(final AppCompatActivity activity, int containerId, final Fragment fragment) {
    activity.getSupportFragmentManager()
        .beginTransaction()
        .replace(containerId, fragment)
        .addToBackStack(null)
        .commit();
  }

  public static final String ERROR_NO_INTERNET = "Unable to connect to Internet!";
}
