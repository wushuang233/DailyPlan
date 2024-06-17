package com.example.test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.Calendar;

import android.widget.DatePicker;
import android.widget.TextView;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.test.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlanActivity extends AppCompatActivity {

    EditText editTextStartDate2;
    EditText editTextEndDate2;
    EditText titleTextView;
    EditText editTextStartDate;
    EditText editTextEndDate;
    TextView timeTextView;
    Spinner weekdaySpinner;
    EditText editTextPlanContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plan);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPlan(token);
            }
        });

        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the current activity and go back to the previous one
            }
        });

        titleTextView = findViewById(R.id.titleTextView);
        editTextPlanContent = findViewById(R.id.editTextMemo);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        timeTextView = findViewById(R.id.timeTextView);
        weekdaySpinner = findViewById(R.id.weekdaySpinner);
        editTextStartDate2 = findViewById(R.id.editTextStartDate2);
        editTextEndDate2 = findViewById(R.id.editTextEndDate2);

        editTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogStart(PlanActivity.this);
            }
        });

        editTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogEnd(PlanActivity.this);
            }
        });

        // 格式化当前时间并设置给 TextView 控件
        SimpleDateFormat dateFormat = new SimpleDateFormat("'今天' HH:mm", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());
        timeTextView.setText(currentTime);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weekday_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekdaySpinner.setAdapter(adapter);

        String[] weekdays = getResources().getStringArray(R.array.weekday_array);
        ArrayAdapter<String> adaptertwo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weekdays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.weekdaySpinner);
        spinner.setAdapter(adaptertwo);

        EditText editTextStartDate2 = findViewById(R.id.editTextStartDate2);
        EditText editTextEndDate2 = findViewById(R.id.editTextEndDate2);

        editTextStartDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v, false);
            }
        });

        editTextEndDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v, true);
            }
        });

    }

    private void sendPlan(String token) {
        String planName = titleTextView.getText().toString();
        String start = editTextStartDate.getText().toString() + " " + editTextStartDate2.getText().toString();
        String end = editTextEndDate.getText().toString() + " " + editTextEndDate2.getText().toString();
        String clock = weekdaySpinner.getSelectedItem().toString();
        // 获取计划内容
        String planContent = editTextPlanContent.getText().toString();

        Plan plan = new Plan(planName, start, end, clock, planContent);

        Gson gson = new Gson();
        String json = gson.toJson(plan);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url("http://150.109.6.243:8787/plan/add")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(PlanActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanActivity.this, "计划发送成功", Toast.LENGTH_SHORT).show();
                        // 跳转到HomeActivity并清除PlanActivity的历史记录
                        Intent intent = new Intent(PlanActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanActivity.this, "计划发送失败，错误码：" + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // 计划类，用于 Gson 序列化为 JSON
    private static class Plan {
        String planName;
        String start;
        String end;
        String clock;
        String planContent;

        public Plan(String planName, String start, String end, String clock, String planContent) {
            this.planName = planName;
            this.start = start;
            this.end = end;
            this.clock = clock;
            this.planContent = planContent;
        }
    }

    private void showDatePickerDialogStart(Context context) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 将选定的日期显示在 EditText 控件中
                        editTextStartDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void showDatePickerDialogEnd(Context context) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 将选定的日期显示在 EditText 控件中
                        editTextEndDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void showTimePickerDialog(View v, boolean isEndTime) {
        final EditText editText = (EditText) v;
        final Context context = editText.getContext();

        // 创建一个Calendar实例来获取当前时间
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 创建并显示时间选择器对话框
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 当用户设置时间后，更新EditText的文本
                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        editText.setText(time);
                    }
                },
                hour,
                minute,
                android.text.format.DateFormat.is24HourFormat(context) // 自动检测是否使用24小时制
        );
        timePickerDialog.show();
    }

}
