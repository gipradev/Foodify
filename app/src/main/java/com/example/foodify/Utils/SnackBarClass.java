package com.example.foodify.Utils;

import android.graphics.Color;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;


public class SnackBarClass {
    public static void showSBMargin(Snackbar snackbar, int side, int bottom){
        final View snackBarView = snackbar.getView();

        final CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) snackBarView.getLayoutParams();
        params.setMargins(params.leftMargin + side,
                params.topMargin,
                params.rightMargin + side,
                params.bottomMargin +bottom);
        snackBarView.setLayoutParams(params);
        snackbar.show();
    }

    public static Snackbar getSB(View v, String msg, String action){
        return Snackbar.make(v,msg, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).setActionTextColor(Color.CYAN);
    }
}
