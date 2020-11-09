package com.example.foodify.Adaptors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

;import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Activities.ListFoodItems;
import com.example.foodify.R;
import com.example.foodify.Utils.InternetHandler;
import com.example.foodify.WebServices;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RecyclerCategoryItems extends RecyclerView.Adapter<RecyclerCategoryItems.MyViewHolder> {
    private static final String TAG = "RecyclerCategoryItems";
    private final Context context;
    private final JSONArray array;
    private final ProductsAdapterListener listener;
    private final TextView cartCount;
    private final TextView cartSum;
    private final CardView cartView;

    private SharedPreferences user, chair;
    private String userID, chairID;
    private String cartStatus;
    private String product_id;
    private int p_count;
    private String sub_id;
    private RequestQueue requestQueue;


    public RecyclerCategoryItems(Context applicationContext, JSONArray jsonArray, ProductsAdapterListener listener,
                                 CardView cartView, TextView cartCount, TextView totalSum) {
        this.context = applicationContext;
        this.array = jsonArray;
        this.listener = listener;
        this.cartCount = cartCount;
        this.cartView = cartView;
        this.cartSum = totalSum;

        getSharedVariables();
        requestQueue = Volley.newRequestQueue(context);


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.catregaory_products, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        try {
            JSONObject object = array.getJSONObject(position);


            setViewData(object, holder);

            String p_id = array.getJSONObject(position).getString("productid");
            getCartCount(userID, chairID, p_id, holder);


            holder.count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        product_id = array.getJSONObject(position).getString("productid");
                        listener.onProductAddedCart(position, product_id);
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
                        // Log.e(TAG,holder.count.getText().toString()+"on click" +p_count);
                        sub_id = array.getJSONObject(position).getString("submenu_status");
                        product_id = array.getJSONObject(position).getString("productid");
                        listener.onProductCartChange(position, product_id, p_count + 1, sub_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

            holder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {

                        p_count = Integer.parseInt(holder.count.getText().toString());
                        //  Log.e(TAG,holder.count.getText().toString()+"on click" +p_count);
                        product_id = array.getJSONObject(position).getString("productid");
                        listener.onProductCartChange(position, product_id, p_count - 1, sub_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getSharedVariables() {
        user = context.getSharedPreferences("Login", context.MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        chair = context.getSharedPreferences("Chair", context.MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");
    }

    private void setViewData(JSONObject object, MyViewHolder holder) {
        try {

            Picasso.get().load(object.getString("image"))
                    .resize(50, 50)
                    .centerCrop().into(holder.image);

            String myString = object.getString("item");
            String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
            if (upperString.length()>15){

                String [] trim = upperString.split(" ");
                if (trim.length>2){
                    holder.name.setText(trim[0]+" "+trim[1]);
                }
                else {
                    holder.name.setText(trim[0]);
                }


            }else {
                holder.name.setText(upperString);
            }

            holder.price.setText("₹ " + object.getString("price"));

            if (object.getString("category").equals("Vegetarian")) {
                holder.catIcon.setBackgroundResource(R.drawable.veg);
            } else {
                holder.catIcon.setBackgroundResource(R.drawable.non_veg);
            }

            if (object.getString("submenu_status").equals("0")) {
                holder.submenuText.setVisibility(View.GONE);
            } else {
                holder.submenuText.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCartCount(final String userID, final String chairId, final String p_id, final MyViewHolder holder) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            Log.e(TAG, "Setting a new request queue");
        }

        //  Log.e(TAG,"us"+userID+"\n"+chairId+"\n"+p_id);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_Cart_Count", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {

                        //   Log.e(TAG,holder.count.getText().toString()+"get Cart");
                        setProductItem(object, holder);


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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", userID);
                map.put("chair_id", chairId);
                map.put("product_id", p_id);
                return map;
            }


        };
        requestQueue.add(stringRequest);

    }

    private void setProductItem(JSONObject object, MyViewHolder holder) {//set cart count view
        try {
            cartStatus = object.getString("cart_status");
            //  Log.e(TAG,cartStatus);


            if (cartStatus.equals("1") && (!object.getString("product_count").equals("0"))) {


                Log.e(TAG, "here1");

                cartView.setVisibility(View.VISIBLE);

                cartSum.setText("₹ " + object.getString("total_amount") + ".00");
                int count = Integer.parseInt(object.getString("total_count"));
                if (count > 1) {
                    cartCount.setText(count + " ITEMS");
                } else {
                    cartCount.setText(count + " ITEM");
                }

                // cartCount.setText(object.getString("total_count"));
                holder.count.setText(object.getString("product_count"));
                holder.count.setClickable(false);
                holder.minus.setVisibility(View.VISIBLE);
                holder.plus.setVisibility(View.VISIBLE);


            } else if (cartStatus.equals("1") && object.getString("product_count").equals("0")) {
                Log.e(TAG, "here2");
                holder.count.setText("Add");
                cartSum.setText("₹ " + object.getString("total_amount") + ".00");

                int count = Integer.parseInt(object.getString("total_count"));
                if (count > 1) {
                    cartCount.setText(count + " ITEMS");
                } else {
                    cartCount.setText(count + " ITEM");
                }
                cartView.setVisibility(View.VISIBLE);
                holder.plus.setVisibility(View.INVISIBLE);

                holder.minus.setVisibility(View.INVISIBLE);
            } else {
                //  Log.e(TAG, "ADD ");
                //  new ListFoodItems().checkCart(userID, chairID, context);

                Log.e(TAG, "here3");
                holder.count.setText("Add");

                cartView.setVisibility(View.GONE);
                holder.plus.setVisibility(View.INVISIBLE);

                holder.minus.setVisibility(View.INVISIBLE);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


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

    public void updateItem(int position) {
        //Log.e(TAG,position+"...at updation");
        notifyItemChanged(position);
        notifyDataSetChanged();

    }

    public void updateWhole() {
        notifyDataSetChanged();
    }

    public interface ProductsAdapterListener {
        void onProductAddedCart(int index, String product_id);

        void onProductCartChange(int index, String product_id, int count, String sub_id);

        void onProductRemovedFromCart(int index, String product_id, int count);
    }
}
