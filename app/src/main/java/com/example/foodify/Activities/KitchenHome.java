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
import com.example.foodify.BottomFragment.OrderItemBottomSheet;
import com.example.foodify.Utils.DialogClass;
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


public class KitchenHome extends AppCompatActivity
        implements RecyclerKitchenOrder.OrderAdapterListener, DialogClass.DialogueListener {
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
    public void onProcessOrder(Map<String, String> map) {
        DialogClass dialogClass = new DialogClass(this, this, map);
        dialogClass.show();
    }

//    @Override
//    public void onAcceptOrder(int index, String shopID, String orderId, String action) {
//
////        DialogClass dialogClass = new DialogClass(this,this, map);
////        dialogClass.show();
//
//        //processOrder(shopID, orderId, action, index);
//
//    }


    @Override
    public void onCancelOrder(Map<String, String> map) {

        DialogClass dialogClass = new DialogClass(this, this, map);
        dialogClass.show();


    }

    @Override
    public void onViewDetails(int index, String orderId, JSONArray array) {

        showOrderDetails(index, orderId, array);

    }

    private void showOrderDetails(int index, String orderId, JSONArray itemArray) {
        OrderItemBottomSheet itemBottomSheet = new OrderItemBottomSheet(index, orderId, itemArray);
        itemBottomSheet.show(getSupportFragmentManager(), itemBottomSheet.getTag());
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
                        showSnackBar("Kitchen Cart is Empty..");



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

                        showSnackBar(action.toUpperCase()+ "ED");


                    } else {

                        showSnackBar( "ERROR");


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

                        showSnackBar("That Order is no longer visible....!!");

                    } else {
                        // progressBar.setVisibility(View.INVISIBLE);

                        showSnackBar("Please try again later");



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

                        showSnackBar("Error");

//                        Snackbar snackbar = Snackbar
//                                .make(parentView, "ERROR", Snackbar.LENGTH_LONG);
//                        snackbar.show();
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


    //****************************************************************************************************
    //dialogue on Clicks
    @Override
    public void onProcessOrder(String order_id, String action, String position) {

        processOrder(shopID, order_id, action, Integer.parseInt(position));

    }

    @Override
    public void onRemoveOrder(String order_id, String action, String position) {

        removeOrder(shopID, order_id, Integer.parseInt(position));

    }


    //************************************************************************************************************************


    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(parentView, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

}

