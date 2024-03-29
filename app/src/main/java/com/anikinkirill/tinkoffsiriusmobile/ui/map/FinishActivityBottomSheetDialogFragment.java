package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.graphics.Color;
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

import java.io.FileInputStream;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    public String cacheDir="/data/user/0/com.anikinkirill.tinkoffsiriusmobile/cache";

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

        if(getColorTheme().equals(Constants.DARK_COLOR_THEME)){
            finishActivity.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
            finishActivity.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
        }else{
            finishActivity.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
            finishActivity.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.finishActivity:{
                flag=true;
                Finisher f=new Finisher();
                f.start();
                break;
            }
        }
    }

    class Finisher extends Thread{
        @Override
        public void run(){
            getFinishedActivities();
            removeActivity();
        }
    }

    private void hideBottomSheetDialog(){
        dismiss();
    }

    public void removeActivity(){
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
                            DatabaseReference datref=FirebaseDatabase.getInstance().getReference().child(Constants.SOLUTION).child(Constants.AGENTS).child(number+"").child("activities").child(ds.getKey()).child("time");
                            datref.setValue(time());
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
        hideBottomSheetDialog();
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

    public String time(){
        String time="";
        Calendar c=GregorianCalendar.getInstance();
        Date t=c.getTime();
        if(t.getHours()<10){
            time+="0"+t.getHours()+"_";
        }else{
            time+=t.getHours()+"_";
        }
        if(t.getMinutes()<10){
            time+="0"+t.getMinutes();
        }else{
            time+=t.getMinutes();
        }
        return  time;
    }

    public String getColorTheme(){
        String theme="light";
        try{
            FileInputStream fis=new FileInputStream(cacheDir+"theme");
            byte[] b=new byte[fis.available()];
            fis.read(b);
            fis.close();
            theme=new String(b);
        }catch(Exception e){
            Log.e("Failed to get theme",e+"");
        }
        return theme;
    }
}
