package com.example.foodify.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Activities.HomePage;
import com.example.foodify.R;
import com.example.foodify.WebServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ItemSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    private static final String TAG = "ItemSheetFragment";
    private final Context context;
    private final TextView cartCount, cartSum;
    private final LinearLayout cartLayout;

    //private final View root;

    private ProgressBar progressBar;
    private SharedPreferences user, chair, product;
    private String userID, chairID, product_id;
    private ImageView itemImage;
    private TextView itemName, itemPrice, addToCart, itemCart;
    private RecyclerView recycleSubItems, recycleCartItems;
    private String subId = "0";
    private LinearLayout subMenuLayout;
    private Button addButton, removeButton, addMore;
    private String count = "1";
    float price = 0, totalPrice = 0, itemCount;
    private int singleItem;
    private float totalItemPrice;
    private LinearLayout cartView, addView;
    private RequestQueue requestQueue;
    private Button category;


    public ItemSheetFragment(Context applicationContext, LinearLayout cartView, TextView totalSum, TextView cartCount) {
        this.context = applicationContext;
        this.cartLayout = cartView;
        this.cartCount = cartCount;
        this.cartSum = totalSum;


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestQueue = Volley.newRequestQueue(context);

        defineViews(view);

        setSharedPreferences();

        checkProduct(userID, chairID, product_id);

        addToCart.setOnClickListener(this);
        addButton.setOnClickListener(this);
        removeButton.setOnClickListener(this);
        addMore.setOnClickListener(this);

    }

    private void setSharedPreferences() {

        user = context.getSharedPreferences("Login", context.MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        chair = context.getSharedPreferences("Chair", context.MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");

        product = context.getSharedPreferences("Product", context.MODE_PRIVATE);
        product_id = product.getString("product_id", "0");

    }

    private void defineViews(View view) {

        progressBar = (ProgressBar) view.findViewById(R.id.progressDialogue);
        cartView = (LinearLayout) view.findViewById(R.id.itemsInCart);
        addView = (LinearLayout) view.findViewById(R.id.emptyCart);
        itemName = (TextView) view.findViewById(R.id.itemName);
        itemPrice = (TextView) view.findViewById(R.id.itemPrice);
        itemCart = (TextView) view.findViewById(R.id.cartCount);
        category = (Button) view.findViewById(R.id.cat);

        itemImage = (ImageView) view.findViewById(R.id.itemImage);
        addToCart = (TextView) view.findViewById(R.id.addToCart);
        addButton = (Button) view.findViewById(R.id.plusButton);
        addMore = (Button) view.findViewById(R.id.addMore);
        removeButton = (Button) view.findViewById(R.id.minusButton);
        recycleSubItems = (RecyclerView) view.findViewById(R.id.submenuList);
        recycleCartItems = (RecyclerView) view.findViewById(R.id.cartList);
        subMenuLayout = (LinearLayout) view.findViewById(R.id.subMenuLayout);

    }

    @Override
    public void onClick(View v) {
        float total = 0;
        switch (v.getId()) {
            case R.id.addToCart://To cart

                ToCart(userID, chairID, product_id, subId, "1", singleItem);

                break;
            case R.id.plusButton:// change count

                removeButton.setBackgroundResource(R.drawable.ic_minus);
                addButton.setBackgroundResource(R.drawable.cart_plus);
                singleItem = singleItem + 1;
                itemCart.setText(singleItem + "");
                total = total + (price * singleItem);
              //  addToCart.setText("Add " + singleItem + " To Cart :" + "₹ " + total);


                break;
            case R.id.minusButton://change count

                removeButton.setBackgroundResource(R.drawable.ic_cart_minus);
                addButton.setBackgroundResource(R.drawable.ic_plus);
                if (singleItem == 1) {
                } else {
                    singleItem = singleItem - 1;
                    itemCart.setText(singleItem + "");

                    total = total + (price * singleItem);
                   // addToCart.setText("Add " + singleItem + " To Cart :" + "₹ " + total);
                }

                break;
            case R.id.addMore:

                cartView.setVisibility(View.GONE);
                ViewAddToCart(userID, chairID, product_id);

                break;
            default:
                break;
        }
    }


    //************************************************show submenu items******************************************************
    private void ViewAddToCart(final String user_ID, final String chair_ID, final String product_id) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Product_Submenu", new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"Check Response"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        JSONArray jsonArray = null;
                        try {
                            addView.setVisibility(View.VISIBLE);
                            jsonArray = jsonObject.getJSONArray("data");
                            ListPriceAdapter listPriceAdapter = new ListPriceAdapter(context, jsonArray);
                            LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(context);
                            linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
                            recycleSubItems.setLayoutManager(linearLayoutManagerOffer);
                            recycleSubItems.setItemAnimator(new DefaultItemAnimator());
                            recycleSubItems.setAdapter(listPriceAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

//********************************************************************************************************************

    //********************************************check submenu product or not************************************************************************
    private void checkProduct(final String user_ID, final String chair_ID, final String product_id) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Product_Submenu", new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"Check Response"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1") && jsonObject.getString("cart_status").equals("0")) {
                        progressBar.setVisibility(View.GONE);
                        JSONArray jsonArray = null;
                        try {

                            addView.setVisibility(View.VISIBLE);
                            jsonArray = jsonObject.getJSONArray("data");
                            ListPriceAdapter listPriceAdapter = new ListPriceAdapter(context, jsonArray);
                            LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(context);
                            linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
                            recycleSubItems.setLayoutManager(linearLayoutManagerOffer);
                            recycleSubItems.setItemAnimator(new DefaultItemAnimator());
                            recycleSubItems.setAdapter(listPriceAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //itemCart.setText(count);
                        getItemDetails(product_id);
                    } else if (jsonObject.getString("status").equals("1") && jsonObject.getString("cart_status").equals("1")) {
                        //  JSONArray jsonArray = jsonObject.getJSONArray("data");
                        // Toast.makeText(context, "HEre", Toast.LENGTH_SHORT).show();

                        getCartData(userID, chairID, product_id);
                        getItemDetails(product_id);
                        //setPriceValues(jsonObject,jsonArray);
                    } else {
                        addView.setVisibility(View.VISIBLE);
                        //itemCart.setText(count);
                        addButton.setEnabled(true);
                        removeButton.setEnabled(true);
                        addToCart.setEnabled(true);
                        //addToCart.setBackgroundColor(Color.parseColor("#00ABE9"));
                        addToCart.setBackgroundResource(R.drawable.add_to_cart_enabled);
                        progressBar.setVisibility(View.GONE);
                        subMenuLayout.setVisibility(View.GONE);

                        getItemDetails(product_id);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", user_ID);
                map.put("chair_id", chair_ID);
                // Log.e(TAG,product_id);
                map.put("product_id", product_id);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    //********************************************************************************************************************
    //****************************************************Show submenu items****************************************************************
    public class ListPriceAdapter extends RecyclerView.Adapter<ListPriceAdapter.MyViewHolder> {


        private final Context con;
        private final JSONArray array;
        private static final String TAG = "ListPriceAdapter";

        private int lastSelectedPosition = -1;


        public ListPriceAdapter(Context con, JSONArray array) {
            this.con = con;
            this.array = array;

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout layoutItem;
            TextView name, price;
            RadioButton checkBox;

            public MyViewHolder(View itemView) {
                super(itemView);
                layoutItem = (ConstraintLayout) itemView.findViewById(R.id.layoutItem);
                name = itemView.findViewById(R.id.qtyName);
                price = itemView.findViewById(R.id.itemPrice);
                checkBox = itemView.findViewById(R.id.checkboxItem);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_menus, parent, false);
            return new MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            try {

                final JSONObject object = array.getJSONObject(position);
                String myString = object.getString("size");
                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                holder.checkBox.setText(upperString);

                holder.price.setText("₹ " + object.getString("price") + ".00");
                holder.checkBox.setChecked(lastSelectedPosition == position);
                holder.checkBox.setClickable(false);

                holder.layoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastSelectedPosition = position;
                        try {
                            notifyDataSetChanged();
                            totalPrice = 0;
                            count = "1";
                            itemCount = Float.parseFloat(count);
                            itemCart.setText(count);
                            singleItem = Integer.parseInt(itemCart.getText().toString());
                            subId = array.getJSONObject(position).getString("submenuid");
                            addButton.setEnabled(true);
                            removeButton.setEnabled(true);
                            addToCart.setEnabled(true);
                            //addToCart.setBackgroundColor(Color.parseColor("#00ABE9"));
                            addToCart.setBackgroundResource(R.drawable.add_to_cart_enabled);
                            price = Float.parseFloat(object.getString("price"));
                            totalPrice = totalPrice + (price * itemCount);
                           // addToCart.setText("Add " + singleItem + " To Cart :" + "₹ " + price);

                        } catch (Exception e) {
                            Log.e(TAG, e + "");
                        }
                    }
                });


            } catch (Exception e) {
                Log.e(TAG, e + "");
            }
        }

        @Override
        public int getItemCount() {
            return array.length();
        }

    }

    //********************************************************************************************************************
    //****************************************Product details****************************************************************************
    private void getItemDetails(final String product_id) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Product_Details", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        Picasso.get().load(object.getString("image"))
                                .resize(100, 100)
                                .centerCrop().into(itemImage);

                        String myString = object.getString("itemname");
                        String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                        itemName.setText(upperString);

                        if (object.getString("cart_count").equals("0")) {
                            itemCart.setText("1");
                        } else {
                            //  Log.e(TAG,"cart   "+object.getString("cart_count"));
                            count = object.getString("cart_count");
                        }
                        //    Log.e(TAG,"count"+object.getString("cart_count"));
                        itemCart.setText(count);
                        itemPrice.setText("₹ " + object.getString("price") + ".00");
                        price = Float.parseFloat(object.getString("price"));

                        if (object.getString("categoryname").equals("Vegetarian")) {
                            category.setBackgroundResource(R.drawable.veg);
                        } else {
                            category.setBackgroundResource(R.drawable.non_veg);
                        }


                        singleItem = Integer.parseInt(itemCart.getText().toString());


                        totalItemPrice = totalItemPrice + (price * singleItem);
                        // addToCart.setText("Add "+singleItem+ " To Cart :"+ "₹ "+totalItemPrice);
                     //   addToCart.setText("ADD");


                        //Log.e(TAG,singleItem+"asfsgsggsasgsa");


                    } else {
                        progressBar.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception " + e);
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
                map.put("product_id", product_id);
                map.put("login_id", userID);
                map.put("chair_id", chairID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    //********************************************************************************************************************
    //************************************************TO cart From List items********************************************************************
    private void ToCart(final String userID, final String chairID, final String productId, final String subId, final String action,
                        final int productCount) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Add_Remove_Cart", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"Check Response"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {

                        Toast toast = Toast.makeText(context, "Added", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        ItemSheetFragment.this.dismiss();

                        viewCartList(userID, chairID, context);

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

//**************************************************SUBMENU CART***********************************************************************

    private void getCartData(final String userID, final String chairID, final String product_id) {
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_submenus_cart", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "CartResponse" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        cartView.setVisibility(View.VISIBLE);
                        try {
                            String myString = jsonObject.getString("product_name");
                            String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            ListCartAdapter listCartAdapter = new ListCartAdapter(context, jsonArray, upperString);
                            LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getActivity());
                            linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
                            recycleCartItems.setLayoutManager(linearLayoutManagerOffer);
                            recycleCartItems.setItemAnimator(new DefaultItemAnimator());
                            recycleCartItems.setAdapter(listCartAdapter);
                            //setPriceValues(jsonObject,jsonArray);


                        } catch (JSONException e) {
                            Log.e(TAG, "Excception" + e);
                        }

                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "No data", Toast.LENGTH_SHORT).show();
                        //   snackBar.dismiss();
                        ItemSheetFragment.this.dismiss();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", userID);
                map.put("chair_id", chairID);
                map.put("product_id", product_id);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }


//***********************************************************************************************************************************


    //*****************************************************LISTING CART******************************************************************
    public class ListCartAdapter extends RecyclerView.Adapter<ListCartAdapter.MyViewHolder> {

        private final Context con;
        private final JSONArray array;
        private static final String TAG = "ListPriceAdapter";
        private final String productName;
        private String count, productId, sub_id;

        public ListCartAdapter(Context con, JSONArray array, String upperString) {
            this.con = con;
            this.array = array;
            this.productName = upperString;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutItem;
            TextView name, size, price, itemTotal;
            Button itemCount, plusButton, minusButton, removeButton, category;


            public MyViewHolder(View itemView) {
                super(itemView);
                layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
                name = itemView.findViewById(R.id.itemName);
                size = itemView.findViewById(R.id.itemSize);
                price = itemView.findViewById(R.id.itemPrice);
                itemCount = (Button) itemView.findViewById(R.id.itemCount);
                itemTotal = (TextView) itemView.findViewById(R.id.subTotal);
                plusButton = (Button) itemView.findViewById(R.id.buttonPlus);
                minusButton = (Button) itemView.findViewById(R.id.buttonMinus);
                removeButton = (Button) itemView.findViewById(R.id.deleteButton);
                //category = (Button) itemView.findViewById(R.id.cat);

            }


        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_cart_items, parent, false);
            return new MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            product = context.getSharedPreferences("Product", context.MODE_PRIVATE);
            productId = product.getString("product_id", "0");
            Log.e(TAG, "Here" + productId);

            try {

                final JSONObject object = array.getJSONObject(position);
                setCartValues(object, holder);

                holder.plusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            count = array.getJSONObject(position).getString("product_count");

                            sub_id = array.getJSONObject(position).getString("submenuid");

                            addMore.setClickable(false);
                            int itemCount = Integer.parseInt(count);
                            itemCount = itemCount + 1;

                            Log.e(TAG, itemCount + "");
                            changeCartCount(userID, chairID, productId, sub_id, itemCount);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                holder.minusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            count = array.getJSONObject(position).getString("product_count");

                            sub_id = array.getJSONObject(position).getString("submenuid");
                            addMore.setClickable(false);

                            int itemCount = Integer.parseInt(count);
                            if (itemCount == 1) {

                            } else {

                                itemCount = itemCount - 1;

                                Log.e(TAG, itemCount + "");
                                changeCartCount(userID, chairID, productId, sub_id, itemCount);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
                holder.removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            addMore.setClickable(false);
                            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();

                            sub_id = array.getJSONObject(position).getString("submenuid");
                            int position = holder.getAdapterPosition();
                            removeFromCartList(product_id, sub_id, position, 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, e + "");
            }
        }


        private void setCartValues(JSONObject object, MyViewHolder holder) {
            try {

//                holder.name.setText(productName);

                if (!object.getString("size").equals("0")) {
                    char first = object.getString("size").charAt(0);
                    holder.size.setText("(" + first + ")");

                    holder.name.setText(productName + "  (" + first + ")");

                    Log.e(TAG, "Setting " + first);
                } else {
                    holder.name.setText(productName);
                }

                holder.price.setText("₹ " + object.getString("price") + ".00");

                holder.itemCount.setText(object.getString("product_count"));


                float itemTotal = Float.parseFloat(object.getString("product_count")) *
                        Float.parseFloat(object.getString("price"));
                holder.itemTotal.setText("₹ " + itemTotal + "0");

            } catch (Exception e) {
                Log.e(TAG, e + "");
            }
        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        private void removeFromCartList(final String product_id, final String sub_id, final int position, final int action) {
            progressBar.setVisibility(View.VISIBLE);
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context);
                Log.e(TAG, "Setting a new request queue");
            }
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    WebServices.BaseUrl + "Add_Remove_Cart", new Response.Listener<String>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onResponse(String response) {
                    //Log.e(TAG,"Check Response"+response);
                    try {
                        JSONObject jsonObject = new JSONObject(response.trim());
                        if (jsonObject.getString("status").equals("1")) {
                            progressBar.setVisibility(View.GONE);
                            removeItem(position);
                            addMore.setClickable(true);

                            viewCartList(userID, chairID, context);

                            Toast toast = Toast.makeText(context, "Item Removing", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

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
                }
            }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();

                    Log.e(TAG, product_id + "   " + sub_id);
                    map.put("login_id", userID);
                    map.put("chair_id", chairID);
                    map.put("product_id", product_id);
                    map.put("submenu_id", sub_id);
                    map.put("action", String.valueOf(action));
                    return map;
                }
            };
            requestQueue.add(stringRequest);
        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void removeItem(int position) {

            array.remove(position);
            notifyItemRemoved(position);
            getCartData(userID, chairID, product_id);

        }
    }

    private void changeCartCount(final String userID,
                                 final String chairID,
                                 final String product_id,
                                 final String sub_id,
                                 final int itemCount) {

        Log.e(TAG, product_id);
        progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Cart_List_Change", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG,"ChangeResponse"+response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        getCartData(userID, chairID, product_id);
                        viewCartList(userID, chairID, context);

                        addMore.setClickable(true);

                    } else {
                        progressBar.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();

                //Log.e(TAG,"sub"+ sub_id+"  "+itemCount+"  "+userID+"  "+chairID+"  "+product_id);
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
    //***********************************************************************************************************************************


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

                        cartLayout.setVisibility(View.VISIBLE);
                        try {
                            int count = Integer.parseInt(jsonObject.getString("total_count"));
                            if (count > 1) {
                                cartCount.setText(count + " ITEMS");
                            } else {
                                cartCount.setText(count + " ITEM");
                            }
                            cartSum.setText("₹" + jsonObject.getString("total_sum") + "  plus tax");

                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException" + e);
                        }


                    } else {
                        cartLayout.setVisibility(View.GONE);

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError @ View cart" + error);

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
