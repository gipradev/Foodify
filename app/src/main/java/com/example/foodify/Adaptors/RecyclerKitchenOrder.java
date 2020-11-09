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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
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

    private int flag1;
    private int flag2;


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
        View view = LayoutInflater.from(context).inflate(R.layout.kitchen_order_list, null);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        try {

            jsonObject = array.getJSONObject(position);


            holder.chair.setText("CHAIR No.  : #" + jsonObject.getString("n_chair_id"));
            holder.order.setText("ORDER No.  : #" + jsonObject.getString("orderid"));
            holder.orderDate.setText(jsonObject.getString("date"));

            //******************************set Items*******************************************
            JSONArray itemArray = jsonObject.getJSONArray("item");

            RecyclerOrderItems recyclerOrderItems = new RecyclerOrderItems(context, itemArray,
                    array.getJSONObject(position).getString("order_status"), holder);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            holder.itemList.setLayoutManager(linearLayoutManager);
            holder.itemList.setAdapter(recyclerOrderItems);
            //**********************************************************************************

            String orderId = array.getJSONObject(position).getString("orderid");
            setChanges(shopID, orderId, holder);//check item status


            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        String order_id = array.getJSONObject(position).getString("orderid");

                        ProcessDialogClass cdd = new ProcessDialogClass
                                (kitchenActivity, shopID, order_id, "accept", holder, position);
                        cdd.show();

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

                        ProcessDialogClass cdd = new ProcessDialogClass
                                (kitchenActivity, shopID, order_id, "finish", holder, position);
                        cdd.show();
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
                Log.e(TAG, "Response" + response);
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
                //holder.finishedText.setVisibility(View.GONE);
                holder.finishButton.setVisibility(View.GONE);

            } else if (orderStatus.equals("1")) {
                holder.acceptButton.setVisibility(View.GONE);
                holder.finishButton.setVisibility(View.VISIBLE);
                // holder.finishedText.setVisibility(View.GONE);
                holder.orderStatus.setText("Processing...");

            } else if (orderStatus.equals("2")) {
                holder.orderStatus.setText("Completed");
//                holder.finishButton.setVisibility(View.GONE);h
//                holder.acceptButton.setVisibility(View.GONE);
//                holder.finishedText.setVisibility(View.VISIBLE);
//                holder.finishedText.startAnimation((Animation) AnimationUtils.loadAnimation(context, R.anim.linear_animation));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleCancelClick(JSONArray array, int position) {

        try {
            final String order_id = array.getJSONObject(position).getString("orderid");
            Log.e(TAG, "id   " + order_id);


            if (array.getJSONObject(position).getString("order_status").equals("Processing") ||
                    array.getJSONObject(position).getString("order_status").equals("Done")) {

                Snackbar snackbar = Snackbar
                        .make(view, "This Order can't be deleted...", Snackbar.LENGTH_LONG);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                snackbar.show();
            } else {

                DeleteDialogClass cdd = new DeleteDialogClass(kitchenActivity, order_id, position);
                cdd.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void removeOrder(final String shop_id, final String order_id, final int position) {
//        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Cancel_Order_Item", new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {

                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
//                        progressBar.setVisibility(View.INVISIBLE);
//
                        removeOrderItem(position);


                    } else {
                        // progressBar.setVisibility(View.INVISIBLE);
                        Snackbar snackbar = Snackbar
                                .make(view, "ERROR", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        //progressBar.setVisibility(View.GONE);


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

                map.put("login_id", shop_id);
                map.put("order_id", order_id);


                return map;
            }
        };
        requestQueue.add(stringRequest);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void removeOrderItem(int position) {
        array.remove(position);
        notifyItemRemoved(position);

        new Handler().postDelayed(new Runnable() {
            @Override

            public void run() {
                // getOrders(shopID);
            }
        }, 1000);


    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView item;
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


    //***************************************************alert Box********************************************************
    public class  ProcessDialogClass extends Dialog implements
            View.OnClickListener {


        private final MyViewHolder myHolder;
        private final String shopID;
        private final String orderId;
        private final String action;
        private final int index;
        public Activity activity;
        public Dialog d;
        public Button yes, no;
        private TextView message;

        public ProcessDialogClass(Activity activity, String shopID, String order_id, String action, MyViewHolder holder, int position) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.activity = activity;
            this.myHolder = holder;
            this.shopID = shopID;
            this.orderId = order_id;
            this.action = action;
            this.index = position;


        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custome_alert_dialogue);
            message = (TextView) findViewById(R.id.msgText);
            yes = (Button) findViewById(R.id.btn_yes);
            no = (Button) findViewById(R.id.btn_no);

            message.setText("Conform to " + action + " order..!");
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:

                    // processOrder(shopID, orderId, action, myHolder);

                    listener.onAcceptOrder(index, shopID, orderId, action);

                    break;
                case R.id.btn_no:
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }

    }
    //***********************************************************************************************************


    //*************************************************DeleteDialogClass **********************************************************
    public class DeleteDialogClass extends Dialog implements
            View.OnClickListener {

        private final String orderID;
        private final int index;
        public Context activity;
        public Dialog d;
        public Button yes, no;
        private TextView message;

        public DeleteDialogClass(Activity activity, String value, int position) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.activity = activity;
            this.orderID = value;
            this.index = position;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.alert_box_remove_order);
            message = (TextView) findViewById(R.id.msgText);
            yes = (Button) findViewById(R.id.btn_yes);
            no = (Button) findViewById(R.id.btn_no);

            message.setText("Do you want to Delete.?");
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:

                    listener.onCancelOrder(index, orderID);

                    break;
                case R.id.btn_no:
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }

    //***************************************************************************************************************************
    public class RecyclerOrderItems extends RecyclerView.Adapter<RecyclerOrderItems.MyViewHolder> {
        private final Context context;
        private final JSONArray array;
        private final RecyclerKitchenOrder.MyViewHolder orderHolder;
        private JSONObject jsonObject;


        public RecyclerOrderItems(Context applicationContext, JSONArray jsonArray, String order_status,
                                  RecyclerKitchenOrder.MyViewHolder holder) {
            this.context = applicationContext;
            this.array = jsonArray;
            this.orderHolder = holder;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.order_items, null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            try {

                jsonObject = array.getJSONObject(position);

                char first = 0;
                String myString = jsonObject.getString("item_name");
                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);

                if (!jsonObject.getString("subid").equals("0")) {
                    first = jsonObject.getString("submenu_name").charAt(0);
                    holder.name.setText(upperString + "   (" + first + ")");
                } else {
                    holder.name.setText(upperString);
                }

                holder.itemCount.setText(jsonObject.getString("quantity") + " pcs");


                if (!jsonObject.getString("item_status").equals("PENDING")) {
                    holder.removeButton.setVisibility(View.INVISIBLE);
                }


                holder.removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String order_id = array.getJSONObject(position).getString("order_id");
                            String product_id = array.getJSONObject(position).getString("productid");


                            DeleteItemDialogClass cdd = new DeleteItemDialogClass
                                    (context, order_id, product_id, shopID, position);
                            cdd.show();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void removeProductItem(int position) {


            array.remove(position);
            notifyItemRemoved(position);
            //getCartData(userID,chairId);


        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutItem;
            TextView name, size, price, itemCount, itemTotal;
            Button removeButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
                name = itemView.findViewById(R.id.itemName);
                price = itemView.findViewById(R.id.itemPrice);
                itemCount = (TextView) itemView.findViewById(R.id.quantity);
                removeButton = (Button) itemView.findViewById(R.id.removeButton);

            }
        }


        public class DeleteItemDialogClass extends Dialog implements
                View.OnClickListener {

            private final String orderID;
            private final int index;
            private final String shopId;
            private final String productId;
            public Context activity;
            public Dialog d;
            public Button yes, no;
            private TextView message;

            public DeleteItemDialogClass(Context activity, String value, String product_id, String shopID, int position) {
                super(activity);
                // TODO Auto-generated constructor stub
                this.activity = activity;
                this.orderID = value;
                this.shopId = shopID;
                this.productId = product_id;
                this.index = position;
            }

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.custome_alert_dialogue);
                message = (TextView) findViewById(R.id.msgText);
                yes = (Button) findViewById(R.id.btn_yes);
                no = (Button) findViewById(R.id.btn_no);

                message.setText("Do you want Delete.?");
                yes.setOnClickListener(this);
                no.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_yes:

                        removeItem(orderID, productId, shopID, index);

                        break;
                    case R.id.btn_no:
                        dismiss();
                        break;
                    default:
                        break;
                }
                dismiss();
            }
        }

        private void removeItem(final String order_id, final String product_id, final String shopID, final int position) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    WebServices.BaseUrl + "Cancel_Order_Item", new Response.Listener<String>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onResponse(String response) {
                    Log.e(TAG, "Response" + response);
                    try {

                        JSONObject object = new JSONObject(response.trim());
                        String status = object.getString("status");
                        if (status.equals("1")) {

                            removeProductItem(position);

                        } else {

                            Snackbar snackbar = Snackbar
                                    .make(view, "ERROR", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            //progressBar.setVisibility(View.GONE);


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
                    Log.e(TAG, userID + "");
                    map.put("login_id", shopID);
                    map.put("order_id", order_id);
                    map.put("product_id", product_id);

                    return map;
                }
            };
            requestQueue.add(stringRequest);


        }


    }

    //***************************************************************************************************************************
    public void updateItem(int position) {
        //Log.e(TAG,position+"...at updation");
        notifyItemChanged(position);
        notifyDataSetChanged();

    }

    public interface OrderAdapterListener {

        void onAcceptOrder(int index, String shopID, String orderId, String action);

        void onCancelOrder(int index, String orderID);
    }


}


