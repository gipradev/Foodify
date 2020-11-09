package com.example.foodify.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ToCartDialogue extends Dialog implements View.OnClickListener {
  private static final String TAG = "ToCartDialogue";

  private final String chairValue;
  private final Activity activity;

  private View parentView;
  private ProgressBar progressBar;
  private SharedPreferences user,chair,product;
  private String userID,chairID,product_id;
  private ImageView itemImage;
  private TextView itemName,itemPrice,category,addToCart,itemCart;
  private RecyclerView recycleSubItems;
  private String subId = "0";
  private LinearLayout subMenuLayout;
  private Button addButton,removeButton;
  private String count ="0";
  float price = 0,totalPrice =0,itemCount;
  private int singleItem;
  private float totalItemPrice;


  public Dialog d;
  public Button yes, no;
  private TextView message;

  public ToCartDialogue(Activity activity, String value) {
    super(activity);
    // TODO Auto-generated constructor stub
    this.activity = activity;
    this.chairValue = value;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.sequal_layout);


    parentView = findViewById(android.R.id.content);

    progressBar = (ProgressBar) findViewById(R.id.progressDialogue);

    user = activity.getSharedPreferences("Login", activity.MODE_PRIVATE);
    userID = user.getString("user_id","0");

    chair = activity.getSharedPreferences("Chair", activity.MODE_PRIVATE);
    chairID = chair.getString("chair_id","0");
    Log.e(TAG,"chair"+chairID);

    product = activity.getSharedPreferences("Product", activity.MODE_PRIVATE);
    product_id = product.getString("product_id","0");
    Log.e(TAG,"Here"+product_id);

    progressBar = (ProgressBar) findViewById(R.id.progressDialogue);
    // itemImage = (ImageView) findViewById(R.id.itemImage);
    itemName = (TextView) findViewById(R.id.itemName);
    itemPrice = (TextView) findViewById(R.id.itemPrice);
    itemCart = (TextView) findViewById(R.id.cartCount);
    category = (TextView) findViewById(R.id.itemCat);
    addToCart = (TextView) findViewById(R.id.addToCart);
    addButton = (Button) findViewById(R.id.plusButton);
    removeButton = (Button) findViewById(R.id.minusButton);
    recycleSubItems = (RecyclerView) findViewById(R.id.submenuList);
    subMenuLayout = (LinearLayout) findViewById(R.id.subMenuLayout);


    checkProduct(product_id);


    addToCart.setOnClickListener(this);
    addButton.setOnClickListener(this);
    removeButton.setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
    float total = 0;
    switch (v.getId()) {
      case R.id.addToCart:
        Log.e(TAG,singleItem+"");
        // Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
        ToCart(userID,chairID,product_id,subId,"1",singleItem);
        break;
      case R.id.plusButton:

        removeButton.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        addButton.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        singleItem = singleItem + 1;
        itemCart.setText(singleItem+"");
        total = total +(price * singleItem);
        addToCart.setText("Add "+singleItem+ " To Cart :"+ "â‚¹ "+total);

        break;
      case R.id.minusButton:
        removeButton.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        addButton.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        if (singleItem == 1){

        }
        else{
          singleItem = singleItem - 1;
          itemCart.setText(singleItem+"");

          total = total +(price * singleItem);
          addToCart.setText("Add "+singleItem+ " To Cart :"+ "â‚¹ "+total);
        }

        break;
      default:
        break;
    }
  }

  private void checkProduct(final String product_id) {
    progressBar.setVisibility(View.VISIBLE);
    RequestQueue requestQueue = Volley.newRequestQueue(activity);
    StringRequest stringRequest  = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl +"Product_Submenu", new Response.Listener<String>() {
      @SuppressLint("ResourceAsColor")
      @Override
      public void onResponse(String response) {
        //Log.e(TAG,"Check Response"+response);
        try {
          JSONObject jsonObject = new JSONObject(response.trim());
          if(jsonObject.getString("status").equals("1")){
            progressBar.setVisibility(View.GONE);

            JSONArray jsonArray = null;
            try {
              jsonArray = jsonObject.getJSONArray("data");
              ListPriceAdapter listPriceAdapter = new ListPriceAdapter(activity,jsonArray);
              LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(activity);
              linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
              recycleSubItems.setLayoutManager(linearLayoutManagerOffer);
              recycleSubItems.setItemAnimator(new DefaultItemAnimator());
              recycleSubItems.setAdapter(listPriceAdapter);
            } catch (JSONException e) {
              e.printStackTrace();
            }
            //itemCart.setText(count);
            getItemDetails(product_id);
          }
          else {
            //itemCart.setText(count);
            addButton.setEnabled(true);
            removeButton.setEnabled(true);
            addToCart.setEnabled(true);
            addToCart.setBackgroundColor(Color.parseColor("#00ABE9"));
            progressBar.setVisibility(View.GONE);
            subMenuLayout.setVisibility(View.GONE);

            getItemDetails(product_id);
          }
        } catch (JSONException e) {
          Log.e(TAG,"Exception"+e);
        }

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG,"VolleyError"+error);
      }
    }
    ){
      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> map=new HashMap<>();
        map.put("product_id",product_id);
        return map;
      }
    };
    requestQueue.add(stringRequest);
  }

  public class ListPriceAdapter extends RecyclerView.Adapter<ListPriceAdapter.MyViewHolder> {


    private final Context con;
    private final JSONArray array;
    private static final String TAG = "ListPriceAdapter";

    private int lastSelectedPosition = -1;



    public ListPriceAdapter(Context con, JSONArray array) {
      this.con = con;
      this.array = array;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
      LinearLayout layoutItem;
      TextView name,price;
      RadioButton checkBox;
      public MyViewHolder(View itemView) {
        super(itemView);
        layoutItem = (LinearLayout) itemView.findViewById(R.id.layoutItem);
        name=itemView.findViewById(R.id.qtyName);
        price=itemView.findViewById(R.id.itemPrice);
        checkBox=itemView.findViewById(R.id.checkboxItem);
      }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

      View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_menus,parent,false);
      return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
      try{

        final JSONObject object=array.getJSONObject(position);
        String myString = object.getString("size");
        String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
        holder.name.setText(upperString);
        holder.price.setText("â‚¹ "+object.getString("price")+".00");
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
              itemCount= Float.parseFloat(count);
              itemCart.setText(count);
              singleItem= Integer.parseInt(itemCart.getText().toString());
              subId = array.getJSONObject(position).getString("submenuid");
              addButton.setEnabled(true);
              removeButton.setEnabled(true);
              addToCart.setEnabled(true);
              addToCart.setBackgroundColor(Color.parseColor("#00ABE9"));
              price = Float.parseFloat(object.getString("price"));
              totalPrice = totalPrice +(price * itemCount);
              addToCart.setText("Add "+singleItem+ " To Cart :"+ "â‚¹ "+price);

            }
            catch (Exception e)
            {
              Log.e(TAG,e+"");
            }
          }
        });



      }catch (Exception e)
      {
        Log.e(TAG,e+"");
      }
    }

    @Override
    public int getItemCount() {
      return array.length();
    }

  }

  private void getItemDetails(final String product_id) {
    progressBar.setVisibility(View.VISIBLE);
    RequestQueue requestQueue = Volley.newRequestQueue(activity);
    StringRequest stringRequest = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl+"Product_Details", new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        //Log.e(TAG,"Response"+response);
        try {
          JSONObject object=new JSONObject(response.trim());
          String status = object.getString("status");
          if(status.equals("1")){
            progressBar.setVisibility(View.GONE);

//                        Picasso.get().load(object.getString("image"))
//                                .resize(100, 100)
//                                .centerCrop().into(itemImage);

            String myString = object.getString("itemname");
            String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
            itemName.setText(upperString);
            if (object.getString("cart_count").equals("0")){
              itemCart.setText("1");
            }
            else {
              count = object.getString("cart_count");
            }
            Log.e(TAG,"count"+object.getString("cart_count"));
            //itemCart.setText(count);
            itemPrice.setText("â‚¹ "+object.getString("price")+".00");
            price =  Float.parseFloat(object.getString("price"));
            category.setText("("+object.getString("categoryname")+")");

            singleItem= Integer.parseInt(itemCart.getText().toString());


            totalItemPrice = totalItemPrice +(price * singleItem);
            addToCart.setText("Add "+singleItem+ " To Cart :"+ "â‚¹ "+totalItemPrice);


            //Log.e(TAG,singleItem+"asfsgsggsasgsa");


          }
          else{
            progressBar.setVisibility(View.GONE);
          }

        } catch (JSONException e) {
          Log.e(TAG,"Exception "+e);
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
        map.put("chair_id",chairID);
        return map;
      }
    };
    requestQueue.add(stringRequest);
  }

  private void ToCart(final String userID, final String chairID, final String productId, final String subId, final String action,
                      final int productCount) {
    RequestQueue requestQueue = Volley.newRequestQueue(activity);
    StringRequest stringRequest  = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl +"Add_Remove_Cart", new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        //Log.e(TAG,"Check Response"+response);
        try {
          JSONObject jsonObject = new JSONObject(response.trim());
          if(jsonObject.getString("status").equals("1")){


            Toast toast = Toast.makeText(activity,"Added", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            activity.startActivity(new Intent(activity, HomePage.class));
            ToCartDialogue.this.dismiss();


          }
          else {

          }
        } catch (JSONException e) {
          Log.e(TAG,"Exception"+e);
        }

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG,"VolleyError"+error);
      }
    }
    ){
      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> map=new HashMap<>();
        map.put("login_id",userID);
        map.put("chair_id",chairID);
        map.put("product_id",productId);
        map.put("submenu_id",subId);
        map.put("action", action);
        map.put("p_count", String.valueOf(productCount));
        return map;
      }
    };
    requestQueue.add(stringRequest);

  }


}

