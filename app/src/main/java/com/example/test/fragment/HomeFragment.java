package com.example.test.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.test.R;
import com.example.test.activity.PlanActivity;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    private PlansPagerAdapter plansPagerAdapter;
    private ViewPager viewPager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the FloatingActionButton
        rootView.findViewById(R.id.fab_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the PlanActivity
                startActivity(new Intent(getActivity(), PlanActivity.class));
            }
        });

        // Set up ViewPager and TabLayout
        plansPagerAdapter = new PlansPagerAdapter(getChildFragmentManager());
        viewPager = rootView.findViewById(R.id.view_pager);
        viewPager.setAdapter(plansPagerAdapter);

        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // Set listener for page changes
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // Refresh data when switching pages
                plansPagerAdapter.refreshFragment(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        return rootView;
    }

    private class PlansPagerAdapter extends FragmentPagerAdapter {
        private PlanListFragment ongoingFragment;
        private PlanListFragment finishedFragment;

        public PlansPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            ongoingFragment = new PlanListFragment("http://150.109.6.243:8787/plan/list", false);
            finishedFragment = new PlanListFragment("http://150.109.6.243:8787/plan/finish", true);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return ongoingFragment;
            } else {
                return finishedFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "进行中";
            } else {
                return "已结束";
            }
        }

        public void refreshFragment(int position) {
            if (position == 0) {
                ongoingFragment.fetchPlanList();
            } else {
                finishedFragment.fetchPlanList();
            }
        }
    }
}
