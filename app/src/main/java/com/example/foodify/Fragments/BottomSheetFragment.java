package com.example.foodify.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
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
import com.example.foodify.Activities.ListFoodItems;
import com.example.foodify.Activities.PlaceOrder;
import com.example.foodify.R;
import com.example.foodify.WebServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BottomSheetFragment";

    private final Context context;
    private final LinearLayout cartView;
    private final TextView cartCount,cartSum;

    private RequestQueue requestQueue;
    private SharedPreferences user, chair, order, fcm;
    private String userID, chairId, orderID, token_id;
    private Button closeButton, addButton;
    TextView chairName, subTotal, grandTotal;
    private RecyclerView recycleSubItems;
    private LinearLayout orderButton, viewButton;
    private AVLoadingIndicatorView avi;


    public BottomSheetFragment(Context applicationContext, LinearLayout cartView, TextView cartCount, TextView totalSum) {
        this.context = applicationContext;
        this.cartView = cartView;
        this.cartCount = cartCount;
        this.cartSum = totalSum;

        requestQueue = Volley.newRequestQueue(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_menu_dialogue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        user = context.getSharedPreferences("Login", context.MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        chair = context.getSharedPreferences("Chair", context.MODE_PRIVATE);
        chairId = chair.getString("chair_id", "0");

        order = context.getSharedPreferences(chairId, context.MODE_PRIVATE);
        orderID = order.getString("order_id", "0");

        fcm = context.getSharedPreferences("FCM", context.MODE_PRIVATE);
        token_id = fcm.getString("userToken_id", "0");
        Log.e(TAG, "token      " + token_id + "     ");


        avi = (AVLoadingIndicatorView) view.findViewById(R.id.loader);


        closeButton = (Button) view.findViewById(R.id.close);
        orderButton = (LinearLayout) view.findViewById(R.id.buttonOrder);
        viewButton = (LinearLayout) view.findViewById(R.id.buttonView);
        chairName = (TextView) view.findViewById(R.id.chair);
        chairName.setText("CHAIR No. " + chairId);
        subTotal = (TextView) view.findViewById(R.id.subTotal);
        grandTotal = (TextView) view.findViewById(R.id.grandTotal);
        recycleSubItems = (RecyclerView) view.findViewById(R.id.cartItems);
        getCartData(userID, chairId);

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CustomDialogClass cdd = new CustomDialogClass(getActivity());
                cdd.show();

            }
        });

        if (orderID.equals("0")) {

            viewButton.setEnabled(false);
            viewButton.setBackgroundResource(R.drawable.disabled_button);

        }

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                BottomSheetFragment.this.dismiss();
                startActivity(new Intent(getActivity(), PlaceOrder.class));
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BottomSheetFragment.this.dismiss();
                //viewCartList(userID, chairId, context);

               // new HomePage().showSnackBar(context, userID, chairId, getActivity().getSupportFragmentManager());


            }
        });

    }

    void startAnim() {

        avi.smoothToShow();
    }

    void stopAnim() {

        avi.smoothToHide();
    }

    private void getCartData(final String userID, final String chairID) {
        startAnim();
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "View_Cart_List", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //  Log.e(TAG,"CartResponse\n"+response);
                try {
                    JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {
                        stopAnim();
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            ListCartAdapter listCartAdapter = new ListCartAdapter(context, jsonArray);
                            LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getActivity());
                            linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
                            recycleSubItems.setLayoutManager(linearLayoutManagerOffer);
                            recycleSubItems.setItemAnimator(new DefaultItemAnimator());
                            recycleSubItems.setAdapter(listCartAdapter);
                            setPriceValues(jsonObject, jsonArray);

                            viewCartList(userID, chairId, context);


                        } catch (JSONException e) {
                            Log.e(TAG, "Excception" + e);
                        }


                    } else {
                        stopAnim();
                        cartView.setVisibility(View.GONE);
                        BottomSheetFragment.this.dismiss();
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
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void setPriceValues(JSONObject jsonObject, JSONArray jsonArray) {
        float SubTotal = 0;
        try {
            grandTotal.setText("₹ " + jsonObject.getString("total_sum") + ".00");
            for (int i = 0; i < jsonArray.length(); i++) {
                SubTotal = SubTotal + (Float.parseFloat(jsonArray.getJSONObject(i).getString("price"))
                        * Float.parseFloat(jsonArray.getJSONObject(i).getString("product_count")));
            }
            subTotal.setText("₹ " + SubTotal + "0");
        } catch (JSONException e) {
            Log.e(TAG, "Excception" + e);
        }
    }

    public class ListCartAdapter extends RecyclerView.Adapter<ListCartAdapter.MyViewHolder> {

        private final Context con;
        private final JSONArray array;
        private static final String TAG = "ListPriceAdapter";
        private String count, product_id, sub_id;

        public ListCartAdapter(Context con, JSONArray array) {
            this.con = con;
            this.array = array;

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
                category = (Button) itemView.findViewById(R.id.cat);

            }


        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.submenu_items, parent, false);
            return new MyViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            try {

                final JSONObject object = array.getJSONObject(position);
                setCartValues(object, holder);

                holder.plusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            count = array.getJSONObject(position).getString("product_count");
                            product_id = array.getJSONObject(position).getString("product_id");
                            sub_id = array.getJSONObject(position).getString("submenu_id");
                            // Toast.makeText(context, "clicked"+count, Toast.LENGTH_SHORT).show();
                            int itemCount = Integer.parseInt(count);
                            itemCount = itemCount + 1;

                            Log.e(TAG, itemCount + "");
                            changeCartCount(userID, chairId, product_id, sub_id, itemCount);
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
                            product_id = array.getJSONObject(position).getString("product_id");
                            sub_id = array.getJSONObject(position).getString("submenu_id");
                            //Toast.makeText(context, "clicked"+count, Toast.LENGTH_SHORT).show();
                            int itemCount = Integer.parseInt(count);
                            if (itemCount == 1) {

                            } else {

                                itemCount = itemCount - 1;

                                Log.e(TAG, itemCount + "");
                                changeCartCount(userID, chairId, product_id, sub_id, itemCount);
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
                            // Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
                            product_id = array.getJSONObject(position).getString("product_id");
                            sub_id = array.getJSONObject(position).getString("submenu_id");
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


                String myString = object.getString("item_name");

                if (!myString.contains(" ")) {
                    holder.name.setText(myString);
                    Log.e(TAG, "object \n" + object.getString("item_name"));

                } else {

                    Log.e(TAG, "Here \n" + object.getString("item_name"));

                    String[] splitName = myString.split(" ");

                    String firstUpper = splitName[0].substring(0, 1).toUpperCase() + splitName[0].substring(1);
                    String SecondUpper = splitName[1].substring(0, 1).toUpperCase() + splitName[1].substring(1);

                    String upperString = firstUpper + " " + SecondUpper;


                    holder.name.setText(upperString);
                }
                if (!object.getString("submenu_id").equals("0")) {
                    char first = object.getString("submenu_name").charAt(0);
                    holder.size.setText("(" + first + ")");
                }
                holder.price.setText("₹ " + object.getString("price") + ".00");

                holder.itemCount.setText(object.getString("product_count"));

                if (object.getString("category_name").equals("Non vegetarian")) {
                    holder.category.setBackgroundResource(R.drawable.non_veg);
                } else {
                    holder.category.setBackgroundResource(R.drawable.veg);
                }

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
            startAnim();
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
                            stopAnim();
                            removeItem(position);

//                            Toast toast = Toast.makeText(context,"Item Removed", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();

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
                    map.put("chair_id", chairId);
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
            getCartData(userID, chairId);

        }
    }

    private void changeCartCount(final String userID,
                                 final String chairID,
                                 final String product_id,
                                 final String sub_id,
                                 final int itemCount) {
        //progressBar.setVisibility(View.VISIBLE);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Cart_List_Change", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "ChangeResponse" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        stopAnim();
                        getCartData(userID, chairID);
                    } else {
                        stopAnim();
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


    public class CustomDialogClass extends Dialog implements
            View.OnClickListener {


        public Activity activity;
        public Dialog d;
        public Button yes, no;
        private TextView message;

        public CustomDialogClass(Activity activity) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.activity = activity;

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custome_alert_dialogue);
            message = (TextView) findViewById(R.id.msgText);
            yes = (Button) findViewById(R.id.btn_yes);
            no = (Button) findViewById(R.id.btn_no);

            message.setText("Please conform.....!");
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:
                    submitCart(userID, chairId, orderID, token_id);
                    break;
                case R.id.btn_no:
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }

        private void submitCart(final String userID, final String chairId, final String orderID, final String token_id) {
            startAnim();
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context);
                Log.e(TAG, "Setting a new request queue");
            }
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    WebServices.BaseUrl + "Submit_Order", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e(TAG, "ChangeResponse" + response);
                    try {
                        JSONObject object = new JSONObject(response.trim());
                        String status = object.getString("status");
                        if (status.equals("1")) {
                            stopAnim();

                            Log.e(TAG, "order   at response  " + object.getString("order_id"));

                            SharedPreferences.Editor sp = activity.getSharedPreferences(chairId, Context.MODE_PRIVATE).edit();
                            sp.clear();
                            sp.putString("order_id", object.getString("order_id"));
                            sp.commit();

                            BottomSheetFragment.this.dismiss();
//                            Intent intent = new Intent(getActivity(),PlaceOrder.class);
//                            intent.putExtra("order_id",object.getString("order_id"));
//                            startActivity(intent);

                            activity.startActivity(new Intent(getActivity(), PlaceOrder.class));
                            activity.finish();

                        } else {
                            stopAnim();
                            Toast.makeText(activity, "server issue", Toast.LENGTH_SHORT).show();
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

                    Log.e(TAG, "\n" + token_id);

                    map.put("login_id", userID);
                    map.put("chair_id", chairId);
                    map.put("order_id", orderID);
                    map.put("token_id", token_id);
                    return map;
                }
            };
            requestQueue.add(stringRequest);

        }
    }

//***********************************************Check Cart****************************************************************
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

                        cartView.setVisibility(View.VISIBLE);
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
                        cartView.setVisibility(View.GONE);

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
