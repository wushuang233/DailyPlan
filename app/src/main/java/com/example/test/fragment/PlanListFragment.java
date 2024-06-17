package com.example.test.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.PlanAdapter;
import com.example.test.PlanItem;
import com.example.test.R;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlanListFragment extends Fragment {

    private static final String TAG = "PlanListFragment";
    private static final String PREFS_NAME = "MyPrefs";
    private String url;
    private PlanAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isFinishedSection;  // 标识是否是已结束板块

    public PlanListFragment(String url, boolean isFinishedSection) {
        this.url = url;
        this.isFinishedSection = isFinishedSection;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plan_list, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize adapter with empty list
        adapter = new PlanAdapter(getActivity(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Set up long click listener
        adapter.setOnItemLongClickListener(position -> showPopupMenu(position));

        // Fetch plan list from server and update RecyclerView
        fetchPlanList();

        return rootView;
    }

    private void showPopupMenu(int position) {
        View view = recyclerView.findViewHolderForAdapterPosition(position).itemView;
        PopupMenu popupMenu = new PopupMenu(requireContext(), view, Gravity.END);

        Menu menu = popupMenu.getMenu();
        if (!isFinishedSection) {
            MenuItem finishItem = menu.add(Menu.NONE, R.id.action_finish, Menu.NONE, "完成");
            finishItem.setActionView(createMenuItemView(Color.GREEN, "完成"));
        }
        MenuItem deleteItem = menu.add(Menu.NONE, R.id.action_delete, Menu.NONE, "删除");
        deleteItem.setActionView(createMenuItemView(Color.RED, "删除"));

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                deletePlan(adapter.getPlanItems().get(position).getPlanId(), position);
                return true;
            } else if (id == R.id.action_finish) {
                finishPlan(adapter.getPlanItems().get(position).getPlanId(), position);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private View createMenuItemView(int color, String text) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.popup_menu_item, null);
        view.setBackgroundColor(color);
        TextView textView = view.findViewById(R.id.menu_item_text);
        textView.setText(text);
        return view;
    }

    private void deletePlan(int planId, int position) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "未找到 Token", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position); // Restore item
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://150.109.6.243:8787/plan/delete/" + planId)
                .header("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "删除请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position); // Restore item
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        int code = jsonObject.getInt("code");
                        boolean data = jsonObject.getBoolean("data");

                        requireActivity().runOnUiThread(() -> {
                            if (code == 0 && data) {
                                Toast.makeText(requireContext(), "计划删除成功", Toast.LENGTH_SHORT).show();
                                adapter.removePlanItem(position);
                            } else {
                                Toast.makeText(requireContext(), "删除请求失败", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position); // Restore item
                            }
                        });
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "响应解析失败", Toast.LENGTH_SHORT).show();
                            adapter.notifyItemChanged(position); // Restore item
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "删除请求失败，错误代码：" + response.code(), Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position); // Restore item
                    });
                }
            }
        });
    }

    private void finishPlan(int planId, int position) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(requireContext(), "未找到 Token", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(position); // Restore item
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");
        Request request = new Request.Builder()
                .url("http://150.109.6.243:8787/plan/finish/" + planId)
                .header("Authorization", "Bearer " + token)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "完成请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position); // Restore item
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        int code = jsonObject.getInt("code");
                        boolean data = jsonObject.getBoolean("data");

                        requireActivity().runOnUiThread(() -> {
                            if (code == 0 && data) {
                                Toast.makeText(requireContext(), "计划标记为完成", Toast.LENGTH_SHORT).show();
                                adapter.removePlanItem(position);
                            } else {
                                Toast.makeText(requireContext(), "完成请求失败", Toast.LENGTH_SHORT).show();
                                adapter.notifyItemChanged(position); // Restore item
                            }
                        });
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "响应解析失败", Toast.LENGTH_SHORT).show();
                            adapter.notifyItemChanged(position); // Restore item
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "完成请求失败，错误代码：" + response.code(), Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position); // Restore item
                    });
                }
            }
        });
    }

    public void fetchPlanList() {
        if (!isAdded()) {
            return;
        }

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        // Check if token is available
        if (token == null) {
            Toast.makeText(requireContext(), "未找到 Token", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "请求失败：" + e.getMessage());
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        // Show error message or handle failure
                        Toast.makeText(requireContext(), "请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "服务器响应：" + responseData);

                    // Parse JSON response
                    PlanResponse planResponse = parseJson(responseData);

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            if (planResponse != null && planResponse.getCode() == 0) {
                                // Update RecyclerView with plan items
                                adapter.setPlanItems(Arrays.asList(planResponse.getData()));
                                adapter.notifyDataSetChanged();
                            } else {
                                // Show error message or handle failure
                                Toast.makeText(requireContext(), "加载计划失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "请求失败，错误代码：" + response.code());
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            // Show error message or handle failure
                            Toast.makeText(requireContext(), "请求失败，错误代码：" + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private PlanResponse parseJson(String responseData) {
        Gson gson = new Gson();
        return gson.fromJson(responseData, PlanResponse.class);
    }

    private static class PlanResponse {
        private int code;
        private PlanItem[] data;

        public int getCode() {
            return code;
        }

        public PlanItem[] getData() {
            return data;
        }
    }
}
