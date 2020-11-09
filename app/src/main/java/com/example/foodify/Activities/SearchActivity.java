package com.example.foodify.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;



import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Fragments.SearchCartSheet;
import com.example.foodify.Fragments.SearchSheetSubMenu;
import com.example.foodify.Fragments.SubmenuSearchSheet;
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

import static com.example.foodify.Activities.ListFoodItems.parent_view;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private SharedPreferences user, chair;
    private String userID, chairID;
    private ProgressBar progressBar;
    private RecyclerView searchProducts;
    private Button mToolbar;
    private SearchView mSearchView;
    private Snackbar snackbarCart;
    private String srarchKey;


    @SuppressLint("SourceLockedOrientationActivity")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(0, 171, 233));
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
                getSearchItems(userID, chairID, srarchKey, getApplicationContext());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
        finish();
    }

    private void getSearchItems(final String userID, final String chairID, final String query, final Context applicationContext) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Search_Items", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {

                        progressBar.setVisibility(View.INVISIBLE);
                        JSONArray jsonArray = object.getJSONArray("data");
                        RecyclerSearchItems recyclerSearchItems = new RecyclerSearchItems(getApplicationContext(), jsonArray);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
                        searchProducts.setLayoutManager(gridLayoutManager);
                        searchProducts.setItemAnimator(new DefaultItemAnimator());
                        searchProducts.setAdapter(recyclerSearchItems);

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


//******************************************Show Products**************************************************************************

    public class RecyclerSearchItems extends RecyclerView.Adapter<RecyclerSearchItems.MyViewHolder> {
        private final Context context;
        private final JSONArray array;
        private int p_count;
        private String product_id;


        public RecyclerSearchItems(Context applicationContext, JSONArray jsonArray) {
            this.context = applicationContext;
            this.array = jsonArray;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.test_two, null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            final JSONObject object;
            try {
                object = array.getJSONObject(position);

                product_id = array.getJSONObject(position).getString("productid");

                Picasso.get().load(object.getString("image"))
                        .resize(50, 50)
                        .centerCrop().into(holder.image);

                String myString = object.getString("item");
                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                holder.name.setText(upperString);
                holder.price.setText("₹ " + object.getString("price") );
                //holder.cat.setText(object.getString("category"));


                Log.e(TAG, object.getString("cart_status"));

                if (object.getString("category").equals("Vegetarian")) {
                    holder.catIcon.setBackgroundResource(R.drawable.veg);
                } else {
                    holder.catIcon.setBackgroundResource(R.drawable.non_veg);
                }


                if (object.getString("cart_status").equals("1")) {

                    Log.e(TAG, "Hoi ");
                    holder.count.setText(object.getString("cart_count"));
                    holder.addTo.setVisibility(View.GONE);
                    holder.changeCart.setVisibility(View.VISIBLE);
                } else {

                    Log.e(TAG, "called ");
                    holder.addTo.setVisibility(View.VISIBLE);
                    holder.changeCart.setVisibility(View.GONE);

                }


                if (object.getString("submenu_status").equals("0")) {
                    holder.submenuText.setVisibility(View.GONE);
                } else {
                    holder.submenuText.setVisibility(View.VISIBLE);
                }

                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            product_id = array.getJSONObject(position).getString("productid");
                            checkProduct(userID, chairID, product_id, holder);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
                holder.addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            product_id = array.getJSONObject(position).getString("productid");
                            checkProduct(userID, chairID, product_id, holder);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
                holder.plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        try {
                            p_count = Integer.parseInt(holder.count.getText().toString());
                            product_id = array.getJSONObject(position).getString("productid");
                            if (object.getString("submenu_status").equals("0")) {

                                changeCartCount(userID, chairID, product_id, "0", p_count + 1, holder, holder.count);
                            } else {
                                SubmenuSearchSheet searchSheet = new SubmenuSearchSheet(getApplicationContext(), product_id, snackbarCart);
                                searchSheet.show(getSupportFragmentManager(), searchSheet.getTag());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
                holder.minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();

                        try {
                            p_count = Integer.parseInt(holder.count.getText().toString());
                            product_id = array.getJSONObject(position).getString("productid");
                            if (object.getString("submenu_status").equals("0")) {

                                changeCartCount(userID, chairID, product_id, "0", p_count - 1, holder, holder.count);
                            } else {
                                SubmenuSearchSheet searchSheet = new SubmenuSearchSheet(getApplicationContext(), product_id, snackbarCart);
                                searchSheet.show(getSupportFragmentManager(), searchSheet.getTag());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


//
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        product_id = array.getJSONObject(position).getString("productid");
                        Log.e(TAG, "p_id" + product_id);
                        SharedPreferences.Editor sharedProduct = getSharedPreferences("Product", Context.MODE_PRIVATE).edit();
                        sharedProduct.clear();
                        sharedProduct.putString("product_id", product_id);
                        sharedProduct.commit();
                        //startActivity(new Intent(context, AddToCartItem.class));
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception" + e);
                    }

                }
            });


        }

//***************************************************cart change********************************************************************
        private void changeCartCount(final String userID,
                                     final String chairID,
                                     final String product_id,
                                     final String sub_id,
                                     final int itemCount,
                                     final MyViewHolder holder, final Button countButton) {
            Log.e(TAG, "count   " + itemCount);
            progressBar.setVisibility(View.VISIBLE);
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    WebServices.BaseUrl + "Cart_List_Change", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Log.e(TAG,"ChangeResponse"+response);
                    try {
                        JSONObject object = new JSONObject(response.trim());
                        String status = object.getString("status");
                        if (object.getString("status").equals("1") && object.getString("cart_status").equals("1")) {
                            // Toast.makeText(context, object+"", Toast.LENGTH_SHORT).show();
                            //progressBar.setVisibility(View.INVISIBLE);
                            getSearchItems(userID, chairID, srarchKey, getApplicationContext());
////                            finish();
////                            overridePendingTransition( 0, 0);
////                            startActivity(getIntent());
////                            overridePendingTransition( 0, 0);

                            showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

                        } else if (object.getString("status").equals("1") && object.getString("cart_status").equals("0")) {

                            progressBar.setVisibility(View.INVISIBLE);

                            snackbarCart.dismiss();

//                            showSnackBar(getApplicationContext(),userID, chairID, getSupportFragmentManager());
//                            Log.e(TAG,"Else if");
                            changeCartView(holder, SearchActivity.this, "0");
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

//****************************************************************************************************************************************
        @Override
        public int getItemCount() {
            return array.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView name, price, cat, submenuText;
            Button add, catIcon, plus, minus, count, addButton;
            LinearLayout addTo, changeCart;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                addTo = (LinearLayout) itemView.findViewById(R.id.addTo);
                changeCart = (LinearLayout) itemView.findViewById(R.id.cartChange);
                image = (ImageView) itemView.findViewById(R.id.itemImage);
                name = (TextView) itemView.findViewById(R.id.itemName);
                price = (TextView) itemView.findViewById(R.id.actualPrice);
//                cat =(TextView) itemView.findViewById(R.id.category);
                submenuText = (TextView) itemView.findViewById(R.id.submenuItem);
                add = (Button) itemView.findViewById(R.id.plusIcon);
                addButton = (Button) itemView.findViewById(R.id.addView);
                plus = (Button) itemView.findViewById(R.id.buttonPlus);
                minus = (Button) itemView.findViewById(R.id.buttonMinus);
                count = (Button) itemView.findViewById(R.id.itemCount);
                catIcon = (Button) itemView.findViewById(R.id.catIcon);


            }
        }


    }


//*******************************************************Check submenu*******************************************************************************


    private void checkProduct(final String user_ID, final String chair_ID, final String product_id, final RecyclerSearchItems.MyViewHolder holder) {
        progressBar.setVisibility(View.VISIBLE);
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
                        progressBar.setVisibility(View.INVISIBLE);
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = jsonObject.getJSONArray("data");

                            Log.e(TAG, product_id);
                            SearchSheetSubMenu searchSheetSubMenu = new SearchSheetSubMenu(getApplicationContext(),
                                    jsonObject, chairID, product_id, holder, parent_view, SearchActivity.this);
                            searchSheetSubMenu.show(getSupportFragmentManager(), searchSheetSubMenu.getTag());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                        ToCart(userID, chairID, product_id, "0", "1", 1, holder);

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

//*********************************************************************************************************************************

//**********************************************AddToCart***************************************************************

    private void ToCart(final String userID, final String chairID, final String productId, final String subId, final String action,
                        final int productCount, final RecyclerSearchItems.MyViewHolder holder) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Add_Remove_Cart", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"Check Response"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
                        progressBar.setVisibility(View.INVISIBLE);

                        changeCartView(holder, SearchActivity.this, "1");

                        showSnackBar(getApplicationContext(), userID, chairID, getSupportFragmentManager());

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

                InternetHandler handler = new InternetHandler(getApplicationContext(), error);
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

    public static void changeCartView(final RecyclerSearchItems.MyViewHolder holder,
                                      SearchActivity activity, String action) {

        if (action.equals("1")) {
            if (holder.addTo.getVisibility() == View.VISIBLE) {
                Animation out = AnimationUtils.makeOutAnimation(activity, true);
                holder.addTo.startAnimation(out);
                //

            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.addTo.setVisibility(View.GONE);
                    holder.changeCart.setVisibility(View.VISIBLE);

                    //setLayoutValues();
                }
            }, 200);
        } else {
            if (holder.changeCart.getVisibility() == View.VISIBLE) {
                Animation out = AnimationUtils.makeOutAnimation(activity, true);
                holder.changeCart.startAnimation(out);
                holder.changeCart.setVisibility(View.GONE);

            }


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.addTo.setVisibility(View.VISIBLE);
                    //TranslateAnimation animate = new TranslateAnimation(0,holder.changeCart.getWidth(),0,0);
//                                animate.setDuration(500);
//                                animate.setFillAfter(true);
//                                holder.changeCart.startAnimation(animate);
                    //holder.changeCart.setVisibility(View.GONE);

                }
            }, 500);
        }


    }


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
        viewCartList(context, cartCount, totalSum, user_ID, chair_ID);


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

    private void viewCartList(Context context, final TextView cartCount,
                              final TextView totalSum, final String user_id, final String chair_id) {
        Log.e(TAG, "View cart");
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "View_Cart_List", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "CartResponse" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
                        snackbarCart.show();
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
                        snackbarCart.dismiss();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError @ View cart" + error);

                InternetHandler handler = new InternetHandler(getApplicationContext(), error);
                handler.checkServerConnection();
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
