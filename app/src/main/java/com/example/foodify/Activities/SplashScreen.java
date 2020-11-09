package com.example.foodify.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodify.R;
import com.example.foodify.Utils.InternetHandler;
import com.example.foodify.WebServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";

    private static int SPLASH_TIME = 5000;
    private SharedPreferences product,user;
    private String userID;
    private Window window;
    int flag = 0;
    private AVLoadingIndicatorView avi;
    private VolleyError volleyError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.GRAY);
        }


        user = getApplicationContext().getSharedPreferences("Login", getApplicationContext().MODE_PRIVATE);
        userID = user.getString("user_id","0");

        avi = (AVLoadingIndicatorView) findViewById(R.id.loader);
        startAnim();

        new Handler().postDelayed(new Runnable() {
            @Override

            public void run() {
                loginCheck(userID);
            }
        },SPLASH_TIME);



    }

    private void callNoInternet(VolleyError error) {
        Log.e(TAG,"vca");
        InternetHandler handler = new InternetHandler(getApplicationContext(),error);
        handler.checkServerConnection();
    }



    private void loginCheck(final String userID) {

        Log.e(TAG,""+WebServices.BaseUrl+"Login_Check");
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl+"Login_Check", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"Response"+response);
                try {
                    stopAnim();
                    JSONObject object=new JSONObject(response.trim());
                    String status = object.getString("status");
                    if(status.equals("1") && object.getString("usertype").equals("Hotal_login")){
                     stopAnim();
                       startActivity(new Intent(getApplicationContext(),SelectChair.class));
                       finish();
                    }
                    else if(status.equals("1") && object.getString("usertype").equals("Kitchen")){
                       stopAnim();
                        startActivity(new Intent(getApplicationContext(),KitchenHome.class));
                        finish();
                    }
                    else {
                        stopAnim();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();
                    }

                } catch (JSONException e) {
                    Log.e(TAG,"Exception"+e);
                    flag= 1;

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"VolleyError"+error);

                flag=1;
                volleyError = error;
                callingNoInternet(flag,error);




            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                Log.e(TAG,userID);
                map.put("login_id",userID);
                return map;
            }
        };
        requestQueue.add(stringRequest);

    }

    private void callingNoInternet(final int flag, final VolleyError error) {

        Log.e(TAG,flag+"");
        new Handler().postDelayed(new Runnable() {
            @Override

            public void run() {

                if (flag == 1){
                    Log.e(TAG,"ca");
                    callNoInternet(error);
                }

            }
        },SPLASH_TIME);
    }



    void startAnim(){
       // avi.show();
         avi.smoothToShow();
    }

    void stopAnim(){
        //avi.hide();
         avi.smoothToHide();
    }


}
