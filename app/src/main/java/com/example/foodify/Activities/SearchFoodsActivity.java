package com.example.foodify.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Adaptors.RecyclerSearchItems;
import com.example.foodify.BottomFragment.SearchSubMenuChange;
import com.example.foodify.BottomFragment.SearchSubMenuList;
import com.example.foodify.CartViewFragments.SearchFoodCartSheet;
import com.example.foodify.Fragments.SearchCartSheet;
import com.example.foodify.R;
import com.example.foodify.Utils.InternetHandler;
import com.example.foodify.WebServices;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.foodify.Activities.ListFoodItems.parent_view;

public class SearchFoodsActivity extends AppCompatActivity implements RecyclerSearchItems.ProductsAdapterListener {

    private static final String TAG = "SearchFoodsActivity";
    private SharedPreferences user, chair;
    private String userID, chairID;
    private ProgressBar progressBar;
    private RecyclerView searchProducts;
    private Button mToolbar;
    private SearchView mSearchView;
    private Snackbar snackbarCart;
    private String srarchKey;
    private RecyclerSearchItems recyclerSearchItems;
    private CardView cartView;
    private TextView cartCount, totalSum;
    private Button viewButton;
    private RequestQueue requestQueue;


    @SuppressLint("SourceLockedOrientationActivity")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(233, 73, 73));
        }

        mToolbar = (Button) findViewById(R.id.backArrow);

        mToolbar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomePage.class));
                finish();
            }
        });
        parent_view = findViewById(android.R.id.content);

        // showKeyBoard();


        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        chair = getApplicationContext().getSharedPreferences("Chair", getApplicationContext().MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");
        searchProducts = (RecyclerView) findViewById(R.id.itemRecycler);

        progressBar = (ProgressBar) findViewById(R.id.progressDialogue);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e(TAG, query);
                srarchKey = query;

                //renderProducts(srarchKey);
                getSearchItems(userID, chairID, srarchKey, getApplicationContext());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        cartView = (CardView) findViewById(R.id.cartView);
        cartCount = (TextView) findViewById(R.id.countText);
        totalSum = (TextView) findViewById(R.id.totalSum);
        viewButton = (Button) findViewById(R.id.viewButton);

        checkCart(userID, chairID, getApplicationContext());

        // showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

        cartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SearchFoodCartSheet searchFoodCartSheet = new SearchFoodCartSheet(getApplicationContext(), recyclerSearchItems);
                searchFoodCartSheet.setCancelable(true);
                searchFoodCartSheet.show(getSupportFragmentManager(), searchFoodCartSheet.getTag());

            }
        });

    }

    public void checkCart(String userID, String chairID, Context applicationContext) {


        viewCartList(userID, chairID, applicationContext);


    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
        finish();
    }


    private void getSearchItems(final String userID, final String chairID, final String query, final Context applicationContext) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG,"Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Search_Items", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //   Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        JSONArray jsonArray = object.getJSONArray("data");
                        generateListItems(jsonArray);

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
                map.put("login_id", userID);
                map.put("search_key", query);
                map.put("chair_id", chairID);
                return map;
            }

        };
        requestQueue.add(stringRequest);
    }

    private void generateListItems(JSONArray jsonArray) {
        recyclerSearchItems = new RecyclerSearchItems(getApplicationContext(), jsonArray, this, cartView, cartCount, totalSum);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchProducts.setLayoutManager(linearLayoutManager);
        // searchProducts.setItemAnimator(new DefaultItemAnimator());
        searchProducts.setAdapter(recyclerSearchItems);
        recyclerSearchItems.notifyDataSetChanged();
    }

    @Override
    public void onProductAddedCart(int index, String product_id) {
        checkProduct(userID, chairID, product_id, getApplicationContext(), index);
    }

    @Override
    public void onProductCartChange(int index, String product_id, int p_count, String sub_status) {
        Log.e(TAG, "abstract   " + index);
        checkSubMenu(userID, chairID, product_id, index, p_count);
    }

    @Override
    public void onProductRemovedFromCart(int index, String product_id, int p_count) {
        // checkProduct(userID, chairID, product_id, getApplicationContext(),index);
        //changeCartCount(userID, chairID, product_id, "0", p_count - 1,index);
    }


//******************************************Show Products**************************************************************************


    //*******************************************************Check submenu*******************************************************************************
    public void checkProduct(final String user_ID, final String chair_ID,
                             final String product_id, final Context context, final int index) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG,"Setting a new request queue");
        }
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
                            SearchSubMenuList searchSheetSubMenu = new SearchSubMenuList(getApplicationContext(),
                                    jsonObject, chairID, product_id, parent_view, index, recyclerSearchItems);
                            searchSheetSubMenu.show(getSupportFragmentManager(), searchSheetSubMenu.getTag());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                        ToCart(user_ID, chair_ID, product_id, "0", "1", 1, context, index, recyclerSearchItems);


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

    public void checkSubMenu(final String user_ID, final String chair_ID,
                             final String product_id, final int index, final int count) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG,"Setting a new request queue");
        }
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

                            SearchSubMenuChange searchSheet = new SearchSubMenuChange(getApplicationContext(), product_id, snackbarCart,
                                    index, recyclerSearchItems);
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

    //**********************************************AddToCart***************************************************************
    public void ToCart(final String userID, final String chairID, final String productId, final String subId, final String action,
                       final int productCount, final Context context, final int index, final RecyclerSearchItems recycler) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG,"Setting a new request queue");
        }
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


    public void ToCartFrom(final String userID, final String chairID, final String productId, final String subId, final String action,
                           final int productCount, final Context context, final int index, final RecyclerSearchItems recycler, final SearchSubMenuList searchSubMenuList) {

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
                        searchSubMenuList.dismiss();
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


//*********************************************************************************************************************************

    //***************************************************cart change********************************************************************
    private void changeCartCount(final String userID, final String chairID,
                                 final String product_id, final String sub_id, final int itemCount, final int index) {
        Log.e(TAG, "count   " + itemCount);
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG,"Setting a new request queue");
        }
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
                        recyclerSearchItems.updateItem(index);

                        // showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

                    }
//                    else if (object.getString("status").equals("1") && object.getString("cart_status").equals("0")) {
//
//                        progressBar.setVisibility(View.INVISIBLE);
//
//                        snackbarCart.dismiss();
//
////                            showSnackBar(getApplicationContext(),userID, chairID, getSupportFragmentManager());
////                            Log.e(TAG,"Else if");
//                    }
                    else {
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

    //****************************************************************************************************************************************

    public void showSnackBar(final Context context, String user_ID, String chair_ID,
                             final FragmentManager childFragmentManager) {

        Log.e(TAG, "snakcart " + parent_view);
        snackbarCart = Snackbar.make(parent_view, "", Snackbar.LENGTH_INDEFINITE);
// Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbarCart.getView();
// Hide the text
        LayoutInflater mInflater = LayoutInflater.from(parent_view.getContext());
// Inflate our custom view
        View snackView = mInflater.inflate(R.layout.my_snackbar, null);

        snackView.setBackgroundColor(Color.WHITE);
        TextView textView = (TextView) layout.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        TextView cartCount = (TextView) snackView.findViewById(R.id.countText);
        TextView totalSum = (TextView) snackView.findViewById(R.id.totalSum);
        Button viewButton = (Button) snackView.findViewById(R.id.viewButton);

        Log.e(TAG, user_ID + "  at View " + chair_ID);
//        viewCartList(context, cartCount, totalSum, user_ID, chair_ID);


        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //   Log.e(TAG, "  root   ");

                try {
                    // Log.e(TAG, "is null   " + this);
                    SearchCartSheet searchCartSheet = new SearchCartSheet(context, snackbarCart, parent_view, "");
                    searchCartSheet.setCancelable(false);
                    //Log.e(TAG,"getSupportFragmentManager\n"+childFragmentManager);
                    searchCartSheet.show(childFragmentManager, searchCartSheet.getTag());
                } catch (Exception e) {
                    Log.e(TAG, "Exception  " + e);
                }


            }
        });

//If the view is not covering the whole snackbar layout, add this line
        layout.setPadding(0, 0, 0, 0);

// Add the view to the Snackbar's layout
        layout.addView(snackView, 0);
// Show the Snackbar


    }

    private void viewCartList(final String user_id, final String chair_id, Context applicationContext) {
        // Log.e(TAG,"View cart");

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG,"Setting a new request queue");
        }
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
//******************************************************************************************************************************************************************************************
//**********************************************************************Bottom Sheet********************************************************************************************************************

//******************************************************************************************************************************************************************************************

}
