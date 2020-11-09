package com.example.foodify.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.foodify.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.transferwise.sequencelayout.SequenceStep;

public class TrackOrderSheet extends BottomSheetDialogFragment {

    private SequenceStep sequenceStep1,sequenceStep2,sequenceStep3,sequenceStep4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.track_order_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        sequenceStep1=(SequenceStep) view.findViewById(R.id.first);
        sequenceStep2=(SequenceStep) view.findViewById(R.id.second);
        sequenceStep3=(SequenceStep) view.findViewById(R.id.third);
        sequenceStep4=(SequenceStep) view.findViewById(R.id.forth);
        sequenceStep3.setActive(true);
    }
}
