package com.example.test.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.LoginResponse;
import com.example.test.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String LOGIN_URL = "http://150.109.6.243:8787/user/login";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // 检查是否已经登录
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        if (token != null) {
            // 已经登录，跳转到 HomeActivity
            Intent intent = new Intent(Login.this, HomeActivity.class);
            startActivity(intent);
            finish();  // 结束当前活动
            return;
        }

        EditText usernameEditText = findViewById(R.id.editTextUsername);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        Button buttonGoToMain = findViewById(R.id.buttonLogin);
        TextView buttonGoToRegister = findViewById(R.id.buttonRegister);

        gson = new Gson();

        buttonGoToMain.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 使用 Gson 构建 JSON 请求体
            Map<String, String> loginMap = new HashMap<>();
            loginMap.put("username", username);
            loginMap.put("password", password);
            String json = gson.toJson(loginMap);

            // 创建请求体
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);

            // 创建一个 POST 请求
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .build();

            // 执行请求
            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 请求失败的处理
                    runOnUiThread(() -> {
                        // 更新 UI 必须在主线程中执行
                        Toast.makeText(Login.this, "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        LoginResponse loginResponse = gson.fromJson(responseData, LoginResponse.class);

                        runOnUiThread(() -> {
                            // 根据返回的 code 判断登录是否成功
                            if (loginResponse.getCode() == 0) {
                                // 登录成功，存储token
                                String token = loginResponse.getData().getToken();
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("token", token);
                                editor.apply();

                                // 跳转到HomeActivity
                                Intent intent = new Intent(Login.this, HomeActivity.class);
                                startActivity(intent);
                                finish();  // 结束当前活动
                            } else {
                                // 登录失败，显示错误消息
                                Toast.makeText(Login.this, "登录失败: " + loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            // 请求失败，显示错误码
                            Toast.makeText(Login.this, "登录失败，错误码：" + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });

        buttonGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onClick(View v) {
        // 实现 View.OnClickListener 接口
    }
}
