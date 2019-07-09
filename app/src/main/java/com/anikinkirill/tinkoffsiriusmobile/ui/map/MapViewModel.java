package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.models.Activity;
import com.anikinkirill.tinkoffsiriusmobile.models.Agent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.List;
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
                ArrayList<Map<String,String>> arrayList = (ArrayList)dataSnapshot.child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.HISTORY).getValue();
                ArrayList<LatLng> forSetting=new ArrayList<>();
                if(arrayList != null){
                    for(Map<String,String> map : arrayList){
                        forSetting.add(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)),Double.parseDouble(map.get(Constants.LONGITTUDE))));
                    }
                    PolylineOptions polylineOptions=new PolylineOptions().width(15).color(Color.BLUE);
                    polylineOptions.addAll(forSetting);
                    googleMap.addPolyline(polylineOptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public static void showStartCoordinates(){
        final BitmapDescriptor blueMarker= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Map<String, String> map = (HashMap) dataSnapshot.child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.START_COORDINATES).getValue();
                    LatLng position = new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE)));
                    googleMap.addMarker(new MarkerOptions().position(position).title("Start coordinates")).setIcon(blueMarker);
                }catch (Exception e){
                    Log.d(TAG, "onDataChange: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    static class Other extends Thread {

        Context context;

        public Other(Context context){
            this.context = context;
        }

        @Override
        public void run(){
            DatabaseReference dbr=FirebaseDatabase.getInstance().getReference();
            dbr.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> iterable=dataSnapshot.child(date).child(Constants.USERS).getChildren();
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
                        getCurrentUserActivities(context);
                        for(String name:others){
                            if(name!=FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                                DataSnapshot userValues = dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.HISTORY);
                                String login=dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.LOGIN).getValue().toString();
                                ArrayList<Map<String, String>> arrayList = (ArrayList) userValues.getValue();
                                Map<String, String> map = arrayList.get(arrayList.size() - 1);
                                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE)))).title(login);
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

    private static void getCurrentUserActivities(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.CONSTANTS, Context.MODE_PRIVATE);
        final String currentUserId = sharedPreferences.getString(Constants.CURRENT_USER_ID, "");

        final BitmapDescriptor yellowMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: currentUserId " + currentUserId);

                for(DataSnapshot agent : dataSnapshot.getChildren()){
                    if(agent.child("agent").child("id").getValue().toString().equals(currentUserId)){
                        for(DataSnapshot activity : agent.child("activities").getChildren()){
                            double latitude = Double.parseDouble(activity.child("coordinates").child("latitude").getValue().toString());
                            double longitude = Double.parseDouble(activity.child("coordinates").child("longitude").getValue().toString());
                            LatLng location = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions().position(location).title(activity.child("id").getValue().toString()).icon(yellowMarker);
                            googleMap.addMarker(markerOptions);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
