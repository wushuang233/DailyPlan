package com.example.test.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText usernameEditText, passwordEditText, emailEditText, ageEditText;
    private Spinner genderSpinner;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // 初始化视图
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        emailEditText = findViewById(R.id.editTextMileBox);
        ageEditText = findViewById(R.id.editTextAge);
        genderSpinner = findViewById(R.id.spinnerGender);
        registerButton = findViewById(R.id.buttonRegister);

        // 设置性别选择框的数据适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // 为注册按钮设置点击监听器
        registerButton.setOnClickListener(this);

        // 为“已有账号？”文本设置点击监听器
        TextView goToLoginTextView = findViewById(R.id.login);
        goToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到登录页面
                Intent intent = new Intent(RegisterActivity.this, Login.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonRegister) {
            // 调用方法执行注册
            performRegistration();
        }
    }

    private void performRegistration() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        int age = 18;
        int gender = 0;

        try {
            age = Integer.parseInt(ageEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "年龄必须是整数", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取Spinner中选定的性别
        String selectedGender = genderSpinner.getSelectedItem().toString();
        // 根据选定的性别设置发送给后端的值
        if (selectedGender.equals("男")) {
            gender = 0;
        } else {
            gender = 1;
        }

        // 构建JSON对象
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            jsonObject.put("email", email);
            jsonObject.put("sex", gender);
            jsonObject.put("age", age);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 准备请求体
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        // 准备请求
        Request request = new Request.Builder()
                .url("http://150.109.6.243:8787/user/add") // 替换为您实际的注册端点URL
                .post(requestBody)
                .build();

        // 异步执行请求
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 处理失败
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // 处理响应
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        // 注册成功
                        String userId = jsonObject.getJSONObject("data").getString("userId");
                        // 如有需要，可处理token
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                // 跳转到登录页面
                                Intent intent = new Intent(RegisterActivity.this, Login.class);
                                startActivity(intent);
                            }
                        });
                    } else {
                        // 注册失败
                        final String message = jsonObject.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "注册失败：" + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
