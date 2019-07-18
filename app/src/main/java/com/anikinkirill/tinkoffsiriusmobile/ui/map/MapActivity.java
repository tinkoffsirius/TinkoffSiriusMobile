package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.services.SenderService;
import com.anikinkirill.tinkoffsiriusmobile.ui.auth.AuthActivity;
import com.anikinkirill.tinkoffsiriusmobile.ui.historyMap.HistoryMapActivity;
import com.anikinkirill.tinkoffsiriusmobile.ui.profile.ProfileActivity;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ANIKINKIRILL
 */

public class MapActivity extends DaggerAppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "MapActivity";

    // Injections
    @Inject
    ViewModelProviderFactory providerFactory;

    // UI
    private GoogleMap googleMap;
    private TextView textLate;

    // Vars
    private FusedLocationProviderClient fusedLocation;
    private MapViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            FileOutputStream fos = new FileOutputStream(getCacheDir() + "/logged");
            fos.write("in".getBytes());
            fos.flush();
            fos.close();
        }catch(Exception e){
            Log.e(TAG,e+"");
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        LateChecker lc=new LateChecker();
        lc.start();

        textLate=(TextView) findViewById(R.id.textLate);
        textLate.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);

        FloatingActionButton historyMap = findViewById(R.id.historyMap);
        historyMap.setOnClickListener(this);

        FloatingActionButton profile = findViewById(R.id.profile);
        profile.setOnClickListener(this);
    }

    private void initViewModel(){
        viewModel = ViewModelProviders.of(this, providerFactory).get(MapViewModel.class);
        viewModel.context=getApplicationContext();
        viewModel.theme=getColorTheme();
    }

    public String getColorTheme(){
        String colorTheme="light";
        try{
            FileInputStream fis=new FileInputStream(getCacheDir().toString());
        }catch(Exception e){}
        return colorTheme;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        this.googleMap = googleMap;

        setMapStyle();

        viewModel.getRoute(googleMap);

        MapViewModel.Other other = new MapViewModel.Other(this);
        other.start();

        viewModel.showStartCoordinates();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

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


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.logout:{
                try {
                    FileOutputStream fos = new FileOutputStream(getCacheDir() + "/logged");
                    fos.write("out".getBytes());
                    fos.flush();
                    fos.close();
                }catch(Exception e){
                    Log.e(TAG,e+"");
                }
                Intent intent = new Intent(this, SenderService.class);
                stopService(intent);

                FirebaseAuth.getInstance().signOut();

                Intent authIntent = new Intent(this, AuthActivity.class);
                startActivity(authIntent);
                finish();
                break;
            }
            case R.id.historyMap:{
                Intent historyMapIntent = new Intent(this, HistoryMapActivity.class);
                startActivity(historyMapIntent);
                break;
            }
            case R.id.profile:{
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
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

    private class LateChecker extends Thread{
        boolean result=false;
        String endTime="";
        @Override
        public void run() {
            while(true) {
                try{
                    FileInputStream fis=new FileInputStream(getCacheDir()+"/logged");
                    byte[] b=new byte[fis.available()];
                    fis.read(b);
                    fis.close();
                    String r=new String(b);
                    if(r.length()==2){
                        try {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                                    Iterator<DataSnapshot> iterator = iterable.iterator();
                                    while (iterator.hasNext()) {
                                        DataSnapshot next = iterator.next();
                                        if (next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                                            Iterable<DataSnapshot> activities = next.child("activities").getChildren();
                                            Iterator<DataSnapshot> activitiesIterator = activities.iterator();
                                            if (activitiesIterator.hasNext()) {
                                                DataSnapshot a = activitiesIterator.next();
                                                DataSnapshot activity = a.child("dueTimeSeconds");
                                                Double lat = Double.parseDouble(a.child("coordinates").child("latitude").getValue() + "");
                                                Double lon = Double.parseDouble(a.child("coordinates").child("longitude").getValue() + "");
                                                String time = (Integer.parseInt(activity.getValue().toString() + "")) / 3600 + ":" + (Integer.parseInt(activity.getValue() + "")) % 3600 / 60;
                                                Log.e("End time", time);
                                                endTime = time;
                                                fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Location> task) {
                                                        double lat2 = task.getResult().getLatitude();
                                                        double lon2 = task.getResult().getLongitude();
                                                        double res = calculateDistanse(lat, lat2, lon, lon2);
                                                        double lateTime = res / 6.0 * 60;
                                                        Log.e("Time minutes", lateTime + "");
                                                        Date d = new Date();
                                                        double minutes = d.getMinutes() + lateTime;
                                                        double hours = minutes / 60;
                                                        minutes %= 60;
                                                        String arrivalTime = (int) (d.getHours() + hours) + ":" + (int) (minutes);
                                                        Log.e("Arrival time", arrivalTime + "");
                                                        if (Integer.parseInt(arrivalTime.split(":")[0]) < Integer.parseInt(time.split(":")[0])) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    textLate.setVisibility(View.GONE);
                                                                }
                                                            });
                                                        } else if (Integer.parseInt(arrivalTime.split(":")[0]) == Integer.parseInt(time.split(":")[0])) {
                                                            if (Integer.parseInt(arrivalTime.split(":")[1]) < Integer.parseInt(time.split(":")[1])) {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        textLate.setVisibility(View.GONE);
                                                                    }
                                                                });
                                                            } else {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        textLate.setVisibility(View.VISIBLE);
                                                                    }
                                                                });
                                                            }
                                                        } else {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    textLate.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                            try {
                                sleep(2000);
                            } catch (Exception e) {
                                Log.e(TAG, e + "");
                            }
                        }catch(Exception e){
                            Log.e(TAG,e+"");
                        }
                    }
                }catch(Exception e){
                    Log.e(TAG,e+"");
                }
            }
        }
    }

    public double calculateDistanse(double lat1,double lat2,double lon1,double lon2){
        lat1*=3.1415926/180.00;
        lat2*=3.1415926/180.00;
        lon1*=3.1415926/180.00;
        lon2*=3.1415926/180.00;
        double dlon=Math.abs(lon2 - lon1);
        double dlat=Math.abs(lat2 - lat1);
        double r = 6371;
        double a = Math.sin(dlat/2)*Math.sin(dlat/2) + Math.cos(lat1)*Math.cos(lat2)*Math.sin(dlon/2)*Math.sin(dlon/2);
        double c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a) );
        return r*c;
    }
}
