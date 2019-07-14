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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.services.SenderService;
import com.anikinkirill.tinkoffsiriusmobile.ui.auth.AuthActivity;
import com.anikinkirill.tinkoffsiriusmobile.ui.historyMap.HistoryMapActivity;
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

import java.util.ArrayList;
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

    // Vars
    private FusedLocationProviderClient fusedLocation;
    private MapViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        FloatingActionButton logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);

        FloatingActionButton historyMap = findViewById(R.id.historyMap);
        historyMap.setOnClickListener(this);
    }

    private void initViewModel(){
        viewModel = ViewModelProviders.of(this, providerFactory).get(MapViewModel.class);
        viewModel.context=getApplicationContext();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        this.googleMap = googleMap;

        viewModel.getRoute(googleMap);
        viewModel.drawRouteToMeeting();

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
        }
    }
}
