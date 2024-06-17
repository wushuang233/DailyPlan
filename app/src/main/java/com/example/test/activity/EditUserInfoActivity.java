package com.example.test.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditUserInfoActivity extends AppCompatActivity {

    private EditText etIntroduction;
    private EditText etAge;
    private RadioGroup rgSex;
    private OkHttpClient client;
    private Gson gson;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        etIntroduction = findViewById(R.id.et_introduction);
        etAge = findViewById(R.id.et_age);
        rgSex = findViewById(R.id.rg_sex);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnBack = findViewById(R.id.btn_back); // 获取返回按钮

        client = new OkHttpClient();
        gson = new Gson();

        // 从SharedPreferences中获取token
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);

        btnSave.setOnClickListener(v -> updateUserInfo());
        btnBack.setOnClickListener(v -> finish()); // 设置返回按钮的点击事件
    }

    private void updateUserInfo() {
        String introduction = etIntroduction.getText().toString();
        String age = etAge.getText().toString();
        int selectedSexId = rgSex.getCheckedRadioButtonId();

        if (selectedSexId == -1) {
            runOnUiThread(() -> Toast.makeText(EditUserInfoActivity.this, "请选择性别", Toast.LENGTH_SHORT).show());
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedSexId);
        String sexText = selectedRadioButton.getText().toString();
        int sex = sexText.equals("男") ? 0 : 1;

        // 打印日志以调试
        System.out.println("Introduction: " + introduction);
        System.out.println("Age: " + age);
        System.out.println("Sex: " + sex);

        if (introduction.isEmpty() || age.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(EditUserInfoActivity.this, "请填写所有字段", Toast.LENGTH_SHORT).show());
            return;
        }

        String url = "http://150.109.6.243:8787/user/update?introduction=" + introduction + "&age=" + age + "&sex=" + sex;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .put(new FormBody.Builder().build()) // 这里的FormBody是空的，因为我们只发送查询参数
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditUserInfoActivity.this, "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditUserInfoActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // 设置结果为 OK
                        finish(); // 关闭当前活动，返回上一页面
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(EditUserInfoActivity.this, "更新失败，错误码：" + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
