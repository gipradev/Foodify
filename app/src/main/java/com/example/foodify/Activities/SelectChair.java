package com.example.foodify.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.R;
import com.example.foodify.Utils.InternetHandler;
import com.example.foodify.WebServices;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectChair extends AppCompatActivity {

    private static final String TAG = "SelectChair";

    private Spinner spinner;
    private Button continueButton;
    private SharedPreferences user,chair;
    private String userID;

    private View parentView;
    private Window window;
    private String chairId;
    private AVLoadingIndicatorView avi;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chair);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinner = (Spinner) findViewById(R.id.spinnerChair);
        continueButton = (Button) findViewById(R.id.cButton);



        parentView = findViewById(android.R.id.content);
        avi = (AVLoadingIndicatorView) findViewById(R.id.loader);


        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(0,0,0));
        }


        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id","0");

        chair = getApplicationContext().getSharedPreferences("Chair", getApplicationContext().MODE_PRIVATE);
        chairId = chair.getString("chair_id", "0");



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getChairCount(userID);

            }
        },1000);



        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (spinner.getSelectedItem().toString().equals("select chair")){
                    Snackbar snackbar = Snackbar
                            .make(parentView,"Please select chair", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else{
                    CustomDialogClass cdd=new CustomDialogClass(SelectChair.this,spinner.getSelectedItem().toString());
                    cdd.show();
                }

            }
        });

    }

    private void getChairCount(final String userID) {
        startAnim();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl+"Get_Chair_Count", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"Response"+response);
                try {

                    JSONObject object=new JSONObject(response.trim());
                    String status = object.getString("status");
                    if(status.equals("1")){
                        stopAnim();
                        int count = Integer.parseInt(object.getString("chair_count"));
                        ArrayList<String> countArray = new ArrayList<String>();
                        countArray.add(0,"select chair");
                         for (int i = 1; i <= count; i++) {
                             countArray.add(String.valueOf(i));
                         }

                         //Log.e(TAG,countArray+"");



                        ArrayAdapter<String> itemsAdapter =
                                new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item,R.id.spinnerText, countArray);
                        spinner.setAdapter(itemsAdapter);


                    }
                    else{
                        stopAnim();
                    }

                } catch (JSONException e) {
                    Log.e(TAG,"Exception"+e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,"VolleyError"+volleyError);

                InternetHandler handler = new InternetHandler(getApplicationContext(),volleyError);
                handler.checkServerConnection();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                map.put("login_id",userID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void deleteFunction(View view) {
        startAnim();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl+"Delete_Chair_Check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //*Log.e(TAG,"Response"+response);
                try {

                    JSONObject object=new JSONObject(response.trim());
                    String status = object.getString("status");
                    if(status.equals("1")){
                        stopAnim();

                        Snackbar snackbar = Snackbar
                                .make(parentView, object.getString("message"), Snackbar.LENGTH_LONG);
                        snackbar.show();

                        SharedPreferences.Editor sp=getSharedPreferences(chairId, Context.MODE_PRIVATE).edit();
                        sp.clear();
                        sp.commit();


                    }
                    else{
                        stopAnim();
                    }

                } catch (JSONException e) {
                    Log.e(TAG,"Exception"+e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,"VolleyError"+volleyError);

                InternetHandler handler = new InternetHandler(getApplicationContext(),volleyError);
                handler.checkServerConnection();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                map.put("login_id",userID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void logoutFunction(View view) {

        LogoutDialogClass cdd=new LogoutDialogClass(SelectChair.this,userID);
        cdd.show();

    }

    public class CustomDialogClass extends Dialog implements
            View.OnClickListener {

        private final String chairValue;
        public Activity activity;
        public Dialog d;
        public Button yes, no;
        private TextView message;

        public CustomDialogClass(Activity activity, String value) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.activity = activity;
            this.chairValue = value;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custome_alert_dialogue);
            message = (TextView) findViewById(R.id.msgText);
            yes = (Button) findViewById(R.id.btn_yes);
            no = (Button) findViewById(R.id.btn_no);

            message.setText("Conform chair number :"+chairValue);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:

                    checkChair(userID,spinner.getSelectedItem().toString());

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

    private void checkChair(final String userID, final String chairValue) {

        startAnim();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl+"Vacant_Chair_Check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //*Log.e(TAG,"Response"+response);
                try {

                    JSONObject object=new JSONObject(response.trim());
                    String status = object.getString("status");
                    if(status.equals("1")){
                        stopAnim();
                        SharedPreferences.Editor sp=getSharedPreferences("Chair", Context.MODE_PRIVATE).edit();
                        sp.clear();
                        sp.putString("chair_id",chairValue);
                        sp.commit();
                        startActivity(new Intent(getApplicationContext(),HomePage.class));

                    }
                    else{
                            stopAnim();

                        Snackbar snackbar = Snackbar
                                .make(parentView, object.getString("message"), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG,"Exception"+e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,"VolleyError"+volleyError);

                InternetHandler handler = new InternetHandler(getApplicationContext(),volleyError);
                handler.checkServerConnection();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                map.put("login_id",userID);
                map.put("chair_id",chairValue);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }


    void startAnim(){
        avi.show();
        avi.smoothToShow();
    }

    void stopAnim(){
        avi.hide();
        avi.smoothToHide();
    }


    public class LogoutDialogClass extends Dialog implements
            View.OnClickListener {

        private final String userID;
        public Activity activity;
        public Dialog d;
        public Button yes, no;
        private TextView message;

        public LogoutDialogClass(Activity activity, String value) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.activity = activity;
            this.userID = value;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custome_alert_dialogue);
            message = (TextView) findViewById(R.id.msgText);
            yes = (Button) findViewById(R.id.btn_yes);
            no = (Button) findViewById(R.id.btn_no);

            message.setText("Do you want to logout.?");
            yes.setOnClickListener(this);
            no.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:

                    logOut(userID);

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

    private void logOut(final String user_ID) {

        startAnim();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl+"Logout", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"Response"+response);
                try {

                    JSONObject object=new JSONObject(response.trim());
                    String status = object.getString("status");
                    if(status.equals("1")){
                        stopAnim();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();


                    }
                    else{
                        stopAnim();

                    }

                } catch (JSONException e) {
                    Log.e(TAG,"Exception"+e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG,"VolleyError"+volleyError);

                InternetHandler handler = new InternetHandler(getApplicationContext(),volleyError);
                handler.checkServerConnection();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                map.put("login_id",user_ID);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }


}
