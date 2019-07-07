package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.util.Map;

import javax.inject.Inject;

import static androidx.core.content.ContextCompat.checkSelfPermission;

/**
 * CREATED BY ANIKINKIRILL
 */
public class MapViewModel extends ViewModel {

    private static final String TAG = "MapViewModel";

    // Vars
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 12000;
    private String date = "";
    private Context context;
    private FusedLocationProviderClient fusedLocation;
    private ArrayList<Map<String,String>> forSending = new ArrayList<>();
    ArrayList<LatLng> arrayList = new ArrayList<>();
    public String login;
    private GoogleMap googleMap;

    @Inject
    public MapViewModel(){
        setDate();
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

    public void startUserLocationsRunnable(Context context, FusedLocationProviderClient fusedLocation, GoogleMap googleMap) {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");

        this.context = context;
        this.fusedLocation = fusedLocation;
        this.googleMap = googleMap;

        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    public void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
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

    public void setDate(){
        Log.d(TAG, "setDate: called");
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

}
