package com.example.foodify.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.foodify.R;
import com.example.foodify.WebServices;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FoodItemDetails extends AppCompatActivity {
    private static final String TAG = "FoodItemDetails";

    private SharedPreferences product,user;
    private String product_id,userID;
    private ImageView itemImage;
    private TextView itemName,itemPrice,category,itemData;
    private TextView details,ingredient;
    private RecyclerView reviewList;
    private ProgressBar progressBar;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(239,60,60));
        }
        progressBar = (ProgressBar) findViewById(R.id.progressDialogue);

        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id","0");

        product = getApplicationContext().getSharedPreferences("Product", getApplicationContext().MODE_PRIVATE);
        product_id = product.getString("product_id","0");
        Log.e(TAG,"Here"+product_id);

        itemImage= (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        category = (TextView) findViewById(R.id.itemCat);
        itemData = (TextView) findViewById(R.id.switchText);

        details = (TextView) findViewById(R.id.details);

        SpannableString content = new SpannableString("Details");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        details.setText(content);
        itemData.setText("Ingredients vary according to the region and the type of meat used. Meat (of either chicken," +
                " goat, beef, lamb,[22] prawn or fish) is the prime ingredient with rice. As is common in dishes " +
                "of the Indian subcontinent, vegetables are also used when preparing biryani," +
                " which is known as vegetable biriyani. ");
        details.setTextColor(Color.rgb(239,60,60));

        ingredient = (TextView) findViewById(R.id.ingredient);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableString content = new SpannableString("DETAILS");
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                details.setText(content);
                details.setTextColor(Color.rgb(239,60,60));
                itemData.setText("Ingredients vary according to the region and the type of meat used. Meat (of either chicken," +
                        " goat, beef, lamb,[22] prawn or fish) is the prime ingredient with rice. As is common in dishes " +
                        "of the Indian subcontinent, vegetables are also used when preparing biryani," +
                        " which is known as vegetable biriyani. ");
                ingredient.setTextColor(Color.rgb(0,0,0));
                ingredient.setText("INGREDIENTS");
            }
        });
        ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableString content = new SpannableString("INGREDIENTS");
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                ingredient.setText(content);
                itemData.setText("Marinate the chicken (preferrably over-night or at least 1-2 hours) in yogurt," +
                        " biryani masala, garam masala, lemon juice, ginger-garlic paste, 2 bay leaves, " +
                        "1 Black cardamom, 2 green cardamoms, 2 cloves and salt.");
                ingredient.setTextColor(Color.rgb(239,60,60));
                details.setTextColor(Color.rgb(0,0,0));
                details.setText("DETAILS");
            }
        });


        getItemDetails(product_id);

        reviewList = (RecyclerView) findViewById(R.id.reviewRecycle);
        RecyclerReview recyclerReview = new RecyclerReview(getApplicationContext());
        LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getApplicationContext());
        linearLayoutManagerOffer.setOrientation(LinearLayoutManager.HORIZONTAL);
        reviewList.setLayoutManager(linearLayoutManagerOffer);
        reviewList.setItemAnimator(new DefaultItemAnimator());
        reviewList.setAdapter(recyclerReview);
    }

    private void getItemDetails(final String product_id) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl+"Product_Details", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"Response"+response);
                try {

                    JSONObject object=new JSONObject(response.trim());

                    String status = object.getString("status");
                    if(status.equals("1")){
                        progressBar.setVisibility(View.GONE);


                        itemName.setText(object.getString("itemname"));
                        category.setText(object.getString("categoryname"));
                        Picasso.get().load(object.getString("image"))
                                .resize(100, 100)
                                .centerCrop().into(itemImage);
                        itemPrice.setText("₹ "+object.getString("price")+".00");

                    }
                    else{

                    }

                } catch (JSONException e) {
                    Log.e(TAG,"Exception"+e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,"VolleyError"+volleyError);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                map.put("product_id",product_id);
                map.put("login_id",userID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    public class RecyclerReview extends RecyclerView.Adapter<RecyclerReview.MyViewHolder> {
        private final Context context;

        private String[] customer_name={"Fruits","Tea","Protien","Snacks","Ice Creams"};
        private String[] customer_review={
                "Evidently, Smartphone ka Champion!  I was surprised when I found out that the outer box had 8 MP camera on ",
                "just got a mail from the company confirming that it was a sticker print issue and has a 13 MP.",
                "Needless to say, realme has to be the best in the market, right from the features offered in after- sales service!",
                "Evidently, Smartphone ka Champion!  I was surprised when I found out that the outer box had 8 MP camera on",
                "Ice just got a mail from the company confirming that it was a sticker print issue and has a 13 MP."};

        public RecyclerReview(Context applicationContext) {
            this.context = applicationContext;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(context).inflate(R.layout.review_layout,null);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//            holder.review.setText(customer_review[position]);
//            holder.name.setText(customer_name[position]);


        }

        @Override
        public int getItemCount() {
            return customer_name.length;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView name,review;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.itemImage);
//                name = (TextView) itemView.findViewById(R.id.customerName);
//                review = (TextView) itemView.findViewById(R.id.reviewText);

            }
        }
    }

}
