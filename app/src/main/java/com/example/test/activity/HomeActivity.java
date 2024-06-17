package com.example.test.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.test.R;
import com.example.test.fragment.FocusFragment;
import com.example.test.fragment.HomeFragment;
import com.example.test.fragment.MineFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

public class HomeActivity extends AppCompatActivity {

    private FocusFragment focusFragment;
    private HomeFragment homeFragment;
    private MineFragment mineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav);

        focusFragment = new FocusFragment();
        homeFragment = new HomeFragment();
        mineFragment = new MineFragment();

        // 设置初始 Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_focus) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, focusFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.navigation_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.navigation_mine) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mineFragment).commit();
                    return true;
                }
                return false;
            }
        });
    }
}
