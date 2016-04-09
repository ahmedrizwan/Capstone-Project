package com.minimize.android.routineplan.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.minimize.android.routineplan.R;
import com.minimize.android.routineplan.fragment.BaseFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;
import java.util.ArrayList;
import java.util.List;

public class ContainerActivity extends BaseActivity {

  private BottomBar mBottomBar;
  private FragNavController mNavController;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);
    List<Fragment> fragments = new ArrayList<>(5);

    fragments.add(new BaseFragment());
    fragments.add(new BaseFragment());
    fragments.add(new BaseFragment());

    mNavController =
        new FragNavController(getSupportFragmentManager(), R.id.fragment_container, fragments);

    mBottomBar = BottomBar.attach(this, savedInstanceState);
    mBottomBar.setItems(new BottomBarTab(R.drawable.routines, "Routines"),
        new BottomBarTab(R.drawable.history, "History"), new BottomBarTab(R.drawable.user, "User"));
    // Listen for tab changes
    mBottomBar.setOnTabClickListener(new OnTabClickListener() {
      @Override
      public void onTabSelected(int position) {
        // The user selected a tab at the specified position
        switch (position) {
          case 0:
            mNavController.switchTab(FragNavController.TAB1);
            break;
          case 1:
            mNavController.switchTab(FragNavController.TAB2);
            break;
          case 2:
            mNavController.switchTab(FragNavController.TAB3);
            break;
        }

      }

      @Override
      public void onTabReSelected(int position) {
        // The user reselected a tab at the specified position!
        mNavController.clearStack();
      }
    });
  }
}
