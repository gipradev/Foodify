package com.example.foodify.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.example.foodify.Activities.SearchActivity;
import com.example.foodify.R;
import com.example.foodify.WebServices;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SubmenuSearchSheet extends BottomSheetDialogFragment implements View.OnClickListener {
  private static final String TAG = "ItemSheetFragment";
  private final Context context;
  private final Snackbar snackBar;
  //private final View root;

  private ProgressBar progressBar;
  private SharedPreferences user,chair,product;
  private String userID,chairID,product_id;
  private ImageView itemImage;
  private TextView itemName,itemPrice,addToCart;
  private RecyclerView recycleSubItems,recycleCartItems;
  private String subId = "0";
  private LinearLayout subMenuLayout;
  private Button category,addMore;
  private String count ="1";
  float price = 0,totalPrice =0,itemCount=1;
  private int singleItem=1;
  private float totalItemPrice;
  private LinearLayout cartView,addView;
  private Button closeButton;

  public SubmenuSearchSheet(Context applicationContext, String product_id, Snackbar snackbar) {
    this.context = applicationContext;
    this.snackBar = snackbar;
    this.product_id= product_id;
//    this.root = parentView;


  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.submenu_buttom_sheet, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);




    progressBar = (ProgressBar)  view.findViewById(R.id.progressDialogue);

    user = context.getSharedPreferences("Login", context.MODE_PRIVATE);
    userID = user.getString("user_id","0");

    chair = context.getSharedPreferences("Chair", context.MODE_PRIVATE);
    chairID = chair.getString("chair_id","0");
    Log.e(TAG,"chair"+chairID);

//    product = context.getSharedPreferences("Product", context.MODE_PRIVATE);
//    product_id = product.getString("product_id","0");
//    Log.e(TAG,"Here"+product_id);

    progressBar = (ProgressBar)  view.findViewById(R.id.progressDialogue);
    cartView = (LinearLayout) view.findViewById(R.id.itemsInCart);
    addView = (LinearLayout) view.findViewById(R.id.emptyCart);
    // itemImage = (ImageView) findViewById(R.id.itemImage);
    itemName = (TextView)  view.findViewById(R.id.itemName);
    itemPrice = (TextView)  view.findViewById(R.id.itemPrice);

    category = (Button)  view.findViewById(R.id.cat);
    itemImage = (ImageView) view.findViewById(R.id.itemImage);
    addToCart = (TextView)  view.findViewById(R.id.addToCart);
    closeButton = (Button) view.findViewById(R.id.close);
    addMore = (Button)  view.findViewById(R.id.addMore);

    recycleSubItems = (RecyclerView)  view.findViewById(R.id.submenuList);
    recycleCartItems = (RecyclerView)  view.findViewById(R.id.itemsInCart);
    subMenuLayout = (LinearLayout)  view.findViewById(R.id.subMenuLayout);


    //checkProduct(userID,chairID,product_id);


    getItemDetails(product_id);
    getCartData(userID,chairID,product_id);

    addToCart.setOnClickListener(this);
    closeButton.setOnClickListener(this);
    addMore.setOnClickListener(this);

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
      case R.id.close:

        SubmenuSearchSheet.this.dismiss();
//        getActivity().finish();
//        startActivity(getActivity().getIntent());


        Intent i2 = new Intent(getActivity(), SearchActivity.class);
        startActivity(i2);
        getActivity().overridePendingTransition( R.anim.fade_in, R.anim.fade_out );
        getActivity().finish();

        break;
      case R.id.addMore:
       cartView.setVisibility(View.GONE);
       addView.setVisibility(View.VISIBLE);
        ViewAddToCart(userID,chairID,product_id);
        break;
      default:
        break;
    }
  }



  private void ViewAddToCart(final String user_ID, final String chair_ID, final String product_id) {
    progressBar.setVisibility(View.VISIBLE);
    RequestQueue requestQueue = Volley.newRequestQueue(context);
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
              addView.setVisibility(View.VISIBLE);
              jsonArray = jsonObject.getJSONArray("data");
              ListPriceAdapter listPriceAdapter = new ListPriceAdapter(context,jsonArray);
              LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(context);
              linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
              recycleSubItems.setLayoutManager(linearLayoutManagerOffer);
              recycleSubItems.setItemAnimator(new DefaultItemAnimator());
              recycleSubItems.setAdapter(listPriceAdapter);
            } catch (JSONException e) {
              e.printStackTrace();
            }
            //itemCart.setText(count);
            //getItemDetails(product_id);
          }

          else {
//            addView.setVisibility(View.VISIBLE);
//            //itemCart.setText(count);
//            addButton.setEnabled(true);
//            removeButton.setEnabled(true);
//            addToCart.setEnabled(true);
//            //addToCart.setBackgroundColor(Color.parseColor("#00ABE9"));
//            addToCart.setBackgroundResource(R.drawable.add_to_cart_enabled);
//            progressBar.setVisibility(View.GONE);
//            subMenuLayout.setVisibility(View.GONE);
//
//            getItemDetails(product_id);
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
        map.put("login_id",user_ID);
        map.put("chair_id", chair_ID);
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
      holder.checkBox.setClickable(false);
      try{

        final JSONObject object=array.getJSONObject(position);
        String myString = object.getString("size");
        String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
        holder.checkBox.setText(upperString);
        holder.price.setText("₹ "+object.getString("price")+".00");
        holder.checkBox.setChecked(lastSelectedPosition == position);



//        addToCart.setEnabled(true);
//        addToCart.setBackgroundResource(R.drawable.cart_background);

        holder.layoutItem.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            lastSelectedPosition = position;
            try {

              notifyDataSetChanged();
              totalPrice = 0;
              subId= array.getJSONObject(position).getString("submenuid");
              Log.e(TAG,subId);

              price = Float.parseFloat(array.getJSONObject(position).getString("price"));



              totalPrice = totalPrice +(price * itemCount);
              addToCart.setText("Add "+singleItem+ " To Cart :"+ "₹ "+totalPrice);

              addToCart.setEnabled(true);
              addToCart.setBackgroundResource(R.drawable.cart_background);

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
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    StringRequest stringRequest = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl+"Product_Details", new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
      //  Log.e(TAG,"Response"+response);
        try {
          JSONObject object=new JSONObject(response.trim());
          String status = object.getString("status");
          if(status.equals("1")){
            progressBar.setVisibility(View.GONE);
            Picasso.get().load(object.getString("image"))
                    .resize(100, 100)
                    .centerCrop().into(itemImage);

            String myString = object.getString("itemname");
            String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
            itemName.setText(upperString);
//
//            if (object.getString("cart_count").equals("0")){
//              itemCart.setText("1");
//            }
//            else {
//              Log.e(TAG,"cart   "+object.getString("cart_count"));
//              count = object.getString("cart_count");
//            }
            //    Log.e(TAG,"count"+object.getString("cart_count"));

            itemPrice.setText("₹ "+object.getString("price")+".00");
            price =  Float.parseFloat(object.getString("price"));

            if (object.getString("categoryname").equals("veg")){
              category.setBackgroundResource(R.drawable.veg);
            }
            else{
              category.setBackgroundResource(R.drawable.non_veg);
            }





            totalItemPrice = totalItemPrice +(price * singleItem);
            addToCart.setText("Add "+singleItem+ " To Cart :"+ "₹ "+totalItemPrice);


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
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    StringRequest stringRequest  = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl +"Add_Remove_Cart", new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        //Log.e(TAG,"Check Response"+response);
        try {
          JSONObject jsonObject = new JSONObject(response.trim());
          if(jsonObject.getString("status").equals("1")){

            Toast toast = Toast.makeText(context,"Added", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            SubmenuSearchSheet.this.dismiss();
//
//            new SearchActivity().showSnackBar(context, userID,chairID,getActivity().getSupportFragmentManager());
          }
          else {

          }
        } catch (JSONException e) {
          Log.e(TAG,"Exception at add"+e);
        }

      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG,"VolleyError  A add to cart"+error);
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



//**************************************************SUBMENU CART***********************************************************************

  private void getCartData(final String userID, final String chairID, final String product_id) {
    progressBar.setVisibility(View.VISIBLE);
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    StringRequest stringRequest = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl + "Get_submenus_cart", new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        Log.e(TAG,"CartResponse"+response);
        try {
          JSONObject jsonObject = new JSONObject(response.trim());
          if (jsonObject.getString("status").equals("1")) {
            progressBar.setVisibility(View.GONE);
            cartView.setVisibility(View.VISIBLE);
            try {
              String myString = jsonObject.getString("product_name");
              String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
              JSONArray jsonArray = jsonObject.getJSONArray("data");
              ListCartAdapter listCartAdapter = new ListCartAdapter(context, jsonArray,upperString);
              LinearLayoutManager linearLayoutManagerOffer = new LinearLayoutManager(getActivity());
              linearLayoutManagerOffer.setOrientation(LinearLayoutManager.VERTICAL);
              recycleCartItems.setLayoutManager(linearLayoutManagerOffer);
              recycleCartItems.setItemAnimator(new DefaultItemAnimator());
              recycleCartItems.setAdapter(listCartAdapter);
              //setPriceValues(jsonObject,jsonArray);


            } catch (JSONException e) {
              Log.e(TAG, "Excception" + e);
            }

          } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(context, "No data", Toast.LENGTH_SHORT).show();
            snackBar.dismiss();
            SubmenuSearchSheet.this.dismiss();

            Intent i2 = new Intent(getActivity(), SearchActivity.class);
            startActivity(i2);
            getActivity().overridePendingTransition( R.anim.fade_in, R.anim.fade_out );
            getActivity().finish();

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
        map.put("product_id", product_id);
        return map;
      }
    };
    requestQueue.add(stringRequest);
  }


//***********************************************************************************************************************************


//*****************************************************LISTING CART******************************************************************
  public class ListCartAdapter extends RecyclerView.Adapter<ListCartAdapter.MyViewHolder> {

    private final Context con;
    private final JSONArray array;
    private static final String TAG = "ListPriceAdapter";
  private final String productName;
  private String count,productId,sub_id;

    public ListCartAdapter(Context con, JSONArray array, String upperString) {
      this.con = con;
      this.array = array;
      this.productName = upperString;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
      LinearLayout layoutItem;
      TextView name, size, price,itemTotal;
      Button itemCount,plusButton,minusButton,removeButton,category;



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
        //category = (Button) itemView.findViewById(R.id.cat);

      }


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_cart_items, parent, false);
      return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

//      product = context.getSharedPreferences("Product", context.MODE_PRIVATE);
//      productId = product.getString("product_id","0");
//      Log.e(TAG,"Here"+productId);

      try {

        final JSONObject object = array.getJSONObject(position);
        setCartValues(object, holder);

        if (array.getJSONObject(position).getString("product_count").equals("1")){
          holder.minusButton.setEnabled(false);
          holder.minusButton.setBackgroundResource(R.drawable.ic_minus);
        }

        holder.plusButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            try {
              count = array.getJSONObject(position).getString("product_count");
              Log.e(TAG,"count   "+count);
              //product_id = array.getJSONObject(position).getString("product_id");
              sub_id = array.getJSONObject(position).getString("submenuid");
              // Toast.makeText(context, "clicked"+count, Toast.LENGTH_SHORT).show();
              int itemCount = Integer.parseInt(count);
              itemCount=itemCount+1;

              Log.e(TAG,itemCount+"");
              changeCartCount(userID,chairID,product_id,sub_id,itemCount);
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
              Log.e(TAG,"count   "+count);
              //product_id = array.getJSONObject(position).getString("product_id");
              sub_id = array.getJSONObject(position).getString("submenuid");
              //Toast.makeText(context, "clicked"+count, Toast.LENGTH_SHORT).show();
              int itemCount = Integer.parseInt(count);
              if (itemCount ==1){

              }
              else {

                itemCount = itemCount - 1;

                Log.e(TAG, itemCount + "");
                changeCartCount(userID, chairID, product_id, sub_id, itemCount);
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
              Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
              //product_id = array.getJSONObject(position).getString("product_id");
              sub_id = array.getJSONObject(position).getString("submenuid");
              int position = holder.getAdapterPosition();
              removeFromCartList(product_id,sub_id,position,0);
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


        holder.name.setText(productName);

        if (!object.getString("size").equals("0")) {
          char first = object.getString("size").charAt(0);
          holder.size.setText("("+first+")");
        }
        holder.price.setText("₹ " + object.getString("price") + ".00");

        holder.itemCount.setText(object.getString("product_count"));

//        if (object.getString("category_name").equals("Non veg")){
//          holder.category.setBackgroundResource(R.drawable.non_veg);
//        }
//        else {
//          holder.category.setBackgroundResource(R.drawable.veg);
//        }

        float itemTotal = Float.parseFloat(object.getString("product_count")) *
                Float.parseFloat(object.getString("price"));
        holder.itemTotal.setText("₹ "+itemTotal+"0");

      } catch (Exception e) {
        Log.e(TAG, e + "");
      }
    }

    @Override
    public int getItemCount() {
      return array.length();
    }

    private void removeFromCartList(final String product_id, final String sub_id, final int position, final int action) {
      progressBar.setVisibility(View.VISIBLE);
      RequestQueue requestQueue = Volley.newRequestQueue(context);
      StringRequest stringRequest  = new StringRequest(Request.Method.POST,
              WebServices.BaseUrl +"Add_Remove_Cart", new Response.Listener<String>() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResponse(String response) {
          //Log.e(TAG,"Check Response"+response);
          try {
            JSONObject jsonObject = new JSONObject(response.trim());
            if(jsonObject.getString("status").equals("1")){
              progressBar.setVisibility(View.GONE);
              removeItem(position);

              Toast toast = Toast.makeText(context,"Item Removing", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();

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

          Log.e(TAG,product_id+"   "+sub_id);
          map.put("login_id",userID);
          map.put("chair_id",chairID);
          map.put("product_id",product_id);
          map.put("submenu_id",sub_id);
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
      getCartData(userID,chairID,product_id);

    }
  }

  private void changeCartCount(final String userID,
                               final String chairID,
                               final String product_id,
                               final String sub_id,
                               final int itemCount) {

    Log.e(TAG,product_id);
    progressBar.setVisibility(View.VISIBLE);
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    StringRequest stringRequest = new StringRequest(Request.Method.POST,
            WebServices.BaseUrl +"Cart_List_Change", new Response.Listener<String>() {
      @Override
      public void onResponse(String response) {
        Log.e(TAG,"ChangeResponse"+response);
        try {
          JSONObject object=new JSONObject(response.trim());
          String status = object.getString("status");
          if(status.equals("1")) {
            progressBar.setVisibility(View.GONE);
            getCartData(userID,chairID,product_id);
            getCartData(userID,chairID,product_id);
          }
          else {
            progressBar.setVisibility(View.GONE);
          }

        } catch (JSONException e) {
          Log.e(TAG, "Exception" + e);
        }
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG,"VolleyError"+error);
      }
    }){
      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String,String> map=new HashMap<>();

        Log.e(TAG,"sub   "+ sub_id+" \n count   "+itemCount+" \n user   "+userID+"  \n chair " +chairID+" \n p_id  "+product_id);
        map.put("login_id",userID);
        map.put("chair_id",chairID);
        map.put("product_id", product_id);
        map.put("submenu_id", sub_id);
        map.put("product_count", String.valueOf(itemCount));
        return map;
      }
    };
    requestQueue.add(stringRequest);
  }
  //***********************************************************************************************************************************

}
