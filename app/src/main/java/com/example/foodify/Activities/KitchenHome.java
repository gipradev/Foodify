package com.example.foodify.Activities;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Adaptors.RecyclerKitchenOrder;
import com.example.foodify.WebServices;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.foodify.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class KitchenHome extends AppCompatActivity implements RecyclerKitchenOrder.OrderAdapterListener {
    private static final String TAG = "KitchenHome";

    private Window window;
    private SharedPreferences user;
    private String userID, shopID;
    private View parentView;
    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManagerOffer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    int flag1 = 0, flag2 = 0;
    private RelativeLayout emptyCart;
    private RecyclerView orderList;
    private RecyclerKitchenOrder recyclerOrder;
    private SharedPreferences fcm;
    private String token_id;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.WHITE);
        toolbar.setTitle("KITCHEN CART");
        toolbar.setTitleTextColor(Color.BLACK);
        //toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);

        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        parentView = findViewById(android.R.id.content);
        progressBar = (ProgressBar) findViewById(R.id.progressDialogue);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorBlack));
        emptyCart = (RelativeLayout) findViewById(R.id.emptyCart);

        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id", "0");
        shopID = user.getString("shop_id", "0");

        fcm = getApplicationContext().getSharedPreferences("FCM", getApplicationContext().MODE_PRIVATE);
        token_id = fcm.getString("kitchenToken", "0");
        Log.e(TAG, "token      " + token_id + "     ");


        orderList = (RecyclerView) findViewById(R.id.orderList);
        getOrders(shopID);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOrders(shopID);

            }
        });


    }


    @Override
    public void onAcceptOrder(int index, String shopID, String orderId, String action) {

        processOrder(shopID, orderId, action, index);

    }


    @Override
    public void onCancelOrder(int index, String orderID) {

        removeOrder(shopID, orderID, index);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logoutFunction(userID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void getOrders(final String shopID) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Kitchen_Cart_Order_List", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {

                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.INVISIBLE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        emptyCart.setVisibility(View.GONE);

                        JSONArray jsonArray = object.getJSONArray("data");
                        generateData(jsonArray);


                    } else {

                        emptyCart.setVisibility(View.VISIBLE);

                        progressBar.setVisibility(View.INVISIBLE);
                        Snackbar snackbar = Snackbar
                                .make(parentView, "Kitchen Cart is Empty..", Snackbar.LENGTH_LONG);
                        snackbar.show();


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
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void generateData(JSONArray array) {

        recyclerOrder = new RecyclerKitchenOrder(getApplicationContext(), KitchenHome.this, array, this, parentView);
        linearLayoutManagerOffer = new LinearLayoutManager(getApplicationContext());
        linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
        orderList.setHasFixedSize(true);
        orderList.setLayoutManager(linearLayoutManagerOffer);
        orderList.setAdapter(recyclerOrder);


    }


//    public class RecyclerOrder extends RecyclerView.Adapter<RecyclerOrder.MyViewHolder> {
//        private final Context context;
//        private final JSONArray array;
//
//        private JSONObject jsonObject;
//
//
//        public RecyclerOrder(Context applicationContext, JSONArray jsonArray) {
//            this.context = applicationContext;
//            this.array = jsonArray;
//        }
//
//        @NonNull
//        @Override
//        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(context).inflate(R.layout.kitchen_order_list, null);
//            return new MyViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
//            try {
//                jsonObject = array.getJSONObject(position);
//                holder.chair.setText("CHAIR No.  : #" + jsonObject.getString("n_chair_id"));
//                holder.order.setText("ORDER No.  : #" + jsonObject.getString("orderid"));
//
//                holder.orderDate.setText(jsonObject.getString("date"));
//
//
//                if (jsonObject.getString("order_status").equals("PENDING")) {
//                    holder.orderStatus.setText("New Arrival");
//                    holder.finishButton.setEnabled(false);
//                    holder.acceptButton.setBackgroundResource(R.drawable.curve_border);
//                    holder.finishButton.setBackgroundResource(R.drawable.disabled_button);
//
//                } else if (jsonObject.getString("order_status").equals("Processing")) {
//                    holder.acceptButton.setEnabled(false);
//                    holder.acceptButton.setBackgroundResource(R.drawable.disabled_button);
//                    holder.finishButton.setBackgroundResource(R.drawable.black_border);
//                    holder.orderStatus.setText(jsonObject.getString("order_status"));
//                } else if (jsonObject.getString("order_status").equals("Done")) {
//                    holder.orderStatus.setText(jsonObject.getString("order_status"));
//                    holder.acceptButton.setEnabled(false);
//                    holder.finishButton.setEnabled(false);
//                    holder.finishButton.setBackgroundResource(R.drawable.disabled_button);
//                    holder.acceptButton.setBackgroundResource(R.drawable.disabled_button);
//                }
//                JSONArray itemArray = jsonObject.getJSONArray("item");
//
//
//                RecyclerOrderItems recyclerOrderItems = new RecyclerOrderItems(getApplicationContext(), itemArray,
//                        array.getJSONObject(position).getString("order_status"), holder);
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//
////                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(holder.itemList.getContext(),
////                        linearLayoutManager.getOrientation());
////                holder.itemList.addItemDecoration(dividerItemDecoration);
//                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                holder.itemList.setLayoutManager(linearLayoutManager);
//                holder.itemList.setAdapter(recyclerOrderItems);
//
//                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            String order_id = array.getJSONObject(position).getString("orderid");
//
//
//                            ProcessDialogClass cdd = new ProcessDialogClass
//                                    (KitchenHome.this, shopID, order_id, "accept", holder);
//                            cdd.show();
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                });
//
//                holder.finishButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        try {
//                            String order_id = array.getJSONObject(position).getString("orderid");
//                            //processOrder(shopID,order_id,"finish", holder);
//
//                            ProcessDialogClass cdd = new ProcessDialogClass
//                                    (KitchenHome.this, shopID, order_id, "finish", holder);
//                            cdd.show();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//                holder.item.setOnLongClickListener(new View.OnLongClickListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//                    @Override
//                    public boolean onLongClick(View v) {
//
////                        try {
////                            final String order_id =array.getJSONObject(position).getString("orderid");
////                            Log.e(TAG,"id   "+order_id);
////
////
////                            if (array.getJSONObject(position).getString("order_status").equals("Processing")||
////                                    array.getJSONObject(position).getString("order_status").equals("Done")){
////
////                                Snackbar snackbar = Snackbar
////                                        .make(parentView, "This Order can't be deleted...", Snackbar.LENGTH_LONG);
////                                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
////                                snackbar.show();
////                            }else {
////
////                                DeleteDialogClass cdd = new DeleteDialogClass(KitchenHome.this, order_id, position);
////                                cdd.show();
////                            }
////                        } catch (JSONException e) {
////                            e.printStackTrace();
////                        }
//
//                        return false;
//                    }
//                });
//
//
//                holder.menu.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        PopupMenu popup = new PopupMenu(context, holder.menu);
//
//                        popup.inflate(R.menu.recycler_menu);
//                        //adding click listener
//                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                            @Override
//                            public boolean onMenuItemClick(MenuItem item) {
//                                switch (item.getItemId()) {
//                                    case R.id.menu1:
//                                        //handle menu1 click
//
//                                        handleCancelClick(array, position);
//
//                                        break;
//
//                                }
//                                return false;
//                            }
//                        });
//                        //displaying the popup
//                        popup.show();
//                    }
//                });
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//
//        private void handleCancelClick(JSONArray array, int position) {
//
//            try {
//                final String order_id = array.getJSONObject(position).getString("orderid");
//                Log.e(TAG, "id   " + order_id);
//
//
//                if (array.getJSONObject(position).getString("order_status").equals("Processing") ||
//                        array.getJSONObject(position).getString("order_status").equals("Done")) {
//
//                    Snackbar snackbar = Snackbar
//                            .make(parentView, "This Order can't be deleted...", Snackbar.LENGTH_LONG);
//                    snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
//                    snackbar.show();
//                } else {
//
//                    DeleteDialogClass cdd = new DeleteDialogClass(KitchenHome.this, order_id, position);
//                    cdd.show();
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//        private void removeOrder(final String shop_id, final String order_id, final int position) {
//            progressBar.setVisibility(View.VISIBLE);
//            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//            StringRequest stringRequest = new StringRequest(Request.Method.POST,
//                    WebServices.BaseUrl + "Cancel_Order_Item", new Response.Listener<String>() {
//                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//                @Override
//                public void onResponse(String response) {
//                    Log.e(TAG, "Response" + response);
//                    try {
//
//                        JSONObject object = new JSONObject(response.trim());
//                        String status = object.getString("status");
//                        if (status.equals("1")) {
//                            progressBar.setVisibility(View.INVISIBLE);
//
//                            removeOrderItem(position);
//
//
//                        } else {
//                            progressBar.setVisibility(View.INVISIBLE);
//                            Snackbar snackbar = Snackbar
//                                    .make(parentView, "ERROR", Snackbar.LENGTH_LONG);
//                            snackbar.show();
//                            //progressBar.setVisibility(View.GONE);
//
//
//                        }
//
//                    } catch (JSONException e) {
//                        Log.e(TAG, "Exception" + e);
//                    }
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    Log.e(TAG, "VolleyError" + volleyError);
//                }
//            }) {
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> map = new HashMap<>();
//
//                    map.put("login_id", shop_id);
//                    map.put("order_id", order_id);
//
//
//                    return map;
//                }
//            };
//            requestQueue.add(stringRequest);
//
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//        private void removeOrderItem(int position) {
//            array.remove(position);
//            notifyItemRemoved(position);
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//
//                public void run() {
//                    getOrders(shopID);
//                }
//            }, 1000);
//
//
//        }
//
//        private void processOrder(final String shopID, final String orderID, final String action, final MyViewHolder holder) {
//            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//            StringRequest stringRequest = new StringRequest(Request.Method.POST,
//                    WebServices.BaseUrl + "Process_Order", new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.e(TAG, "Response" + response);
//                    try {
//
//                        JSONObject object = new JSONObject(response.trim());
//                        String status = object.getString("status");
//                        if (status.equals("1")) {
//
//
//                            if (action.equals("accept")) {
//
//                                Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
//                                        R.anim.fade_out);
//                                final Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
//                                        R.anim.fade_in);
//                                holder.orderStatus.startAnimation(animFadeOut);
//                                final Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        holder.orderStatus.startAnimation(animFadeIn);
//                                        // Do something after 1s = 1000ms
//                                        flag1 = 1;
//                                        holder.orderStatus.setText("Processing");
//                                        //holder.orderStatus.setVisibility(View.VISIBLE);
//
//                                        holder.acceptButton.setEnabled(false);
//                                        holder.acceptButton.setBackgroundResource(R.drawable.disabled_button);
//                                        holder.finishButton.setBackgroundResource(R.drawable.black_border);
//                                        holder.finishButton.setEnabled(true);
//
//
//                                    }
//                                }, 200);
//
//                                final Handler handler1 = new Handler();
//                                handler1.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//
//
//                                        getOrders(shopID);
//                                    }
//                                }, 100);
//
//                                Snackbar snackbar = Snackbar
//                                        .make(parentView, "ACCEPTED", Snackbar.LENGTH_LONG);
//                                snackbar.show();
//
//                            } else {
//
//                                Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
//                                        R.anim.fade_out);
//                                final Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
//                                        R.anim.fade_in);
//                                holder.orderStatus.startAnimation(animFadeOut);
//                                final Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        holder.orderStatus.startAnimation(animFadeIn);
//                                        // Do something after 1s = 1000ms
//                                        flag2 = 1;
//                                        holder.orderStatus.setText("Done");
//                                        //holder.orderStatus.setVisibility(View.VISIBLE);
//
//                                        // holder.acceptButton.setEnabled(false);
//                                        holder.finishButton.setEnabled(false);
//                                        holder.finishButton.setBackgroundResource(R.drawable.disabled_button);
//
//
//                                    }
//                                }, 200);
//
//                                final Handler handler1 = new Handler();
//                                handler1.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//
//
//                                        getOrders(shopID);
//                                    }
//                                }, 100);
//
//                                Snackbar snackbar = Snackbar
//                                        .make(parentView, "Finished", Snackbar.LENGTH_LONG);
//                                snackbar.show();
//
//                            }
//
//
//                        } else {
//
//                            Snackbar snackbar = Snackbar
//                                    .make(parentView, "ERROR", Snackbar.LENGTH_LONG);
//                            snackbar.show();
//                            //progressBar.setVisibility(View.GONE);
//
//
//                        }
//
//                    } catch (JSONException e) {
//                        Log.e(TAG, "Exception" + e);
//                    }
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    Log.e(TAG, "VolleyError" + volleyError);
//                }
//            }) {
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> map = new HashMap<>();
//                    map.put("action", action);
//                    map.put("shop_id", shopID);
//                    map.put("order_id", orderID);
//                    return map;
//                }
//            };
//            requestQueue.add(stringRequest);
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return array.length();
//        }
//
//        public class MyViewHolder extends RecyclerView.ViewHolder {
//            private final CardView item;
//            LinearLayout acceptButton, layoutItem, finishButton;
//            TextView chair, order, totalPrice, orderStatus, orderDate, menu;
//            Button lessButtn, removeButton, category;
//            RecyclerView itemList;
//
//            public MyViewHolder(@NonNull View itemView) {
//                super(itemView);
//                layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
//                chair = itemView.findViewById(R.id.chairId);
//                order = itemView.findViewById(R.id.orderId);
//                totalPrice = itemView.findViewById(R.id.grandTotal);
//                orderStatus = (TextView) itemView.findViewById(R.id.orderStatus);
//                orderDate = (TextView) itemView.findViewById(R.id.orderDate);
//                acceptButton = (LinearLayout) itemView.findViewById(R.id.acceptButton);
//                finishButton = (LinearLayout) itemView.findViewById(R.id.buttonFinish);
//                item = (CardView) itemView.findViewById(R.id.content);
//                itemList = (RecyclerView) itemView.findViewById(R.id.itemList);
//                menu = (TextView) itemView.findViewById(R.id.textViewOptions);
//            }
//        }
//
//
//        //***************************************************alert Box********************************************************
//        public class ProcessDialogClass extends Dialog implements
//                View.OnClickListener {
//
//
//            private final MyViewHolder myHolder;
//            private final String shopID;
//            private final String orderId;
//            private final String action;
//            public Activity activity;
//            public Dialog d;
//            public Button yes, no;
//            private TextView message;
//
//            public ProcessDialogClass(Activity activity, String shopID, String order_id, String action, MyViewHolder holder) {
//                super(activity);
//                // TODO Auto-generated constructor stub
//                this.activity = activity;
//                this.myHolder = holder;
//                this.shopID = shopID;
//                this.orderId = order_id;
//                this.action = action;
//
//
//            }
//
//            @Override
//            protected void onCreate(Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                requestWindowFeature(Window.FEATURE_NO_TITLE);
//                setContentView(R.layout.custome_alert_dialogue);
//                message = (TextView) findViewById(R.id.msgText);
//                yes = (Button) findViewById(R.id.btn_yes);
//                no = (Button) findViewById(R.id.btn_no);
//
//                message.setText("Conform to " + action + " order..!");
//                yes.setOnClickListener(this);
//                no.setOnClickListener(this);
//
//            }
//
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.btn_yes:
//
//                        processOrder(shopID, orderId, action, myHolder);
//
//                        break;
//                    case R.id.btn_no:
//                        dismiss();
//                        break;
//                    default:
//                        break;
//                }
//                dismiss();
//            }
//
//        }
////***********************************************************************************************************
//
//
//        public class DeleteDialogClass extends Dialog implements
//                View.OnClickListener {
//
//            private final String orderID;
//            private final int index;
//            public Activity activity;
//            public Dialog d;
//            public Button yes, no;
//            private TextView message;
//
//            public DeleteDialogClass(Activity activity, String value, int position) {
//                super(activity);
//                // TODO Auto-generated constructor stub
//                this.activity = activity;
//                this.orderID = value;
//                this.index = position;
//            }
//
//            @Override
//            protected void onCreate(Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                requestWindowFeature(Window.FEATURE_NO_TITLE);
//                setContentView(R.layout.alert_box_remove_order);
//                message = (TextView) findViewById(R.id.msgText);
//                yes = (Button) findViewById(R.id.btn_yes);
//                no = (Button) findViewById(R.id.btn_no);
//
//                message.setText("Do you want to Delete.?");
//                yes.setOnClickListener(this);
//                no.setOnClickListener(this);
//
//            }
//
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.btn_yes:
//
//                        removeOrder(shopID, orderID, index);
//
//                        break;
//                    case R.id.btn_no:
//                        dismiss();
//                        break;
//                    default:
//                        break;
//                }
//                dismiss();
//            }
//        }
//
//
//    }

//    public class RecyclerOrderItems extends RecyclerView.Adapter<RecyclerOrderItems.MyViewHolder> {
//        private final Context context;
//        private final JSONArray array;
//        private final RecyclerOrder.MyViewHolder orderHolder;
//        private JSONObject jsonObject;
//
//
//        public RecyclerOrderItems(Context applicationContext, JSONArray jsonArray, String order_status,
//                                  RecyclerOrder.MyViewHolder holder) {
//            this.context = applicationContext;
//            this.array = jsonArray;
//            this.orderHolder = holder;
//        }
//
//        @NonNull
//        @Override
//        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(context).inflate(R.layout.order_items, null);
//            return new MyViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
//            try {
//
//
//                jsonObject = array.getJSONObject(position);
//
//                if (!orderHolder.acceptButton.isEnabled()) {
//                    holder.removeButton.setVisibility(View.INVISIBLE);
//                }
//                char first = 0;
//                String myString = jsonObject.getString("item_name");
//                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
//
//
//                if (!jsonObject.getString("subid").equals("0")) {
//                    first = jsonObject.getString("submenu_name").charAt(0);
//                    holder.name.setText(upperString + "   (" + first + ")");
////                    holder.size.setText("("+first+")");
//                } else {
//                    holder.name.setText(upperString);
//                }
//
//                holder.itemCount.setText(jsonObject.getString("quantity") + " pcs");
//
//                holder.removeButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            String order_id = array.getJSONObject(position).getString("order_id");
//                            String product_id = array.getJSONObject(position).getString("productid");
//
//
//                            DeleteItemDialogClass cdd = new DeleteItemDialogClass
//                                    (KitchenHome.this, order_id, product_id, shopID, position);
//                            cdd.show();
//
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//
//        private void removeItem(final String order_id, final String product_id, final String shopID, final int position) {
//            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//            StringRequest stringRequest = new StringRequest(Request.Method.POST,
//                    WebServices.BaseUrl + "Cancel_Order_Item", new Response.Listener<String>() {
//                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//                @Override
//                public void onResponse(String response) {
//                    Log.e(TAG, "Response" + response);
//                    try {
//
//                        JSONObject object = new JSONObject(response.trim());
//                        String status = object.getString("status");
//                        if (status.equals("1")) {
//
//                            removeProductItem(position);
//
//                        } else {
//
//                            Snackbar snackbar = Snackbar
//                                    .make(parentView, "ERROR", Snackbar.LENGTH_LONG);
//                            snackbar.show();
//                            //progressBar.setVisibility(View.GONE);
//
//
//                        }
//
//                    } catch (JSONException e) {
//                        Log.e(TAG, "Exception" + e);
//                    }
//
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    Log.e(TAG, "VolleyError" + volleyError);
//                }
//            }) {
//                @Override
//                protected Map<String, String> getParams() throws AuthFailureError {
//                    Map<String, String> map = new HashMap<>();
//                    Log.e(TAG, userID + "");
//                    map.put("login_id", shopID);
//                    map.put("order_id", order_id);
//                    map.put("product_id", product_id);
//
//                    return map;
//                }
//            };
//            requestQueue.add(stringRequest);
//
//
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//        public void removeProductItem(int position) {
//
//
//            array.remove(position);
//            notifyItemRemoved(position);
//            //getCartData(userID,chairId);
//
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return array.length();
//        }
//
//        public class MyViewHolder extends RecyclerView.ViewHolder {
//            LinearLayout layoutItem;
//            TextView name, size, price, itemCount, itemTotal;
//            Button removeButton;
//
//            public MyViewHolder(@NonNull View itemView) {
//                super(itemView);
//                layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
//                name = itemView.findViewById(R.id.itemName);
//                price = itemView.findViewById(R.id.itemPrice);
//                itemCount = (TextView) itemView.findViewById(R.id.quantity);
//                removeButton = (Button) itemView.findViewById(R.id.removeButton);
//
//            }
//        }
//
//
//        public class DeleteItemDialogClass extends Dialog implements
//                View.OnClickListener {
//
//            private final String orderID;
//            private final int index;
//            private final String shopId;
//            private final String productId;
//            public Activity activity;
//            public Dialog d;
//            public Button yes, no;
//            private TextView message;
//
//            public DeleteItemDialogClass(Activity activity, String value, String product_id, String shopID, int position) {
//                super(activity);
//                // TODO Auto-generated constructor stub
//                this.activity = activity;
//                this.orderID = value;
//                this.shopId = shopID;
//                this.productId = product_id;
//                this.index = position;
//            }
//
//            @Override
//            protected void onCreate(Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                requestWindowFeature(Window.FEATURE_NO_TITLE);
//                setContentView(R.layout.custome_alert_dialogue);
//                message = (TextView) findViewById(R.id.msgText);
//                yes = (Button) findViewById(R.id.btn_yes);
//                no = (Button) findViewById(R.id.btn_no);
//
//                message.setText("Do you want Delete.?");
//                yes.setOnClickListener(this);
//                no.setOnClickListener(this);
//
//            }
//
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.btn_yes:
//
//                        removeItem(orderID, productId, shopID, index);
//
//                        break;
//                    case R.id.btn_no:
//                        dismiss();
//                        break;
//                    default:
//                        break;
//                }
//                dismiss();
//            }
//        }
//
//    }

    //***********************************************Process order (accept or finish)*************************************************************************
    private void processOrder(final String shopID, final String orderID, final String action, final int index) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Process_Order", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {

                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {

                        recyclerOrder.updateItem(index);


                        Snackbar snackbar = Snackbar
                                .make(parentView, action.toUpperCase() + "ED", Snackbar.LENGTH_LONG);
                        snackbar.show();

                    } else {

                        Snackbar snackbar = Snackbar
                                .make(parentView, "ERROR", Snackbar.LENGTH_LONG);
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
                map.put("action", action);
                map.put("shop_id", shopID);
                map.put("order_id", orderID);
                return map;
            }
        };
        requestQueue.add(stringRequest);

    }

    //************************************************************************************************************************

    //*************************************************Cancel Order***********************************************************************

    private void removeOrder(final String shop_id, final String order_id, final int position) {
//        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
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
                        recyclerOrder.removeOrderItem(position);

                        Snackbar snackbar = Snackbar
                                .make(parentView, "That Order is no longer visible....!!", Snackbar.LENGTH_LONG);
                        snackbar.show();


                    } else {
                        // progressBar.setVisibility(View.INVISIBLE);
                        Snackbar snackbar = Snackbar
                                .make(parentView, "Please try again later", Snackbar.LENGTH_LONG);
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

    //************************************************Logout function************************************************************************
    private void logoutFunction(final String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Logout", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {

                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        //progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();

                    } else {

                        Snackbar snackbar = Snackbar
                                .make(parentView, "ERROR", Snackbar.LENGTH_LONG);
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
                map.put("login_id", userID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }
    //************************************************************************************************************************
}
