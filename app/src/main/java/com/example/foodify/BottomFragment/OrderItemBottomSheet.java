package com.example.foodify.BottomFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodify.Adaptors.RecyclerOrderItems;
import com.example.foodify.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;

public class OrderItemBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "OrderItemBottomSheet";
    private final int orderPosition;
    private final JSONArray jsonArray;
    private final String orderId;
    private RecyclerView orderItemListRecycler;

    public OrderItemBottomSheet(int index, String orderId, JSONArray itemArray) {
        this.orderPosition = index;
        this.orderId = orderId;
        this.jsonArray = itemArray;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.kitchen_order_item_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e(TAG, jsonArray.toString());
        orderItemListRecycler = (RecyclerView) view.findViewById(R.id.orderItemListRecycler);
        generateRecyclerView(jsonArray, view);

    }

    private void generateRecyclerView(JSONArray jsonArray, View view) {
        RecyclerOrderItems recyclerOrderItems = new RecyclerOrderItems(jsonArray);
        orderItemListRecycler.setAdapter(recyclerOrderItems);
    }
}
