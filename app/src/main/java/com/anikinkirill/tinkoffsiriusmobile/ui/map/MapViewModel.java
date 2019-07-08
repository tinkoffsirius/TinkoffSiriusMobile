package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.graphics.Color;
import android.text.format.Time;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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

import javax.inject.Inject;

/**
 * CREATED BY ANIKINKIRILL
 */
public class MapViewModel extends ViewModel {

    private static final String TAG = "MapViewModel";

    // Vars
    public static final int LOCATION_UPDATE_INTERVAL = 12000;
    private static String date="";
    private static ArrayList<String> others=new ArrayList<>();
    private static GoogleMap googleMap;

    @Inject
    public MapViewModel(){
        setDate();
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

    public static void getRoute(final GoogleMap googleMap){
        MapViewModel.googleMap = googleMap;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Map<String,String>> arrayList = (ArrayList)dataSnapshot.child(date).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("history").getValue();
                ArrayList<LatLng> forSetting=new ArrayList<>();
                if(arrayList != null){
                    for(Map<String,String> map : arrayList){
                        forSetting.add(new LatLng(Double.parseDouble(map.get("latitude")),Double.parseDouble(map.get("longitude"))));
                    }
                    PolylineOptions polylineOptions=new PolylineOptions().width(15).color(Color.RED);
                    polylineOptions.addAll(forSetting);
                    googleMap.addPolyline(polylineOptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public static void showStartCoordinates(){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String,String> map=(HashMap)dataSnapshot.child(date).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("start_coordinates").getValue();
                LatLng position=new LatLng(Double.parseDouble(map.get("latitude")),Double.parseDouble(map.get("longitude")));
                googleMap.addMarker(new MarkerOptions().position(position).title("Start coordinates"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    static class Other extends Thread{
        @Override
        public void run(){
            DatabaseReference dbr=FirebaseDatabase.getInstance().getReference();
            dbr.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> iterable=dataSnapshot.child(date).child("users").getChildren();
                    Iterator iterator=iterable.iterator();
                    while(iterator.hasNext()){
                        DataSnapshot string=(DataSnapshot) iterator.next();
                        if(!others.contains(string.getKey())) {
                            if(!string.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                others.add(string.getKey());
                            }
                        }
                        googleMap.clear();
                        showStartCoordinates();
                        getRoute(googleMap);
                        for(String name:others){
                            if(name!=FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                                DataSnapshot userValues = dataSnapshot.child(date).child("users").child(name).child("history");
                                String login=dataSnapshot.child(date).child("users").child(name).child("login").getValue().toString();
                                ArrayList<Map<String, String>> arrayList = (ArrayList) userValues.getValue();
                                Map<String, String> map = arrayList.get(arrayList.size() - 1);
                                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(map.get("latitude")), Double.parseDouble(map.get("longitude")))).title(login);
                                googleMap.addMarker(markerOptions);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

}
