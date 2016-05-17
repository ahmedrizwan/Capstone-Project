package com.minimize.android.routineplan.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GcmPubSub;
import com.minimize.android.routineplan.App;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.FragmentUserBinding;
import com.minimize.android.routineplan.flux.actions.Keys;
import com.pixplicity.easyprefs.library.Prefs;
import java.io.IOException;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class UserFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener {
  private static final int RC_SIGN_IN = 1001;
  FragmentUserBinding mBinding;
  private GoogleApiClient mGoogleApiClient;

  @Nullable @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

    mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    mBinding.logout.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        signOut();
      }
    });
    if (Prefs.getBoolean(Keys.LOGGED_IN, false)) {
      //make logout button visible
      mBinding.logout.setVisibility(View.VISIBLE);
      mBinding.signInButton.setVisibility(View.GONE);
      //set image
      Glide.with(getContext()).load(Prefs.getString(Keys.PHOTO_URL, null)).into(mBinding.profileImage);
      //set name
      mBinding.userName.setText(Prefs.getString(Keys.NAME, null));

    } else {

      //make logout button gone
      mBinding.logout.setVisibility(View.GONE);
      mBinding.signInButton.setVisibility(View.VISIBLE);

      final SignInButton signInButton = (SignInButton) mBinding.getRoot().findViewById(R.id.sign_in_button);
      signInButton.setSize(SignInButton.SIZE_STANDARD);
      signInButton.setScopes(gso.getScopeArray());

      signInButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          signIn();
        }
      });
    }
    mBinding.about.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        new MaterialDialog.Builder(getContext())
            .title(R.string.app_name)
            .content("Capstone Project - Udacity Nanodegree\nBy: Ahmed Rizwan")
            .positiveText("Close")
            .show();
      }
    });

    mBinding.preferences.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext()).title("Change Theme")
            .customView(R.layout.layout_color_picker, false)
            .positiveText("Close")
            .show();
        View view = dialog.getCustomView();
        View green = view.findViewById(R.id.green);
        View blue = view.findViewById(R.id.blue);
        View orange = view.findViewById(R.id.orange);
        View red = view.findViewById(R.id.red);

        green.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            //set theme to green
            setTheme(R.style.AppTheme_GreenTheme);
            getActivity().recreate();
            dialog.dismiss();
          }
        });
        blue.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            //set theme to blue
            setTheme(R.style.AppTheme_BlueTheme);
            getActivity().recreate();
            dialog.dismiss();
          }
        });
        orange.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            setTheme(R.style.AppTheme_OrangeTheme);
            getActivity().recreate();
            dialog.dismiss();
          }
        });
        red.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            setTheme(R.style.AppTheme);
            getActivity().recreate();
            dialog.dismiss();
          }
        });
      }
    });


    return mBinding.getRoot();
  }

  public static void setTheme(int theme) {
    Prefs.putInt(Keys.THEME, theme);
  }

  private void signIn() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  private void signOut() {
    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
      @Override public void onResult(Status status) {
        // ...
        Timber.e("onResult : Logout");
        Prefs.putBoolean(Keys.LOGGED_IN, false);
        Prefs.putString(Keys.NAME, "Awesome User");
        Prefs.putString(Keys.PHOTO_URL, null);
        GcmPubSub pubSub = GcmPubSub.getInstance(getContext());
        //un-sub to topic
        try {
          pubSub.unsubscribe(Prefs.getString(App.GCM_TOKEN,""), "/topics/" + Prefs.getString(App.USER, ""));
        } catch (IOException e) {
          e.printStackTrace();
        }

        String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        Prefs.putString(App.USER, android_id);

        //Re-sub to topic
        try {
          pubSub.subscribe(Prefs.getString(App.GCM_TOKEN,""), "/topics/" + Prefs.getString(App.USER, ""), null);
        } catch (IOException e) {
          e.printStackTrace();
        }

        mBinding.logout.setVisibility(View.GONE);
        mBinding.signInButton.setVisibility(View.VISIBLE);
        mBinding.profileImage.setImageResource(R.drawable.ic_account);
        mBinding.userName.setText("Awesome User");
      }
    });
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      handleSignInResult(result);
    }
  }

  private void handleSignInResult(GoogleSignInResult result) {
    if (result.isSuccess()) {
      Timber.e("handleSignInResult : Success");
      // Signed in successfully, show authenticated UI.
      GoogleSignInAccount acct = result.getSignInAccount();
      Glide.with(getContext()).load(acct.getPhotoUrl()).into(mBinding.profileImage);
      mBinding.userName.setText(acct.getDisplayName());
      Prefs.putBoolean(Keys.LOGGED_IN, true);
      Prefs.putString(Keys.NAME, acct.getDisplayName());
      Prefs.putString(Keys.PHOTO_URL, acct.getPhotoUrl().toString());
      mActionsCreator.login(acct.getId());
      mBinding.signInButton.setVisibility(View.GONE);
      mBinding.logout.setVisibility(View.VISIBLE);
    } else {
      // Signed out, show unauthenticated UI.
      Timber.e("handleSignInResult : Logged Out");
      signOut();
      mBinding.userName.setText("Awesome User");
    }
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }

  @Override public void onStart() {
    super.onStart();
    if (mGoogleApiClient != null)
      mGoogleApiClient.connect();
  }

  @Override public void onStop() {
    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
      mGoogleApiClient.disconnect();
    }
    super.onStop();
  }
}
