package com.example.foodify.Adaptors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodify.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecyclerOrderItems extends RecyclerView.Adapter<RecyclerOrderItems.MyViewHolder>{
    public JSONArray listArray;

    public RecyclerOrderItems(JSONArray list) {
        this.listArray = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_items, null);
        return new RecyclerOrderItems.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        try {

            JSONObject jsonObject = listArray.getJSONObject(position);

            char first = 0;
            String myString = jsonObject.getString("item_name");
            String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);

            if (!jsonObject.getString("subid").equals("0")) {
                first = jsonObject.getString("submenu_name").charAt(0);
                holder.name.setText(upperString + "   (" + first + ")");
            } else {
                holder.name.setText(upperString);
            }

            holder.itemCount.setText(jsonObject.getString("quantity") + " pcs");


            if (!jsonObject.getString("item_status").equals("PENDING")) {
                holder.removeButton.setVisibility(View.INVISIBLE);
            }


            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /*
                    call api  "Cancel_Order_Item"
                    to cancel single item from order
                     */
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return listArray.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutItem;
        TextView name, size, price, itemCount, itemTotal;
        Button removeButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
            name = itemView.findViewById(R.id.itemName);
            price = itemView.findViewById(R.id.itemPrice);
            itemCount = (TextView) itemView.findViewById(R.id.quantity);
            removeButton = (Button) itemView.findViewById(R.id.removeButton);
        }
    }
}
