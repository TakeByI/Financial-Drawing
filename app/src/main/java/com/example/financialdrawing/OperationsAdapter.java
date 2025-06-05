package com.example.financialdrawing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialdrawing.DataSamples.Operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OperationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_header_date, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_operation, parent, false);
            return new OperationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            ((DateHeaderViewHolder)holder).bind((String)items.get(position));
        } else {
            ((OperationViewHolder)holder).bind((Operation)items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOperations(List<Operation> operations) {
        items.clear();

        // Группируем операции по датам
        Map<String, List<Operation>> groupedOperations = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM", Locale.getDefault());

        for (Operation op : operations) {
            String dateKey = sdf.format(op.getCreatedAt().toDate());
            if (!groupedOperations.containsKey(dateKey)) {
                groupedOperations.put(dateKey, new ArrayList<>());
            }
            groupedOperations.get(dateKey).add(op);
        }

        // Добавляем заголовки и операции в список
        for (Map.Entry<String, List<Operation>> entry : groupedOperations.entrySet()) {
            items.add(entry.getKey()); // Добавляем дату как заголовок
            items.addAll(entry.getValue()); // Добавляем все операции за эту дату
        }

        notifyDataSetChanged();
    }

    // ViewHolder для заголовка даты
    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }

        public void bind(String date) {
            tvDateHeader.setText(date);
        }
    }

    // ViewHolder для операции
    static class OperationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOperationType;
        TextView tvCategory, tvAmount;

        public OperationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOperationType = itemView.findViewById(R.id.ivOperationType);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(Operation operation) {
            // Устанавливаем иконку (доход/расход)
            int iconRes = operation.isIncome()
                    ? R.drawable.ic_arrow_up_green  // Ваша зелёная стрелка вверх
                    : R.drawable.ic_arrow_down_red; // Ваша красная стрелка вниз

            ivOperationType.setImageResource(iconRes);

            // Категория
            tvCategory.setText(operation.getCategory());

            // Сумма (форматируем с рублём)
            String amountText = operation.getAmount() + " ₽";
            tvAmount.setText(amountText);

            // Цвет суммы (зелёный для доходов, красный для расходов)
            int colorRes = operation.isIncome()
                    ? android.R.color.holo_green_dark
                    : android.R.color.holo_red_dark;

            tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }
    }

}