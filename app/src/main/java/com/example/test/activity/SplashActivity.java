package com.example.test.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查是否已经登录
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        if (token != null) {
            // 已经登录，跳转到 HomeActivity
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            // 未登录，跳转到 LoginActivity
            Intent intent = new Intent(SplashActivity.this, Login.class);
            startActivity(intent);
        }

        finish();  // 结束当前活动
    }
}
