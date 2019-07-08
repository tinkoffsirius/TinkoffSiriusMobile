package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.Manifest;
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
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initViewModel();
        initFusedLocationClient();
        init();
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        googleMap.setMyLocationEnabled(true);

    }
}
