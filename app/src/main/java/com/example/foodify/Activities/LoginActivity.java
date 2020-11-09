package com.example.foodify.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.wang.avi.AVLoadingIndicatorView;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";


    private EditText username, password;
    private AVLoadingIndicatorView avi;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.BLACK);
        }
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        avi = (AVLoadingIndicatorView) findViewById(R.id.loader);

    }

    public void LoginFunction(View view) {
        boolean stat = false;
        if (username.getText().toString().isEmpty()) {
            username.setError("required");
            stat = true;
        }
        if (password.getText().toString().isEmpty()) {
            password.setError("required");
            stat = true;
        }
        if (!stat) {
            startAnim();
            // Log.e(TAG,WebServices.BaseUrl );


            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    WebServices.BaseUrl + "Login", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject object = new JSONObject(response.trim());
                        Log.e(TAG, "Response" + object);
                        String status = object.getString("status");
                        if (status.equals("1") && object.getString("usertype").equals("Hotal_login")) {
                            stopAnim();


                            SharedPreferences.Editor sp = getSharedPreferences("Login", Context.MODE_PRIVATE).edit();
                            sp.clear();
                            sp.putString("user_id", object.getString("userid"));
                            sp.commit();


                            generateUserToken();


                            Intent intent = new Intent(getApplicationContext(), SelectChair.class);
                            startActivity(intent);
                            finish();
                        } else if (status.equals("1") && object.getString("usertype").equals("Kitchen")) {
                            stopAnim();


                            SharedPreferences.Editor sp = getSharedPreferences("Login", Context.MODE_PRIVATE).edit();
                            sp.clear();
                            sp.putString("user_id", object.getString("userid"));
                            sp.putString("shop_id", object.getString("hotelid"));
                            sp.commit();


                            generateToken(object.getString("userid"));

                            Intent intent = new Intent(getApplicationContext(), KitchenHome.class);
                            startActivity(intent);
                            finish();
                        } else {
                            stopAnim();
                            Toast.makeText(LoginActivity.this, "Fails", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException" + e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e(TAG, "VolleyError" + volleyError);

                    InternetHandler handler = new InternetHandler(getApplicationContext(), volleyError);
                    handler.checkServerConnection();

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    Log.e(TAG, "para" + username.getText().toString() + "\n" +
                            password.getText().toString());
                    map.put("username", username.getText().toString());
                    map.put("password", password.getText().toString());
                    return map;
                }
            };
            requestQueue.add(stringRequest);

        }
    }

    private void generateUserToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.e(TAG, "FCM Token\n" + token);

                SharedPreferences.Editor fcnShared = getSharedPreferences("FCM", Context.MODE_PRIVATE).edit();
                fcnShared.clear();
                fcnShared.putString("userToken_id", token);
                fcnShared.commit();

            }
        });
    }

    private void generateToken(final String userid) {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.e(TAG, "FCM Token\n" + token);

                if (!token.equals("")) {
                    sendToServer(userid, token);
                }

                SharedPreferences.Editor fcnShared = getSharedPreferences("FCM", Context.MODE_PRIVATE).edit();
                fcnShared.clear();
                fcnShared.putString("kitchenToken", token);
                fcnShared.commit();

            }
        });

    }

    private void sendToServer(final String user_id, final String token) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                WebServices.BaseUrl + "Save_Token", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);
                try {
                    JSONObject object = new JSONObject(response.trim());

                    if (object.getString("status").equals("1")) {


                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Exception" + e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError" + error);

                InternetHandler handler = new InternetHandler(getApplicationContext(), error);
                handler.checkServerConnection();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("login_id", user_id);
                map.put("token_id", token);
                return map;
            }

        };
        requestQueue.add(stringRequest);

    }

    void startAnim() {
        avi.show();
        // or avi.smoothToShow();
    }

    void stopAnim() {
        avi.hide();
        // or avi.smoothToHide();
    }

}
