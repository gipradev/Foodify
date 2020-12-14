package com.example.foodify.Adaptors;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.foodify.Activities.KitchenHome;
import com.example.foodify.R;
import com.example.foodify.WebServices;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RecyclerKitchenOrder extends RecyclerView.Adapter<RecyclerKitchenOrder.MyViewHolder> {
    private static final String TAG = "RecyclerKitchenOrder";

    private final Context context;
    private final JSONArray array;
    private final KitchenHome kitchenActivity;
    private final OrderAdapterListener listener;
    private final View view;

    private JSONObject jsonObject;
    private SharedPreferences user;
    private String userID, shopID;


    public RecyclerKitchenOrder(Context applicationContext, KitchenHome activity, JSONArray jsonArray,
                                OrderAdapterListener adapterListener, View view) {
        this.context = applicationContext;
        this.array = jsonArray;
        this.listener = adapterListener;
        this.kitchenActivity = activity;
        this.view = view;

        getSharedPreference();
    }

    private void getSharedPreference() {
        user = context.getSharedPreferences("Login", context.MODE_PRIVATE);
        userID = user.getString("user_id", "0");
        shopID = user.getString("shop_id", "0");

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kitchen_order_list, null);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        try {

            jsonObject = array.getJSONObject(position);


            holder.chair.setText("CHAIR No.  : #" + jsonObject.getString("n_chair_id"));
            holder.order.setText("ORDER No.  : #" + jsonObject.getString("orderid"));
            holder.orderDate.setText(jsonObject.getString("date"));


            String orderId = array.getJSONObject(position).getString("orderid");
            setChanges(shopID, orderId, holder);//check item status

            holder.viewDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        JSONArray itemArray = array.getJSONObject(position).getJSONArray("item");
                        listener.onViewDetails(position, shopID,itemArray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });


            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        String order_id = array.getJSONObject(position).getString("orderid");


                        Map<String, String> map = new HashMap<>();
                        map.put("position", String.valueOf(position));
                        map.put("order_id", order_id);
                        map.put("action", "accept");

                       // listener.onAcceptOrder(position, shopID, order_id, "accept");
                        listener.onProcessOrder(map);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

            holder.finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        String order_id = array.getJSONObject(position).getString("orderid");
                        //processOrder(shopID,order_id,"finish", holder);

                        Map<String, String> map = new HashMap<>();
                        map.put("position", String.valueOf(position));
                        map.put("order_id", order_id);
                        map.put("action", "finish");

                        listener.onProcessOrder(map);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, holder.menu);

                    popup.inflate(R.menu.recycler_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu1:
                                    //handle menu1 click

                                    handleCancelClick(array, position);

                                    break;

                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void setChanges(final String shopID, final String orderId, final MyViewHolder holder) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_Cart_Status", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "setChanges" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    if (object.getString("status").equals("1")) {

                        String orderStatus = object.getString("order_status");

                        setViewsChanges(orderStatus, holder);

                    } else {


                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "VolleyError" + volleyError);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();

                map.put("login_id", shopID);
                map.put("order_id", orderId);
                return map;
            }
        };
        requestQueue.add(stringRequest);


    }

    private void setViewsChanges(String orderStatus, MyViewHolder holder) {
        try {

            if (orderStatus.equals("0")) {
                holder.orderStatus.setText("New Arrival");
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.finishButton.setVisibility(View.GONE);

            } else if (orderStatus.equals("1")) {
                holder.acceptButton.setVisibility(View.GONE);
                holder.finishButton.setVisibility(View.VISIBLE);
                holder.orderStatus.setText("Processing...");

            } else if (orderStatus.equals("2")) {
                holder.orderStatus.setText("Completed");
                holder.finishButton.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleCancelClick(JSONArray array, int position) {

        try {
            final String order_id = array.getJSONObject(position).getString("orderid");
            Log.e(TAG, "id   " + order_id);


            Map<String, String> map = new HashMap<>();
            map.put("position", String.valueOf(position));
            map.put("order_id", order_id);
            map.put("action", "remove");


            if (array.getJSONObject(position).getString("order_status").equals("Processing") ||
                    array.getJSONObject(position).getString("order_status").equals("Done")) {

                Snackbar snackbar = Snackbar
                        .make(view, "This Order can't be deleted...", Snackbar.LENGTH_LONG);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                snackbar.show();

            } else {

                listener.onCancelOrder(map);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void removeOrderItem(int position) {
        array.remove(position);
        notifyItemRemoved(position);


    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView item;
        private final Button viewDetailsButton;
        // private final TextView finishedText;
        LinearLayout acceptButton, layoutItem, finishButton;
        TextView chair, order, totalPrice, orderStatus, orderDate, menu;
        Button lessButtn, removeButton, category;
        RecyclerView itemList;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
            chair = itemView.findViewById(R.id.chairId);
            order = itemView.findViewById(R.id.orderId);
            totalPrice = itemView.findViewById(R.id.grandTotal);
            orderStatus = (TextView) itemView.findViewById(R.id.orderStatus);
            orderDate = (TextView) itemView.findViewById(R.id.orderDate);
            acceptButton = (LinearLayout) itemView.findViewById(R.id.acceptButton);
            finishButton = (LinearLayout) itemView.findViewById(R.id.buttonFinish);
            viewDetailsButton = (Button) itemView.findViewById(R.id.viewDetailsButton);
            item = (CardView) itemView.findViewById(R.id.content);
            itemList = (RecyclerView) itemView.findViewById(R.id.itemList);
            menu = (TextView) itemView.findViewById(R.id.textViewOptions);
            //finishedText = (TextView) itemView.findViewById(R.id.messageText);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }



    //***************************************************************************************************************************
    public void updateItem(int position) {
        //Log.e(TAG,position+"...at updation");
        notifyItemChanged(position);
        notifyDataSetChanged();

    }

    public interface OrderAdapterListener {

        void onProcessOrder(Map<String, String> map);

     //   void onAcceptOrder(int index, String shopID, String orderId, String action);

        void onCancelOrder(Map<String, String> map);

        void onViewDetails(int index, String orderId ,JSONArray array);
    }


}


