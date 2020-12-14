package com.example.foodify.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.foodify.R;

import java.util.Map;

public class DialogClass extends Dialog implements View.OnClickListener {
    private final DialogueListener clickaListener;
    private final Map<String, String> param;
    private TextView message;
    private Button yes, no;
    String action, position, order_id;

    public DialogClass(@NonNull Context context, DialogueListener listener, Map<String, String> map) {
        super(context);
        this.clickaListener = listener;
        this.param = map;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custome_alert_dialogue);
        message = (TextView) findViewById(R.id.msgText);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);

        action = param.get("action");
        position = param.get("position");
        order_id = param.get("order_id");

        message.setText("Confirm to " + action + " order..!");
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_yes: {
                if (action.equals("accept") || action.equals("finish")) {

                    clickaListener.onProcessOrder(order_id, action, position);

                }

                else if(action.equals("remove")){
                    clickaListener.onRemoveOrder(order_id, action ,position);
                }

                break;
            }
            case R.id.btn_no: {

                break;
            }

        }
        dismiss();
    }


    public interface DialogueListener {


        void onProcessOrder(String order_id, String action, String position);

        void onRemoveOrder(String order_id, String action, String position);


    }
}
