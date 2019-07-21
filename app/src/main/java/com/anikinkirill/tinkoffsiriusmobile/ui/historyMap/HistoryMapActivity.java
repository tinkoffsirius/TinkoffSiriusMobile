package com.anikinkirill.tinkoffsiriusmobile.ui.historyMap;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ANIKINKIRILL
 */

public class HistoryMapActivity extends DaggerAppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap googleMap;
    private static final String TAG="HistoryMap";
    private boolean loggedin=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_historymap);
        init();
        LogoutCheck lc=new LogoutCheck();
        lc.start();
    }

    private void init(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton backToMainMap = findViewById(R.id.backToMainMap);
        backToMainMap.setOnClickListener(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap=googleMap;

        setMapStyle();

        getRoute();
        showStartCoordinates();
        drawFinishedMeetings();

        googleMap.setMyLocationEnabled(true);

        FusedLocationProviderClient fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    LatLng latLng = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                    googleMap.animateCamera(cameraUpdate);
                }
            }
        });
    }

    public void getRoute(){
        class RouteGetter extends Thread{
            @Override
            public void run(){
                while(loggedin) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                                try {
                                    ArrayList<Map<String, String>> arrayList = (ArrayList) dataSnapshot.child(date()).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.HISTORY).getValue();
                                    ArrayList<LatLng> forSetting = new ArrayList<>();
                                    if (arrayList != null) {
                                        for (Map<String, String> map : arrayList) {
                                            forSetting.add(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE))));
                                        }
                                        PolylineOptions polylineOptions = new PolylineOptions().width(15).color(Color.BLUE);
                                        polylineOptions.addAll(forSetting);
                                        googleMap.addPolyline(polylineOptions);
                                    }
                                }catch(Exception e){
                                    Log.e(TAG,e+"");
                                }
                            }
                            databaseReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                    try{
                        sleep(20000);
                    }catch(Exception e){
                        Log.e(TAG,e+"");
                    }
                }
            }
        }
        RouteGetter rg=new RouteGetter();
        rg.start();
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

    public void showStartCoordinates(){
        final BitmapDescriptor blueMarker= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Map<String, String> map = (HashMap) dataSnapshot.child(date()).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.START_COORDINATES).getValue();
                    LatLng position = new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE)));
                    googleMap.addMarker(new MarkerOptions().position(position).title("Start coordinates")).setIcon(blueMarker);
                }catch (Exception e){
                    Log.d(TAG, "onDataChange: " + e.getMessage());
                }
                databaseReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void drawFinishedMeetings(){
        class FinishedDrawer extends Thread{
            @Override
            public void run(){
                while(loggedin){
                    final BitmapDescriptor greenMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                    DatabaseReference dbr=FirebaseDatabase.getInstance().getReference().child(date()).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("finished_activities");
                    dbr.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> iterable=dataSnapshot.getChildren();
                            Iterator<DataSnapshot> iterator=iterable.iterator();
                            while(iterator.hasNext()){
                                DataSnapshot activity = iterator.next();
                                Map<String,Double> map=(HashMap<String,Double>) activity.child("coordinates").getValue();
                                String time=activity.child("time").getValue()+"";
                                Log.e(TAG,map.get("latitude")+"");
                                LatLng position=new LatLng(Double.parseDouble(map.get("latitude")+""),Double.parseDouble(map.get("longitude")+""));
                                googleMap.addMarker(new MarkerOptions().position(position).icon(greenMarker)).setTitle("Finished at: "+time.replace("_",":"));
                            }
                            dbr.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                    try{
                        sleep(10000);
                    }catch(Exception e){
                        Log.e(TAG,e+"");
                    }
                }
            }
        }
        FinishedDrawer fd=new FinishedDrawer();
        fd.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backToMainMap:{
                finish();
                break;
            }
        }
    }

    private void setMapStyle(){
        try{
            FileInputStream fis=new FileInputStream(getCacheDir().toString()+"theme");
            byte[] b=new byte[fis.available()];
            fis.read(b);
            fis.close();
            String theme=new String(b);
            if(theme.equals(Constants.DARK_COLOR_THEME)){
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(),R.raw.style));
            }
        }catch(Exception e){}
    }

    private class LogoutCheck extends Thread{
        @Override
        public void run(){
            while(true){
                try{
                    FileInputStream fis=new FileInputStream("/data/user/0/com.anikinkirill.tinkoffsiriusmobile/cache/logged");
                    byte[] b=new byte[fis.available()];
                    fis.read(b);
                    fis.close();
                    String s=new String(b);
                    Log.e(TAG,s);
                    if(!s.equals("out")){
                        loggedin=true;
                    }else{
                        loggedin=false;
                    }
                    sleep(2000);
                }catch (Exception e){
                    Log.e(TAG,e+"");
                }
            }
        }
    }
}
