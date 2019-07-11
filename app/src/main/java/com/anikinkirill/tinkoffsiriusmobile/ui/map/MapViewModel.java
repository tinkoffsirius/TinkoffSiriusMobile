package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
public class MapViewModel extends ViewModel {

    private static final String TAG = "MapViewModel";

    // Vars
    public static final int LOCATION_UPDATE_INTERVAL = 30000;
    private static String date="";
    private static ArrayList<String> others=new ArrayList<>();
    private static ArrayList<LatLng> meetings=new ArrayList<>();
    private static GoogleMap googleMap;
    static Context context;
    static ArrayList<LatLng> coordinates = new ArrayList<>();;

    @Inject
    public MapViewModel(){
        if(date.equals("")) {
            setDate();
        }
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

    public static void getNextActivity(){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while(iterator.hasNext()){
                    DataSnapshot next=iterator.next();
                    if(next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])){
                        Iterable<DataSnapshot> activities=next.child("activities").getChildren();
                        Iterator<DataSnapshot> activitiesIterator=activities.iterator();
                        DataSnapshot activity=activitiesIterator.next();
                        Map<String,String> map=new HashMap<>();
                        map.put("latitude",activity.child("coordinates").child("latitude").getValue().toString());
                        map.put("longitude",activity.child("coordinates").child("longitude").getValue().toString());
                        LatLng markerPosition=new LatLng(Double.parseDouble(activity.child("coordinates").child("latitude").getValue().toString()),Double.parseDouble(activity.child("coordinates").child("longitude").getValue().toString()));
                        DatabaseReference endCoordinates=FirebaseDatabase.getInstance().getReference().child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("end_coordinates");
                        endCoordinates.setValue(map);
                        googleMap.addMarker(new MarkerOptions().position(markerPosition).position(markerPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
                        getLastActivity();
                        getNextActivity();
                        drawRouteToMeeting();
                        for(String name:others){
                            if(name!=FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                                DataSnapshot userValues = dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.HISTORY);
                                String login=dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.LOGIN).getValue().toString();
                                ArrayList<Map<String, String>> arrayList = (ArrayList) userValues.getValue();
                                if(arrayList!=null) {
                                    Map<String, String> map = arrayList.get(arrayList.size() - 1);
                                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE)))).title(login).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                    googleMap.addMarker(markerOptions);
                                }
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    private static void getCurrentUserActivities(final Context context){

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String markerTitle = marker.getTitle();
                if(markerTitle.contains("Id")){
                    // this marker is a meeting
                    String meetingId = markerTitle.substring(markerTitle.indexOf(":") + 1).trim();
                    Log.d(TAG, "onMarkerClick: " + meetingId);
                    showFinishActivitySheet(meetingId, context);
                }
                return false;
            }
        });

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

                            int startTotalTime = Integer.parseInt(activity.child("readyTimeSeconds").getValue().toString());
                            int endTotalTime = Integer.parseInt(activity.child("dueTimeSeconds").getValue().toString());

                            LatLng location = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(location)
                                    .title("Id: " + activity.child("id").getValue().toString())
                                    .snippet("startTime: " + getActivityTime(startTotalTime) + "\n" +
                                             "endTime: " + getActivityTime(endTotalTime))
                                    .icon(yellowMarker);
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

    private static String getActivityTime(int totalSeconds){
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void getLastActivity(){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while(iterator.hasNext()){
                    DataSnapshot next=iterator.next();
                    if(next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])){
                       Iterable<DataSnapshot> activities=next.child("activities").getChildren();
                       Iterator<DataSnapshot> activitiesIterator=activities.iterator();
                       DataSnapshot last=null;
                       while(activitiesIterator.hasNext()){
                           last=activitiesIterator.next();
                           meetings.add(new LatLng(Double.parseDouble(last.child("coordinates").child("latitude").getValue().toString()),Double.parseDouble(last.child("coordinates").child("longitude").getValue().toString())));
                       }
                       /*PolylineOptions polylineOptions =new PolylineOptions().color(Color.YELLOW).width(15).addAll(meetings);
                       googleMap.addPolyline(polylineOptions);*/
                       LatLng markerPosition=new LatLng(Double.parseDouble(last.child("coordinates").child("latitude").getValue().toString()),Double.parseDouble(last.child("coordinates").child("longitude").getValue().toString()));
                       googleMap.addMarker(new MarkerOptions().position(markerPosition).position(markerPosition));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @SuppressLint("MissingPermission")
    public static void drawRouteToMeeting(){
        FusedLocationProviderClient fusedLocation= LocationServices.getFusedLocationProviderClient(context);
        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                coordinates.add(new LatLng(task.getResult().getLatitude(),task.getResult().getLongitude()));
            }
        });
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while(iterator.hasNext()){
                    DataSnapshot next=iterator.next();
                    if(next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])){
                        Iterable<DataSnapshot> activities=next.child("activities").getChildren();
                        Iterator<DataSnapshot> activitiesIterator=activities.iterator();
                        DataSnapshot activity=activitiesIterator.next();
                        Map<String,String> map=new HashMap<>();
                        map.put("latitude",activity.child("coordinates").child("latitude").getValue().toString());
                        map.put("longitude",activity.child("coordinates").child("longitude").getValue().toString());
                        LatLng position=new LatLng(Double.parseDouble(activity.child("coordinates").child("latitude").getValue().toString()),Double.parseDouble(activity.child("coordinates").child("longitude").getValue().toString()));
                        DatabaseReference endCoordinates=FirebaseDatabase.getInstance().getReference().child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("end_coordinates");
                        endCoordinates.setValue(map);
                        coordinates.add(position);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        PolylineOptions polylineOptions=new PolylineOptions().addAll(coordinates).width(15).color(Color.rgb(255,128,0));
        googleMap.addPolyline(polylineOptions);
    }

    private static void showFinishActivitySheet(String meetingId, Context context){
        FinishActivityBottomSheetDialogFragment dialogFragment = new FinishActivityBottomSheetDialogFragment(meetingId);
        dialogFragment.show(((DaggerAppCompatActivity) context).getSupportFragmentManager(), "showFragment");
    }

}
