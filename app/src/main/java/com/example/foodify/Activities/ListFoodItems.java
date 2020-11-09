package com.example.foodify.Activities;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Adaptors.RecyclerCategoryItems;
import com.example.foodify.BottomFragment.CategorySubMenu;
import com.example.foodify.BottomFragment.CategorySubMenuChange;
import com.example.foodify.CartViewFragments.ListFoodCartSheet;
import com.example.foodify.Fragments.CartSheetFragment;
import com.example.foodify.R;
import com.example.foodify.Utils.InternetHandler;
import com.example.foodify.WebServices;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListFoodItems extends AppCompatActivity implements RecyclerCategoryItems.ProductsAdapterListener {
    private static final String TAG = "ListFoodItems";


    private SharedPreferences user, category, chair;
    private String userID, catID, chairID;
    private RecyclerView categoryProducts;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private TextView toolbarTitle;
    private Window window;
    private String product_id;
    static View parent_view;
    private ProgressBar progressBar;
    private Snackbar snackbarCart;
    VolleyError volleyError;
    int flag = 0;

    private RecyclerCategoryItems categoryItems;
    private CardView cartView;
    private TextView cartCount, totalSum;
    private Button viewButton;

    @SuppressLint("SourceLockedOrientationActivity")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_food_items);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(233, 73, 73));
        }
        parent_view = findViewById(android.R.id.content);
        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id", "0");
        category = getApplicationContext().getSharedPreferences("Category", getApplicationContext().MODE_PRIVATE);
        catID = category.getString("cat_id", "0");

        Log.e(TAG, "cat" + catID);
        chair = getApplicationContext().getSharedPreferences("Chair", getApplicationContext().MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");


        progressBar = (ProgressBar) findViewById(R.id.progressDialogue);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomePage.class));
                finish();
            }
        });


        categoryProducts = (RecyclerView) findViewById(R.id.itemRecycler);

        getCategoryItems(userID, chairID, catID, getApplicationContext());// get Category Items
        //showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());// to show cart snackBar

        cartView = (CardView) findViewById(R.id.cartView);
        cartCount = (TextView) findViewById(R.id.countText);
        totalSum = (TextView) findViewById(R.id.totalSum);
        viewButton = (Button) findViewById(R.id.viewButton);

        checkCart(userID, chairID, getApplicationContext());

        new Handler().postDelayed(new Runnable() {
            @Override

            public void run() {

                if (flag == 1) {
                    callNoInternet();
                }

            }
        }, 10000);

        cartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListFoodCartSheet listFoodCartSheet = new ListFoodCartSheet(getApplicationContext(), catID, categoryItems);
                listFoodCartSheet.setCancelable(true);
                listFoodCartSheet.show(getSupportFragmentManager(), listFoodCartSheet.getTag());

            }
        });

    }

    public void checkCart(String userID, String chairID, Context applicationContext) {


        viewCartList(userID, chairID, applicationContext);


    }

    private void callNoInternet() {
        InternetHandler handler = new InternetHandler(getApplicationContext(), volleyError);
        handler.checkServerConnection();
    }


    public void getCategoryItems(final String userID, final String chair_id, final String catID, final Context applicationContext) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Category_Search", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Log.e(TAG,"Response"+response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.INVISIBLE);
                        JSONArray jsonArray = object.getJSONArray("data");

                        generateRecycler(jsonArray);


                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);

                volleyError = error;
                flag = 1;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", userID);
                map.put("category_id", catID);
                map.put("chair_id", chair_id);
                return map;
            }

        };
        requestQueue.add(stringRequest);
    }

    private void generateRecycler(JSONArray jsonArray) {
        categoryItems = new RecyclerCategoryItems(getApplicationContext(), jsonArray, this, cartView, cartCount, totalSum);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryProducts.setLayoutManager(linearLayoutManager);
        categoryProducts.setItemAnimator(new DefaultItemAnimator());
        categoryProducts.setAdapter(categoryItems);
    }

    //****************************click interface***********************************************************************
    @Override
    public void onProductAddedCart(int index, String product_id) {
        checkProduct(userID, chairID, product_id, getApplicationContext(), index);
    }

    @Override
    public void onProductCartChange(int index, String product_id, int count, String sub_id) {
        Log.e(TAG, "abstract   " + index);
        checkSubMenu(userID, chairID, product_id, index, count);
    }

    @Override
    public void onProductRemovedFromCart(int index, String product_id, int count) {

    }

//******************************************************************************************************************

//***************************************Check product***************************************************************************

    public void checkProduct(final String user_ID, final String chair_ID,
                             final String product_id, final Context context, final int index) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Product_Submenu", new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Check Response" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {

                        JSONArray jsonArray = null;
                        try {
                            jsonArray = jsonObject.getJSONArray("data");

                            Log.e(TAG, index + "...pos");

                            Log.e(TAG, product_id);

                            CategorySubMenu categorySheetSubMenu = new CategorySubMenu(getApplicationContext(),
                                    jsonObject, chairID, product_id, parent_view, index, categoryItems);
                            categorySheetSubMenu.show(getSupportFragmentManager(), categorySheetSubMenu.getTag());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {


                        ToCart(user_ID, chair_ID, product_id, "0", "1", 1, context, index, categoryItems);


                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);

                InternetHandler handler = new InternetHandler(context, error);
                handler.checkServerConnection();

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", user_ID);
                map.put("chair_id", chair_ID);
                map.put("product_id", product_id);
                return map;
            }
        };
        requestQueue.add(stringRequest);


    }


//*********************************************************************************************************************************

//**********************************************AddToCart***************************************************************

    public void ToCart(final String userID, final String chairID, final String productId, final String subId, final String action,
                       final int productCount, final Context context, final int index, final RecyclerCategoryItems categoryItems) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Add_Remove_Cart", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"Check Response"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
//                        changeCartView(holder, SearchFoodsActivity.this, "1");

                        categoryItems.updateItem(index);
                        // showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

                    } else {

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception at add" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError  A add to cart" + error);

                InternetHandler handler = new InternetHandler(context, error);
                handler.checkServerConnection();

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", userID);
                map.put("chair_id", chairID);
                map.put("product_id", productId);
                map.put("submenu_id", subId);
                map.put("action", action);
                map.put("p_count", String.valueOf(productCount));
                return map;
            }
        };
        requestQueue.add(stringRequest);


    }

    //*****************************************************ADD to from submenu*************************************************************
    public void ToCartFrom(final String userID, final String chairID, final String productId, final String subId, final String action,
                           final int productCount, final Context context, final int index, final RecyclerCategoryItems recycler, final CategorySubMenu categorySubMenu) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Add_Remove_Cart", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"Check Response"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
//                        changeCartView(holder, SearchFoodsActivity.this, "1");

                        recycler.updateItem(index);
                        categorySubMenu.dismiss();

                        // showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

                    } else {

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception at add" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError  A add to cart" + error);

                InternetHandler handler = new InternetHandler(context, error);
                handler.checkServerConnection();

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", userID);
                map.put("chair_id", chairID);
                map.put("product_id", productId);
                map.put("submenu_id", subId);
                map.put("action", action);
                map.put("p_count", String.valueOf(productCount));
                return map;
            }
        };
        requestQueue.add(stringRequest);


    }
//*************************************************************************************************************************

    //**************************************************Check submenu***********************************************************************
    public void checkSubMenu(final String user_ID, final String chair_ID,
                             final String product_id, final int index, final int count) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Product_Submenu", new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Check Response" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {

                        JSONArray jsonArray = null;
                        try {
                            jsonArray = jsonObject.getJSONArray("data");

                            Log.e(TAG, product_id);

                            Log.e(TAG, "before.." + index);

                            CategorySubMenuChange searchSheet = new CategorySubMenuChange(getApplicationContext(), product_id, snackbarCart,
                                    index, categoryItems);
                            searchSheet.show(getSupportFragmentManager(), searchSheet.getTag());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                        //    Log.e(TAG,"pos.."+index);

                        changeCartCount(userID, chairID, product_id, "0", count, index);


                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);

                InternetHandler handler = new InternetHandler(getApplicationContext(), error);
                handler.checkServerConnection();

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", user_ID);
                map.put("chair_id", chair_ID);
                map.put("product_id", product_id);
                return map;
            }
        };
        requestQueue.add(stringRequest);


    }
//**********************************************************************************************************************

//*****************************************changeCartCount***************************************************************************************

    private void changeCartCount(final String userID, final String chairID,
                                 final String product_id, final String sub_id, final int itemCount, final int index) {
        Log.e(TAG, "count   " + itemCount);
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Cart_List_Change", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "ChangeResponse" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (object.getString("status").equals("1")) {

                        progressBar.setVisibility(View.INVISIBLE);
                        categoryItems.updateItem(index);

                        // showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);

                InternetHandler handler = new InternetHandler(getApplicationContext(), error);
                handler.checkServerConnection();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();

                Log.e(TAG, "sub   " + sub_id + " \n count   " + itemCount + " \n user   " + userID + "  \n chair " + chairID + " \n p_id  " + product_id);
                map.put("login_id", userID);
                map.put("chair_id", chairID);
                map.put("product_id", product_id);
                map.put("submenu_id", sub_id);
                map.put("product_count", String.valueOf(itemCount));
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }
//*****************************************************************************************************************

    //**************************************************SNACKBAR **********************************************************************

    private void viewCartList(final String user_id, final String chair_id, Context applicationContext) {
        // Log.e(TAG,"View cart");

        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "View_Cart_List", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "CartResponse" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
                        //snackbarCart.show();

                        cartView.setVisibility(View.VISIBLE);
                        try {
                            int count = Integer.parseInt(jsonObject.getString("total_count"));
                            if (count > 1) {
                                cartCount.setText(count + " ITEMS");
                            } else {
                                cartCount.setText(count + " ITEM");
                            }
                            totalSum.setText("₹" + jsonObject.getString("total_sum") + "  plus tax");

                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException" + e);
                        }


                    } else {
                        cartView.setVisibility(View.GONE);
                        //snackbarCart.dismiss();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError @ View cart" + error);

                volleyError = error;
                flag = 1;
                ;
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                Log.e(TAG, user_id + "              lo" + chair_id);
                map.put("login_id", user_id);
                map.put("chair_id", chair_id);
                return map;
            }
        };
        requestQueue.add(stringRequest);

    }

    //**************************************************SNACKBAR **********************************************************************
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
        finish();
    }


}
