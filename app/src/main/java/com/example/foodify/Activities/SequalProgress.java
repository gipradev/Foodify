package com.example.foodify.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodify.R;
import com.transferwise.sequencelayout.SequenceStep;


public class SequalProgress  extends AppCompatActivity {


    private SequenceStep sequenceStep1,sequenceStep2,sequenceStep3,sequenceStep4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sequal_layout);

        sequenceStep1=(SequenceStep)findViewById(R.id.first);
        sequenceStep2=(SequenceStep)findViewById(R.id.second);
        sequenceStep3=(SequenceStep)findViewById(R.id.third);
        sequenceStep4=(SequenceStep)findViewById(R.id.forth);
        sequenceStep3.setActive(true);





    }
}
