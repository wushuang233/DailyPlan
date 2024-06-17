package com.example.test.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.test.R;
import com.example.test.UserInfo;
import com.example.test.activity.AboutActivity;
import com.example.test.activity.EditUserInfoActivity; // 导入 EditUserInfoActivity
import com.example.test.activity.Login;
import com.google.gson.Gson;

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

public class MineFragment extends Fragment {

    private static final int EDIT_USER_INFO_REQUEST_CODE = 1; // 定义请求码

    private TextView tvUsername;
    private TextView tvSex;
    private TextView tvAge;
    private TextView tvPlanNumber;
    private TextView tvIntroduction;
    private Gson gson;
    private OkHttpClient client;
    private Context context; // 添加这个引用

    public MineFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        // Initialize views
        tvUsername = view.findViewById(R.id.tv_username);
        tvSex = view.findViewById(R.id.tv_gender_value);
        tvAge = view.findViewById(R.id.tv_age_value);
        tvPlanNumber = view.findViewById(R.id.tv_plans_completed_value);
        tvIntroduction = view.findViewById(R.id.tv_nikename);
        Button btnLogout = view.findViewById(R.id.btn_logout);
        View aboutAppLayout = view.findViewById(R.id.about_app_layout);
        View editInfoLayout = view.findViewById(R.id.edit_info_layout); // 获取 edit_info_layout

        // Initialize Gson and OkHttpClient
        gson = new Gson();
        client = new OkHttpClient();

        // Save the context
        context = getActivity();

        // Fetch user info
        fetchUserInfo();

        // Set click listener for the logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Set click listener for the about app layout
        aboutAppLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });

        // Set click listener for the edit info layout
        editInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), EditUserInfoActivity.class), EDIT_USER_INFO_REQUEST_CODE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_USER_INFO_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            // 更新用户信息
            fetchUserInfo();
        }
    }

    private void fetchUserInfo() {
        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        // Check if token is available
        if (token == null) {
            Toast.makeText(context, "未找到 Token", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request to fetch user info
        String url = "http://150.109.6.243:8787/user/info";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(context, "获取用户信息失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    UserInfo userInfo = gson.fromJson(responseData, UserInfo.class);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            if (userInfo.getCode() == 0) {
                                UserInfo.Data data = userInfo.getData();
                                tvUsername.setText(data.getUsername());
                                tvSex.setText(data.getSex() == 1 ? "女" : "男");
                                tvAge.setText(String.valueOf(data.getAge()));
                                String introduction = data.getIntroduction();
                                tvIntroduction.setText(introduction == null || introduction.isEmpty() ? "这个人很懒，什么都没有留下哦~" : introduction);
                                // Fetch plan count
                                fetchPlanCount();
                            } else {
                                Toast.makeText(context, "获取用户信息失败：" + userInfo.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, "获取用户信息失败，错误代码：" + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void fetchPlanCount() {
        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        // Check if token is available
        if (token == null) {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> Toast.makeText(context, "未找到 Token", Toast.LENGTH_SHORT).show());
            }
            return;
        }

        // Create request to fetch plan count
        String url = "http://150.109.6.243:8787/plan/count";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(context, "获取计划数量失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        int code = jsonObject.getInt("code");
                        if (code == 0) {
                            int planCount = jsonObject.getInt("data");
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> tvPlanNumber.setText(String.valueOf(planCount)));
                            }
                        } else {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> Toast.makeText(context, "获取计划数量失败", Toast.LENGTH_SHORT).show());
                            }
                        }
                    } catch (JSONException e) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(context, "响应解析失败", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, "获取计划数量失败，错误代码：" + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void logoutUser() {
        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        // Check if token is available
        if (token == null) {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> Toast.makeText(context, "未找到 Token", Toast.LENGTH_SHORT).show());
            }
            return;
        }

        // Create request to logout
        String url = "http://150.109.6.243:8787/user/logout";
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(context, "退出登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            // Clear token from SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("token");
                            editor.apply();

                            // Navigate to login activity
                            Intent intent = new Intent(requireActivity(), Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                    }
                } else {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, "退出登录失败，错误代码：" + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }
}
