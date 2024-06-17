package com.example.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.PlanItem;
import com.example.test.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private Context context;
    private List<PlanItem> planItems;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public PlanAdapter(Context context, List<PlanItem> planItems) {
        this.context = context;
        this.planItems = planItems;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false);
        return new PlanViewHolder(view, onItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanItem planItem = planItems.get(position);
        holder.planNameTextView.setText(planItem.getPlanName());
        holder.planContentTextView.setText(planItem.getPlanContent());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        String startDate = sdf.format(new Date(planItem.getStart()));
        String endDate = sdf.format(new Date(planItem.getEnd()));

        holder.planStartTimeTextView.setText("开始时间: " + startDate);
        holder.planEndTimeTextView.setText("结束时间: " + endDate);
    }

    @Override
    public int getItemCount() {
        return planItems.size();
    }

    public void setPlanItems(List<PlanItem> newPlanItems) {
        planItems.clear();
        planItems.addAll(newPlanItems);
        notifyDataSetChanged();
    }

    public List<PlanItem> getPlanItems() {
        return planItems;
    }

    public void removePlanItem(int position) {
        planItems.remove(position);
        notifyItemRemoved(position);
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView planNameTextView;
        TextView planContentTextView;
        TextView planStartTimeTextView;
        TextView planEndTimeTextView;

        public PlanViewHolder(@NonNull View itemView, OnItemLongClickListener onItemLongClickListener) {
            super(itemView);
            planNameTextView = itemView.findViewById(R.id.plan_name);
            planContentTextView = itemView.findViewById(R.id.plan_content);
            planStartTimeTextView = itemView.findViewById(R.id.plan_start_time);
            planEndTimeTextView = itemView.findViewById(R.id.plan_end_time);

            itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemLongClickListener.onItemLongClick(position);
                    }
                }
                return true;
            });
        }
    }
}
