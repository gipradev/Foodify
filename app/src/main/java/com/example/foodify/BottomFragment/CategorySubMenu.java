package com.example.foodify.BottomFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.foodify.Activities.ListFoodItems;
import com.example.foodify.Adaptors.RecyclerCategoryItems;
import com.example.foodify.R;
import com.example.foodify.WebServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CategorySubMenu extends BottomSheetDialogFragment {
    private static final String TAG = "CategorySheetSubMenu";
    private final String productId;
    private final String chair_id;

    private final int itemPosition;
    private final RecyclerCategoryItems recycler;


    private ProgressBar progressBar;
    TextView itemName;
    RecyclerView subMenuList;
    LinearLayout addToCart;


    private String subId = "0";
    private final Context context;
    private final View parentView;
    private final JSONObject object;
    private RecyclerView recycleSubItems;
    private Button closeButton, cat;

    private SharedPreferences user, chair, category;
    private String userID, catID, chairID;
    private ImageView imageItem;
    private LinearLayout addButton;
    private TextView cartText;


    public CategorySubMenu(Context context, JSONObject jsonObject, String chairID, String product_id,
                           View parentView, int index, RecyclerCategoryItems categoryItems) {
        this.context = context;
        this.itemPosition = index;
        this.object = jsonObject;
        this.productId = product_id;
        this.chair_id = chairID;
        this.parentView = parentView;
        this.recycler = categoryItems;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.subitems_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = context.getSharedPreferences("Login", context.MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        category = getActivity().getSharedPreferences("Category", getActivity().MODE_PRIVATE);
        catID = category.getString("cat_id", "0");

        chair = getActivity().getSharedPreferences("Chair", getActivity().MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");

        progressBar = (ProgressBar) view.findViewById(R.id.progressDialogue);


        imageItem = (ImageView) view.findViewById(R.id.itemImage);

        closeButton = (Button) view.findViewById(R.id.close);
        addButton = (LinearLayout) view.findViewById(R.id.buttonView);
        itemName = (TextView) view.findViewById(R.id.itemName);
        cartText = (TextView) view.findViewById(R.id.cartText);
        addToCart = (LinearLayout) view.findViewById(R.id.addToCart);
        addToCart.setEnabled(false);
        cat = (Button) view.findViewById(R.id.catIcon);


        recycleSubItems = (RecyclerView) view.findViewById(R.id.submenus);
        try {
//                itemId.setText(object.getString("product_name"));
            JSONArray jsonArray = object.getJSONArray("data");
            ListPriceAdapter listPriceAdapter = new ListPriceAdapter(getActivity(), jsonArray);
            LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getActivity());
            linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
            recycleSubItems.setLayoutManager(linearLayoutManagerOffer);
            recycleSubItems.setItemAnimator(new DefaultItemAnimator());
            recycleSubItems.setAdapter(listPriceAdapter);

        } catch (JSONException e) {
            Log.e(TAG, "Excception" + e);
        }
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategorySubMenu.this.dismiss();
            }
        });

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ListFoodItems().ToCartFrom(userID, chairID, productId, subId, "1", 1,
                        context, itemPosition, recycler, CategorySubMenu.this);
            }
        });

        getItemDetails(productId);


    }


    private void getItemDetails(final String product_id) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Product_Details", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //  Log.e(TAG,"Response"+response);
                try {
                    JSONObject object = new JSONObject(response.trim());
                    String status = object.getString("status");
                    if (status.equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        Picasso.get().load(object.getString("image"))
                                .resize(100, 100)
                                .centerCrop().into(imageItem);

                        String myString = object.getString("itemname");
                        String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                        itemName.setText(upperString);

//                            if (object.getString("cart_count").equals("0")){
//                                itemCart.setText("1");
//                            }
//                            else {
//                                Log.e(TAG,"cart   "+object.getString("cart_count"));
//                                count = object.getString("cart_count");
//                            }
//                            //    Log.e(TAG,"count"+object.getString("cart_count"));
//                            itemCart.setText(count);
//                            itemPrice.setText("₹ "+object.getString("price")+".00");


                        if (object.getString("categoryname").equals("veg")) {
                            cat.setBackgroundResource(R.drawable.veg);
                        } else {
                            cat.setBackgroundResource(R.drawable.non_veg);
                        }


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


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public class ListPriceAdapter extends RecyclerView.Adapter<ListPriceAdapter.MyViewHolder> {

        float totalP = 0;
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

        @SuppressLint("ResourceAsColor")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.checkBox.setClickable(false);
            try {

                final JSONObject object = array.getJSONObject(position);
                String myString = object.getString("size");
                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                holder.checkBox.setText(upperString);
                holder.price.setText("₹ " + object.getString("price") + ".00");
                holder.checkBox.setChecked(lastSelectedPosition == position);


//                Toast.makeText(con, "ADD "+ "₹ "+ array.getJSONObject(0).getString("price")+".00", Toast.LENGTH_SHORT).show();
//
//                //cartText.setText("ADD "+ "₹ "+ array.getJSONObject(0).getString("price")+".00");
//                addToCart.setEnabled(true);
//                addToCart.setBackgroundResource(R.drawable.cart_background);

                holder.layoutItem.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onClick(View v) {
                        lastSelectedPosition = position;

                        try {
                            notifyDataSetChanged();
                            totalP = 0;
                            subId = array.getJSONObject(position).getString("submenuid");

                            totalP = totalP + Float.parseFloat(object.getString("price"));
                            Log.e(TAG, totalP + "");

                            //Toast.makeText(con, "ADD "+ "₹ "+ array.getJSONObject(position).getString("price")+".00", Toast.LENGTH_SHORT).show();

                            cartText.setText("ADD " + "₹ " + array.getJSONObject(position).getString("price") + ".00");


                            addToCart.setEnabled(true);
                            addToCart.setBackgroundResource(R.drawable.cart_background);

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

}
