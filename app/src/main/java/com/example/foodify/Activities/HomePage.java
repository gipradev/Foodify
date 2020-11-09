package com.example.foodify.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.foodify.Adaptors.RecyclerTodaySpecial;
import com.example.foodify.Adaptors.RecyclerAllProducts;
import com.example.foodify.Fragments.BottomSheetFragment;
import com.example.foodify.Fragments.ItemSheetFragment;
import com.example.foodify.R;
import com.example.foodify.Utils.InternetHandler;
import com.example.foodify.WebServices;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;


public class HomePage extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomePage";
    private RecyclerView offerList, categoryList, popularProduct, todayProduct, topRated;
    private SharedPreferences user;
    private String userID, chairID;
    private ProgressBar progressBar;
    static View parentView;
    private SharedPreferences chair;
    private FragmentManager getFragment;
    private LinearLayout bottomCart;
    private Snackbar snackbar;
    private Snackbar snackbarCart;
    private LinearLayout searchLayout;
    private Button cart, notification;
    VolleyError volleyError;
    int flag = 0;
    private RequestQueue requestQueue;
    private LinearLayout cartView;
    private LinearLayout cartLinear;
    private TextView cartCount;
    private TextView totalSum;
    private Button viewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentView = findViewById(android.R.id.content);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        setSharedPreferences();

        progressBar = (ProgressBar) findViewById(R.id.progressDialogue);

        init(userID);

        declareCartView();

        viewCartList(userID, chairID, getApplicationContext());

        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        cart = (Button) findViewById(R.id.cartBag);
        notification = (Button) findViewById(R.id.notification);


        cart.setOnClickListener(this);
        notification.setOnClickListener(this);

        cartLinear.setOnClickListener(this);// view snackBar Cart view


        //showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());
        // viewCart(userID, chairID, getApplicationContext(), parentView);


//************************************************************************************************************************

        offerList = (RecyclerView) findViewById(R.id.offerRecycler);
//        RecyclerOffer recyclerOffer = new RecyclerOffer(getApplicationContext(), parentView);
//        LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManagerOffer.setOrientation(LinearLayoutManager.HORIZONTAL);
//        offerList.setLayoutManager(linearLayoutManagerOffer);
//        offerList.setItemAnimator(new DefaultItemAnimator());
//        offerList.setAdapter(recyclerOffer);
//************************************************************************************************************************

//************************************************************************************************************************
        categoryList = (RecyclerView) findViewById(R.id.categoryRecycler);
//        RecyclerCartAdaptor recyclerCategory = new RecyclerCartAdaptor(getApulicationContext(),jsonArray);

//************************************************************************************************************************

//************************************************************************************************************************

        popularProduct = (RecyclerView) findViewById(R.id.popularRecycler);

//************************************************************************************************************************

//************************************************************************************************************************

        todayProduct = (RecyclerView) findViewById(R.id.todayRecycler);

//************************************************************************************************************************

//************************************************************************************************************************

        topRated = (RecyclerView) findViewById(R.id.mostRecycler);
//        RecyclerTopRated recyclerTopRated = new RecyclerTopRated(getApplicationContext());
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
//        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
//        topRated.setLayoutManager(gridLayoutManager);
//        topRated.setItemAnimator(new DefaultItemAnimator());
//        topRated.setAdapter(recyclerTopRated);
//************************************************************************************************************************

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), SearchFoodsActivity.class));
                finish();

            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override

            public void run() {

                if (flag == 1) {
                    callNoInternet();
                }

            }
        }, 10000);


    }

    private void declareCartView() {
        cartView = (LinearLayout) findViewById(R.id.bottom_sheet);
        cartLinear = (LinearLayout) findViewById(R.id.linearCart);
        cartCount = (TextView) findViewById(R.id.countText);
        totalSum = (TextView) findViewById(R.id.totalSum);
        viewButton = (Button) findViewById(R.id.viewButton);
    }

    private void setSharedPreferences() {
        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        chair = getApplicationContext().getSharedPreferences("Chair", getApplicationContext().MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");
    }

    private void init(String userID) {
        getCategory(userID);
        getPopularProducts(userID);
        getTodaysSpecial(userID);
        getAllProducts(userID);
    }


    private void callNoInternet() {
        InternetHandler handler = new InternetHandler(getApplicationContext(), volleyError);
        handler.checkServerConnection();
    }

    //*************************************On click*******************************************************************************

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cartBag:
                startActivity(new Intent(getApplicationContext(), PlaceOrder.class));
                finish();
                break;

            case R.id.notification:
                Toast.makeText(this, "No notifications...", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(getApplicationContext(),PlaceOrder.class));
                break;

            case R.id.linearCart:

                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(getApplicationContext(), cartView, cartCount, totalSum);
               // bottomSheetFragment.setCancelable(false);
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

                break;

            default:

                break;
        }

    }

    //***********************************************************************************************************************

    //*********************************************GET CATEGORY**************************************************************************
    private void getCategory(final String userID) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_Category", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.e(TAG,"Response"+response);
                try {

                    JSONObject object = new JSONObject(response.trim());

                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.INVISIBLE);
                        JSONArray jsonArray = object.getJSONArray("data");
                        RecyclerCategory recyclerCategory = new RecyclerCategory(getApplicationContext(), jsonArray);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                        categoryList.setLayoutManager(linearLayoutManager);
                        categoryList.setItemAnimator(new DefaultItemAnimator());
                        categoryList.setAdapter(recyclerCategory);
                    } else {

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
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }


    //************************************************************************************************************************

    //****************************************GET POPULAR ITEMS*******************************************************************************
    private void getPopularProducts(final String userID) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_Product", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        JSONArray jsonArray = object.getJSONArray("data");
                        RecyclerPopularProduct recyclerPopularProduct = new RecyclerPopularProduct(getApplicationContext(), jsonArray);
                        LinearLayoutManager linearLayoutManagerProduct = new LinearLayoutManager(getApplicationContext());
                        linearLayoutManagerProduct.setOrientation(linearLayoutManagerProduct.HORIZONTAL);
                        popularProduct.setLayoutManager(linearLayoutManagerProduct);
                        popularProduct.setItemAnimator(new DefaultItemAnimator());
                        popularProduct.setAdapter(recyclerPopularProduct);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception:    " + e);
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
                map.put("chair_id", chairID);
                return map;
            }
        };
        requestQueue.add(stringRequest);

    }


    //****************************************GET Today's Special*******************************************************************************
    private void getTodaysSpecial(final String userID) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_Today_Special", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        JSONArray jsonArray = object.getJSONArray("data");
                        RecyclerTodaySpecial recyclerTodayProduct = new RecyclerTodaySpecial(getApplicationContext(), jsonArray,
                                cartView, cartCount, totalSum, getSupportFragmentManager());
                        LinearLayoutManager linearLayoutManagerToday = new LinearLayoutManager(getApplicationContext());
                        linearLayoutManagerToday.setOrientation(linearLayoutManagerToday.HORIZONTAL);
                        todayProduct.setLayoutManager(linearLayoutManagerToday);
                        todayProduct.setItemAnimator(new DefaultItemAnimator());
                        todayProduct.setAdapter(recyclerTodayProduct);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception:    " + e);
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
                map.put("chair_id", chairID);
                return map;
            }
        };
        requestQueue.add(stringRequest);

    }


    //**************************************************al products **********************************************************************

    private void getAllProducts(final String userID) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_all_Product", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                 Log.e(TAG, "ALL_products" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        JSONArray jsonArray = object.getJSONArray("data");
                        RecyclerAllProducts recyclerTopRated = new RecyclerAllProducts(getApplicationContext(), jsonArray,
                                cartView, cartCount, totalSum, getSupportFragmentManager());
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                        topRated.setLayoutManager(gridLayoutManager);
                        topRated.setItemAnimator(new DefaultItemAnimator());
                        topRated.setAdapter(recyclerTopRated);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception:    " + e);
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
                map.put("chair_id", chairID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }
    //************************************************* **********************************************************************

    //**************************************************CATEGORY **********************************************************************


    public class RecyclerCategory extends RecyclerView.Adapter<RecyclerCategory.MyViewHolder> {
        private static final String TAG = "RecyclerCartAdaptor";
        private final Context context;
        private final JSONArray array;

        public RecyclerCategory(Context applicationContext, JSONArray jsonArray) {
            this.context = applicationContext;
            this.array = jsonArray;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.recycler_category_item, null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            try {

                final JSONObject object = array.getJSONObject(position);
                //Log.e(TAG,"path"+object.getString("imageUrl"));
                Picasso.get().load(object.getString("imageUrl"))
                        .resize(50, 50)
                        .centerCrop().into(holder.itemImage);
                holder.itemName.setText(object.getString("category"));

                holder.itemImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            SharedPreferences.Editor sp = getSharedPreferences("Category", Context.MODE_PRIVATE).edit();
                            sp.clear();
                            sp.putString("cat_id", object.getString("catid"));
                            sp.commit();
                            startActivity(new Intent(getApplicationContext(), ListFoodItems.class));
                            finish();
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception" + e);
                        }

                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "Exception" + e);
            }

        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView itemImage;
            TextView itemName;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemImage = (ImageView) itemView.findViewById(R.id.categoryImage);
                itemName = (TextView) itemView.findViewById(R.id.catName);
            }
        }
    }
//***********************************************************************************************************************

//****************************************PopularProduct*******************************************************************************

    public class RecyclerPopularProduct extends RecyclerView.Adapter<RecyclerPopularProduct.MyViewHolder> {
        private final Context context;
        private final JSONArray array;
        String product_id;


        public RecyclerPopularProduct(Context applicationContext, JSONArray jsonArray) {
            this.context = applicationContext;
            this.array = jsonArray;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.food_item, null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

            try {

                JSONObject object = array.getJSONObject(position);

                Picasso.get().load(object.getString("image"))
                        .resize(100, 100)
                        .centerCrop().into(holder.image);
                String myString = object.getString("item");
                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                holder.name.setText(upperString);
                holder.price.setText("₹ " + object.getString("price"));


            } catch (JSONException e) {
                Log.e(TAG, "Exception" + e);
            }


            holder.cardItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        product_id = array.getJSONObject(position).getString("productid");
                        Log.e(TAG, "p_id" + product_id);
                        SharedPreferences.Editor sharedProduct = getSharedPreferences("Product", Context.MODE_PRIVATE).edit();
                        sharedProduct.clear();
                        sharedProduct.putString("product_id", product_id);
                        sharedProduct.commit();

                        ItemSheetFragment itemSheetFragment = new ItemSheetFragment(getApplicationContext(), cartView, totalSum, cartCount);
//                        itemSheetFragment.setCancelable(false);
                        itemSheetFragment.show(getSupportFragmentManager(), itemSheetFragment.getTag());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private final CardView cardItem;
            ImageView image;
            TextView name, price;

            LinearLayout countLayout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                cardItem = (CardView) itemView.findViewById(R.id.cardItem);
                image = (ImageView) itemView.findViewById(R.id.itemImage);
                name = (TextView) itemView.findViewById(R.id.itemName);
                price = (TextView) itemView.findViewById(R.id.actualPrice);

            }
        }

    }
    //********************************************CART VIEW************************************************************

    public void viewCartList(final String user_id, final String chair_id, Context applicationContext) {
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


}

