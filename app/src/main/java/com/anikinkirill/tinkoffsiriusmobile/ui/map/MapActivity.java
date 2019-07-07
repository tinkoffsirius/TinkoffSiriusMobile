package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ANIKINKIRILL
 */

public class MapActivity extends DaggerAppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private GoogleMap googleMap;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private FusedLocationProviderClient fusedLocation;
    ArrayList<LatLng> arrayList = new ArrayList<>();
    public String login;
    private String date = "";
    private ArrayList<Map<String,String>> forSending=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationsRunnable(); // update user locations every 'LOCATION_UPDATE_INTERVAL'
    }

    @Override
    protected void onPause() {
        stopLocationUpdates(); // stop updating user locations
        super.onPause();
    }

    private void startUserLocationsRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void retrieveUserLocations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location currentLocation = task.getResult();
                passValuesToFirebase(currentLocation);
            }
        });
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

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

    public void passValuesToFirebase(Location location){
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        LatLng latLng = new LatLng(lat,lon);
        arrayList.add(latLng);
        LatLng position = new LatLng(lat,lon);
        if(arrayList.size()==1) {
            googleMap.addMarker(new MarkerOptions().position(position).title("Start position"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
            login= FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.e(TAG,login);
            Map<String,String> map=new HashMap<>();
            map.put("latitude",lat+"");
            map.put("longitude",lon+"");
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
            if(!login.equals("")) {
                Log.e(TAG,login);
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
                databaseReference.child("06_07_2019").child("users").child(login).child("history").setValue(forSending);
            }
        }
        PolylineOptions polylineOptions = new PolylineOptions().addAll(arrayList).color(Color.RED).width(15);
        googleMap.addPolyline(polylineOptions);
    }
}
