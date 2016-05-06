package com.minimize.android.routineplan.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.databinding.FragmentUserBinding;
import timber.log.Timber;

/**
 * Created by ahmedrizwan on 09/04/2016.
 */
public class UserFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener {
  private static final int RC_SIGN_IN = 1001;
  FragmentUserBinding mBinding;
  private GoogleApiClient mGoogleApiClient;

  @Nullable @Override public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
      @Nullable final Bundle savedInstanceState) {
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();

    mGoogleApiClient = new GoogleApiClient.Builder(getContext())
        .enableAutoManage(getActivity(), this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();

    final SignInButton signInButton = (SignInButton) mBinding.getRoot().findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_STANDARD);
    signInButton.setScopes(gso.getScopeArray());

    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        signIn();
      }
    });
    return mBinding.getRoot();
  }
  private void signIn() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      handleSignInResult(result);
    }
  }

  private void handleSignInResult(GoogleSignInResult result) {
    Timber.e("handleSignInResult:" + result.isSuccess());
    if (result.isSuccess()) {
      // Signed in successfully, show authenticated UI.
      GoogleSignInAccount acct = result.getSignInAccount();
      Timber.e("handleSignInResult : "+acct.getDisplayName());

    } else {
      // Signed out, show unauthenticated UI.
      Timber.e("handleSignInResult : Logged Out");
    }
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }
}
