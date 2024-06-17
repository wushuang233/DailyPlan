package com.example.test.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.UserInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MineActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvSex;
    private TextView tvAge;
    private TextView tvPlanNumber;
    private TextView tvIntroduction;
    private Gson gson;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_mine);

        // 从SharedPreferences中获取token
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        tvUsername = findViewById(R.id.tv_username);
        tvSex = findViewById(R.id.tv_gender_value);
        tvAge = findViewById(R.id.tv_age_value);
        tvPlanNumber = findViewById(R.id.tv_plans_completed_value);
        tvIntroduction = findViewById(R.id.tv_nikename);

        gson = new Gson();
        client = new OkHttpClient();


        fetchUserInfo(token);  // 将token传递给fetchUserInfo方法
    }

    private void fetchUserInfo(String token) {
        String url = "http://150.109.6.243:8787/user/info";
        System.out.println("token: " + token);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)  // 添加Authorization头部
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MineActivity.this, "获取用户信息失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println("Response Data: " + responseData);  // 打印返回的数据
                    UserInfo userInfo = gson.fromJson(responseData, UserInfo.class);
                    if (userInfo.getCode() == 0) {
                        runOnUiThread(() -> {
                            UserInfo.Data data = userInfo.getData();
                            tvUsername.setText(data.getUsername());
                            tvSex.setText(data.getSex() == 1 ? "女" : "男");
                            tvAge.setText(String.valueOf(data.getAge()));
                            tvPlanNumber.setText(String.valueOf(data.getPlanNumber()));
                            tvIntroduction.setText(data.getIntroduction().isEmpty() ? "这个人很懒，什么都没有留下哦~" : data.getIntroduction());
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(MineActivity.this, "获取用户信息失败: " + userInfo.getMsg(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MineActivity.this, "获取用户信息失败，错误码：" + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
