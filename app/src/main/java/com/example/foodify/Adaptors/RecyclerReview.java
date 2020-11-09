package com.example.foodify.Adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodify.R;


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
//        holder.review.setText(customer_review[position]);
//        holder.name.setText(customer_name[position]);


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
//            name = (TextView) itemView.findViewById(R.id.customerName);
//            review = (TextView) itemView.findViewById(R.id.reviewText);

        }
    }
}
