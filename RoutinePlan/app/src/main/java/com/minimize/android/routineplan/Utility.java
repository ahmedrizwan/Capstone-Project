package com.minimize.android.routineplan;

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
    Timber.e("checkForErrorResponse : BeforeCheck");
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

  public static final String ERROR_NO_INTERNET = "Unable to connect to Internet!";
}
