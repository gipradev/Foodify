package com.example.foodify.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.Fragments.TrackOrderSheet;
import com.example.foodify.R;
import com.example.foodify.WebServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlaceOrder extends AppCompatActivity {
    private static final String TAG = "PlaceOrder";

    private Window window;
    private TextView toolbarTitle;
    private Toolbar mToolbar;
    private SharedPreferences user, chair, order, fcm;
    private String userID, chairID, orderID, token_id;
    private ProgressBar progressBar;
    private RecyclerView orderList;
    private Button addMore, continue_shopping;
    TextView order_id, chair_id, grandTotal, date;
    Button more, less;
    private LinearLayout checkOut, viewProgress;
    private RelativeLayout emptyCart, viewCart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(0, 171, 233));

        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.primary_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HomePage.class));
                finish();
            }
        });


        order_id = (TextView) findViewById(R.id.orderId);
        chair_id = (TextView) findViewById(R.id.chairId);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        date = (TextView) findViewById(R.id.orderDate);


        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id", "0");

        chair = getApplicationContext().getSharedPreferences("Chair", getApplicationContext().MODE_PRIVATE);
        chairID = chair.getString("chair_id", "0");

        order = getApplicationContext().getSharedPreferences(chairID, getApplicationContext().MODE_PRIVATE);
        orderID = order.getString("order_id", "0");


        progressBar = (ProgressBar) findViewById(R.id.progressDialogue);
        orderList = (RecyclerView) findViewById(R.id.orderRecycler);
        addMore = (Button) findViewById(R.id.addMore);
        checkOut = (LinearLayout) findViewById(R.id.checkout);
        viewProgress = (LinearLayout) findViewById(R.id.viewProgress);

        emptyCart = (RelativeLayout) findViewById(R.id.emptyCart);
        viewCart = (RelativeLayout) findViewById(R.id.itemsInCart);

        continue_shopping = (Button) findViewById(R.id.cShop);

        getOrderList(userID, chairID, orderID);


        viewProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackOrderSheet trackOrderSheet = new TrackOrderSheet();
//                        trackOrderSheet.setCancelable(false);
                trackOrderSheet.show(getSupportFragmentManager(), trackOrderSheet.getTag());
            }
        });

        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), HomePage.class));
                finish();


            }
        });

        continue_shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HomePage.class));
                finish();
            }
        });

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CustomDialogClass cdd=new CustomDialogClass(PlaceOrder.this);
                cdd.show();




            }
        });


    }

    private void getOrderList(final String userID, final String chairID, final String orderID) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Get_Order_History", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "OrderResponse" + response);

                try {
                    final JSONObject jsonObject = new JSONObject(response.trim());
                    if (jsonObject.getString("status").equals("1")) {

                        progressBar.setVisibility(View.INVISIBLE);
                        viewCart.setVisibility(View.VISIBLE);
                        order_id.setText("Order : #" + jsonObject.getString("order_id"));
                        chair_id.setText("Chair No. : #" + chairID);

                        String grand = "<font color=#080808>Total  :</font> <font color=#000dff>" + "₹ " + jsonObject.getString("grand_total") + ".00" + "</font>";
                        grandTotal.setText(Html.fromHtml(grand));
                        // grandTotal.setText("Grand Total : "+jsonObject.getString("grand_total"));
                        date.setText("Date : " + jsonObject.getString("date"));
                    } else {
                        progressBar.setVisibility(View.GONE);
                        emptyCart.setVisibility(View.VISIBLE);
                        //Toast.makeText(PlaceOrder.this, "no data", Toast.LENGTH_SHORT).show();
                    }

                    orderList.setVisibility(View.VISIBLE);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    RecyclerOrder recyclerOrder = new RecyclerOrder(getApplicationContext(), jsonArray);
                    LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getApplicationContext());
                    linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
//                    DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), VERTICAL);
//                    orderList.addItemDecoration(itemDecor);
                    orderList.setLayoutManager(linearLayoutManagerOffer);
                    orderList.setItemAnimator(new DefaultItemAnimator());
                    orderList.setAdapter(recyclerOrder);


                } catch (JSONException e) {
                    e.printStackTrace();
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
                map.put("login_id", userID);
                map.put("chair_id", chairID);
                Log.e(TAG, "orderid    :" + orderID);
                map.put("order_id", orderID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    public class RecyclerOrder extends RecyclerView.Adapter<RecyclerOrder.MyViewHolder> {
        private final Context context;
        private final JSONArray array;
        private JSONObject jsonObject;


        public RecyclerOrder(Context applicationContext, JSONArray jsonArray) {
            this.context = applicationContext;
            this.array = jsonArray;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.order_history, null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            try {
                jsonObject = array.getJSONObject(position);

                String myString = jsonObject.getString("product_name");
                String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
                holder.name.setText(upperString);


//                String subTotal = "<font color=#080808>Subtotal :</font> <font color=#000dff>"+"₹ "+jsonObject.getString("sub_total")+".00"+"</font>";
//                holder.itemTotal.setText(Html.fromHtml(subTotal));


                holder.price.setText("₹ " + jsonObject.getString("price") + ".00");
                if (!jsonObject.getString("attribute").equals("0")) {

                    char first = jsonObject.getString("attribute").charAt(0);
                    holder.size.setText("(" + first + ")");
                    // holder.size.setText("("+jsonObject.getString("attribute")+")");
                }
                if (!jsonObject.getString("category").equals("Non vegetarian")) {
                    holder.category.setBackgroundResource(R.drawable.veg);
                } else {
                    holder.category.setBackgroundResource(R.drawable.non_veg);
                }
                //holder.itemTotal.setText("Sub Total : "+jsonObject.getString("sub_total"));
                holder.itemCount.setText("Qty : " + jsonObject.getString("quantity"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        public int getItemCount() {
            return array.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutItem;
            TextView name, size, price, itemCount, itemTotal;
            Button moreButton, lessButtn, removeButton, category;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
                name = itemView.findViewById(R.id.itemName);
                size = itemView.findViewById(R.id.itemSize);
                price = itemView.findViewById(R.id.itemPrice);
                itemCount = (TextView) itemView.findViewById(R.id.quantity);
                itemTotal = (TextView) itemView.findViewById(R.id.subTotal);
                category = (Button) itemView.findViewById(R.id.cat);
            }
        }
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

            message.setText("Are you sure..??");
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:

                    SharedPreferences.Editor sp = getSharedPreferences(chairID, Context.MODE_PRIVATE).edit();
                    sp.clear();
                    sp.commit();
                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                    finish();

                    break;
                case R.id.btn_no:
                    dismiss();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }


}
