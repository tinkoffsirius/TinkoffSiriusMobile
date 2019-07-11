package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CREATED BY ANIKINKIRILL
 */

public class FinishActivityBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // UI
    private TextView finishActivity;
    private String meetingId;
    private static ArrayList<Object> finishedActivities=new ArrayList<>();
    private static boolean flag;

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
                flag=true;
                getFinishedActivities();
                removeActivity();
                break;
            }
        }
    }

    public static void removeActivity(){
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                int number=-1;
                while(iterator.hasNext() && flag){
                    number++;
                    DataSnapshot next=iterator.next();
                    if(next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])){
                        flag=false;
                        Iterable<DataSnapshot> activities=next.child("activities").getChildren();
                        Iterator<DataSnapshot> activitiesIterator=activities.iterator();
                        if(activitiesIterator.hasNext()){
                            DataSnapshot ds=activitiesIterator.next();
                            final DatabaseReference dbr=FirebaseDatabase.getInstance().getReference().child(Constants.SOLUTION).child(Constants.AGENTS).child(number+"").child("activities").child(ds.getKey());
                            dbr.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    finishedActivities.add(dataSnapshot.getValue());
                                    DatabaseReference finished=FirebaseDatabase.getInstance().getReference().child(date()).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("finished_activities");
                                    finished.setValue(finishedActivities);
                                    dbr.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                            Log.e("For remove",dbr.toString());
                            dbr.removeValue();
                            databaseReference.removeEventListener(this);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public static String date(){
        String date="";
        Time time=new Time(Time.getCurrentTimezone());
        time.setToNow();
        if(time.monthDay<10){
            date+="0"+time.monthDay+"_";
        }else{
            date+=time.monthDay+"_";
        }
        if((time.month+1)<10){
            date+="0"+(time.month+1)+"_";
        }else{
            date+=(time.month+1)+"_";
        }
        date+=time.year;
        return date;
    }

    public static void getFinishedActivities(){
        try{
            finishedActivities=new ArrayList<>();
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child(date()).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("finished_activities");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> iterable=dataSnapshot.getChildren();
                    Iterator<DataSnapshot> iterator=iterable.iterator();
                    while(iterator.hasNext()){
                        finishedActivities.add(iterator.next().getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }catch(Exception e){
            Log.e("Loading finished","failed"+e.toString());
        }
    }
}
