package com.example.foodify.Adaptors;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodify.R;
import com.google.android.material.snackbar.Snackbar;

public class RecyclerOffer extends RecyclerView.Adapter<RecyclerOffer.MyViewHolder> {
    private final Context context;
    private final View view;
    private int[] images={R.drawable.offer_one, R.drawable.offer_two,R.drawable.offer_three,
                            R.drawable.offer_four,R.drawable.offer_five,R.drawable.offer_six};
//    private String[] image_text={"Fruits","Tea","Protien","Snacks","Ice Creams","Pizza","Fired","Fish","Soups"};

    public RecyclerOffer(Context applicationContext, View parentView) {
        this.context = applicationContext;
        this.view = parentView;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.offer_layout,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.image.setImageResource(images[position]);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //setSnackBar(context,view,"This is your SnackBar");

            }
        });
//        holder.name.setText(image_text[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.offerImage);
//            name = (TextView) itemView.findViewById(R.id.catName);
        }
    }

 public static void setSnackBar(final Context context, View root, String snackTitle) {

        final Snackbar snackbar = Snackbar.make(root, snackTitle, Snackbar.LENGTH_INDEFINITE);
// Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
// Hide the text

        LayoutInflater mInflater = LayoutInflater.from(root.getContext());
// Inflate our custom view
        View snackView = mInflater.inflate(R.layout.my_snackbar, null);

        snackView.setBackgroundColor(Color.WHITE);
        TextView textView = (TextView) layout.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        Button viewButton = (Button) snackView.findViewById(R.id.viewButton);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();

            }
        });

//If the view is not covering the whole snackbar layout, add this line
        layout.setPadding(0,0,0,0);

// Add the view to the Snackbar's layout
        layout.addView(snackView, 0);
// Show the Snackbar

        snackbar.show();

    }
}
