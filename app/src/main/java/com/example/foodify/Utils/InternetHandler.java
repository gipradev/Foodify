package com.example.foodify.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.foodify.Activities.NoInternetActivity;

public class InternetHandler extends AppCompatActivity {
    private static final String TAG = "InternetHandler";



    private final VolleyError vError;
    private final Context activity;
    int flag = 0;

    public InternetHandler(Context applicationContext, VolleyError volleyError) {
            this.activity = applicationContext;
            this.vError = volleyError;


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkServerConnection();


    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    public void checkServerConnection() {

        String message = null;
        if (vError instanceof NetworkError) {
            message = "Cannot connect to Internet...Please check your connection!";
            flag =1;
        } else if (vError instanceof ServerError) {
            message = "The server could not be found. Please try again after some time!!";
        } else if (vError instanceof AuthFailureError) {
            message = "Cannot connect to Internet...Please check your connection!";
            flag =1;
        } else if (vError instanceof ParseError) {
            message = "Parsing error! Please try again after some time!!";
        } else if (vError instanceof NoConnectionError) {
            message = "Cannot connect to Internet...Please check your connection!";
            flag =1;
        } else if (vError instanceof TimeoutError) {
            message = "Connection TimeOut! Please check your internet connection.";
            flag =1;
        }

        Log.e(TAG,message);
        Log.e(TAG, flag+"");
        if (flag == 1){
//            Toast toast = Toast.makeText(activity,"Please check your Internet Connection", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
            Intent intent = new Intent(activity,NoInternetActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }
}
