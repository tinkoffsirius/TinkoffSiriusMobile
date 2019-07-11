package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * CREATED BY ANIKINKIRILL
 */

public class FinishActivityBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // UI
    private TextView finishActivity;
    private String meetingId;

    public FinishActivityBottomSheetDialogFragment(String meetingId) {
        this.meetingId = meetingId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheetdialogfragment_finishactivity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view){
        finishActivity = view.findViewById(R.id.finishActivity);

        finishActivity.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.finishActivity:{

                // HERE YOU PUT OUT LOGIC

                break;
            }
        }
    }
}
