package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * CREATED BY ANIKINKIRILL
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    ArrayList<LatLng> al = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                LatLng latLng = new LatLng(lat,lon);
                al.add(latLng);
                LatLng position = new LatLng(lat,lon);
                if(al.size()==1) {
                    googleMap.addMarker(new MarkerOptions().position(position).title("Start position"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
                }
                PolylineOptions polylineOptions = new PolylineOptions().addAll(al).color(Color.RED).width(15);
                googleMap.addPolyline(polylineOptions);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        });
    }
}
