package com.example.foodify.Adaptors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodify.Fragments.ItemSheetFragment;
import com.example.foodify.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RecyclerAllProducts extends RecyclerView.Adapter<RecyclerAllProducts.MyViewHolder> {
    private static final String TAG = "RecyclerAllProducts";
    private final Context context;

    private final JSONArray array;

    private final FragmentManager fragmentManager;
    private final LinearLayout cartSnack;
    private final TextView cartSum, cartCount;
    private String product_id;


    public RecyclerAllProducts(Context applicationContext, JSONArray jsonArray, LinearLayout cartView,
                               TextView cartCount, TextView totalSum, FragmentManager supportFragmentManager) {
        this.context = applicationContext;
        this.array = jsonArray;
        this.cartSnack = cartView;
        this.cartCount = cartCount;
        this.cartSum = totalSum;
        this.fragmentManager = supportFragmentManager;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.top_recycler_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        try {


            JSONObject object = array.getJSONObject(position);

            Picasso.get().load(object.getString("image"))
                    .resize(100, 100)
                    .centerCrop().into(holder.image);
            String myString = object.getString("item");
            String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
            holder.name.setText(upperString);
            holder.price.setText("₹ " + object.getString("price"));


            holder.cardItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        product_id = array.getJSONObject(position).getString("productid");
                        Log.e(TAG, "p_id" + product_id);
                        SharedPreferences.Editor sharedProduct = context.getSharedPreferences("Product", Context.MODE_PRIVATE).edit();
                        sharedProduct.clear();
                        sharedProduct.putString("product_id", product_id);
                        sharedProduct.commit();

                        ItemSheetFragment itemSheetFragment = new ItemSheetFragment(context, cartSnack, cartSum, cartCount);
//                        itemSheetFragment.setCancelable(false);
                        itemSheetFragment.show(fragmentManager, itemSheetFragment.getTag());

                    } catch (JSONException e) {
                        e.printStackTrace();
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
        ImageView image;
        TextView name, price;
        private final CardView cardItem;
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
