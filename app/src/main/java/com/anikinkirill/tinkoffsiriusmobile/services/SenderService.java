package com.anikinkirill.tinkoffsiriusmobile.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anikinkirill.tinkoffsiriusmobile.ui.map.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SenderService extends Service {

    private static final String TAG = "SenderService";

    private String date = "";
    private FusedLocationProviderClient fusedLocation;
    private ArrayList<Map<String,String>> forSending = new ArrayList<>();
    ArrayList<LatLng> arrayList = new ArrayList<>();
    public String login;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG,"Service started");
        setDate();
        Log.i(TAG,"Date set: "+date);
        login = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i(TAG,"Date set: "+login);
        checkNumber();
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        Log.i(TAG,"Initialized fusedLocation");
        Sender sender = new Sender();
        sender.start();
        Log.i(TAG,"Sender started");
    }

    class Sender extends Thread{
        @SuppressLint("MissingPermission")
        @Override
        public void run(){
            while(true) {
                fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = task.getResult();
                            passValuesToFirebase(currentLocation);
                            Log.i(TAG, "Sent successfully");
                        } else {
                            Log.i(TAG, "Problem: " + task.getException() + "");
                        }
                    }
                });
                try {
                    sleep(MapViewModel.LOCATION_UPDATE_INTERVAL);
                } catch (Exception e) {
                }
            }
        }
    }

    public void passValuesToFirebase(Location location){
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        LatLng latLng = new LatLng(lat,lon);
        arrayList.add(latLng);
        LatLng position = new LatLng(lat,lon);
        if(arrayList.size()==1) {
            Map<String,String> map=new HashMap<>();
            map.put("latitude",lat+"");
            map.put("longitude",lon+"");
            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
            if(!login.equals("")) {
                databaseReference.child(date).child("users").child(login).child("start_coordinates").setValue(map);
            }else{
                databaseReference.child(date).child("users").child("user3").child("start_coordinates").setValue(map);
            }
        }else{
            Time time = new Time(Time.getCurrentTimezone());
            time.setToNow();
            Map<String,String> map = new HashMap<>();
            map.put("latitude",lat + "");
            map.put("longitude",lon + "");
            map.put("order", (forSending.size() + 1) + "");
            String resultTime = time.hour+":"+time.minute+":"+time.second;
            map.put("time", resultTime);
            forSending.add(map);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            if(!date.equals("")) {
                databaseReference.child(date).child("users").child(login).child("history").setValue(forSending);
            }else{
                databaseReference.child("07_07_2019").child("users").child(login).child("history").setValue(forSending);
            }
        }
    }

    public void setDate(){
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
    }

    public void checkNumber(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Map<String,String>> arrayList = (ArrayList)dataSnapshot.child(date).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("history").getValue();
                if(arrayList != null){
                    forSending=arrayList;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
