package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.services.SenderService;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ANIKINKIRILL
 */

public class MapActivity extends DaggerAppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    // Injections
    @Inject
    ViewModelProviderFactory providerFactory;

    // UI
    private GoogleMap googleMap;

    // Vars
    private FusedLocationProviderClient fusedLocation;
    private MapViewModel viewModel;
    private String date="";
    private ArrayList<String> others=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDate();
        startService();
        setContentView(R.layout.activity_map);
        initViewModel();
        initFusedLocationClient();
        init();
    }

    private void startService(){
        Intent intent = new Intent(this, SenderService.class);
        startService(intent);
    }

    private void initFusedLocationClient(){
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
    }

    private void init(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initViewModel(){
        viewModel = ViewModelProviders.of(this, providerFactory).get(MapViewModel.class);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        this.googleMap = googleMap;

        getRoute();
        Other other=new Other();
        other.start();
        showStartCoordinates();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        googleMap.setMyLocationEnabled(true);

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

    public void getRoute(){
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

    public void showStartCoordinates(){
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

    class Other extends Thread{
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
                        getRoute();
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
