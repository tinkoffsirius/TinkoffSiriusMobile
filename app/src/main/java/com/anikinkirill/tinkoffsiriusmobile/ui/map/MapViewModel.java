package com.anikinkirill.tinkoffsiriusmobile.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
/*import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;*/

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private static boolean loggedin=true;
    private static String date="";
    private static ArrayList<String> others=new ArrayList<>();
    private static ArrayList<LatLng> meetings=new ArrayList<>();
    private static GoogleMap googleMap;
    static String theme;
    static Context context;
    static ArrayList<LatLng> coordinates = new ArrayList<>();
    //private static GeoApiContext geoApiContext;

    @Inject
    public MapViewModel(){
        if(date.equals("")) {
            setDate();
            LogoutCheck lc=new LogoutCheck();
            lc.start();
        }
        //Log.e(TAG,theme);
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
        class NextActivityGetter extends Thread{
            @Override
            public void run(){
                while(loggedin){
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                            Iterator<DataSnapshot> iterator = iterable.iterator();
                            while(iterator.hasNext()){
                                DataSnapshot next=iterator.next();
                                if(loggedin) {
                                    try {
                                        if (next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                                            Iterable<DataSnapshot> activities = next.child("activities").getChildren();
                                            Iterator<DataSnapshot> activitiesIterator = activities.iterator();
                                            if (activitiesIterator.hasNext()) {
                                                DataSnapshot activity = activitiesIterator.next();
                                                Map<String, String> map = new HashMap<>();
                                                map.put("latitude", activity.child("coordinates").child("latitude").getValue().toString());
                                                map.put("longitude", activity.child("coordinates").child("longitude").getValue().toString());
                                                LatLng markerPosition = new LatLng(Double.parseDouble(activity.child("coordinates").child("latitude").getValue().toString()), Double.parseDouble(activity.child("coordinates").child("longitude").getValue().toString()));
                                                DatabaseReference endCoordinates = FirebaseDatabase.getInstance().getReference().child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("end_coordinates");
                                                endCoordinates.setValue(map);
                                                googleMap.addMarker(new MarkerOptions().position(markerPosition).position(markerPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                            }
                                        }
                                    }catch(Exception e){
                                        Log.e(TAG,e+"");
                                    }
                                }
                            }
                            databaseReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                    drawRouteToMeeting();
                    try{
                        sleep(10000);
                    }catch(Exception e){
                        Log.e(TAG,e+"");
                    }
                }
            }
        }
        NextActivityGetter nag=new NextActivityGetter();
        nag.start();
    }

    public static void getRoute(final GoogleMap googleMap){
        class RouteGetter extends Thread{
            @Override
            public void run(){
               while(loggedin){
                   MapViewModel.googleMap = googleMap;
                   DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                   databaseReference.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(loggedin && FirebaseAuth.getInstance().getCurrentUser()!=null) {
                               ArrayList<Map<String, String>> arrayList = (ArrayList) dataSnapshot.child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.HISTORY).getValue();
                               ArrayList<LatLng> forSetting = new ArrayList<>();
                               if (arrayList != null) {
                                   for (Map<String, String> map : arrayList) {
                                       forSetting.add(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE))));
                                   }
                                   PolylineOptions polylineOptions = new PolylineOptions().width(15).color(Color.BLUE);
                                   polylineOptions.addAll(forSetting);
                                   googleMap.addPolyline(polylineOptions);
                               }
                           }
                           databaseReference.removeEventListener(this);
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {}
                   });
                   try{
                       sleep(60000);
                   }catch(Exception e){
                       Log.e(TAG,e+"");
                   }
               }
            }
        }
        RouteGetter rg=new RouteGetter();
        rg.start();
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
                    FileOutputStream fos=new FileOutputStream("/data/user/0/com.anikinkirill.tinkoffsiriusmobile/cache/startCoordinates");
                    fos.write((position.latitude+" "+position.longitude).getBytes());
                    fos.flush();
                    fos.close();
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

    static class Other extends Thread {

        Context context;

        public Other(Context context){
            this.context = context;
        }

        @Override
        public void run(){
            while(loggedin) {
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference();
                dbr.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> iterable = dataSnapshot.child(date).child(Constants.USERS).getChildren();
                        Iterator iterator = iterable.iterator();
                        while (iterator.hasNext()) {
                            DataSnapshot string = (DataSnapshot) iterator.next();
                            if (!others.contains(string.getKey())) {
                                if (!string.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
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
                            for (String name : others) {
                                if (name != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                                    DataSnapshot userValues = dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.HISTORY);
                                    String login = dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.LOGIN).getValue().toString();
                                    ArrayList<Map<String, String>> arrayList = (ArrayList) userValues.getValue();
                                    if (arrayList != null) {
                                        Map<String, String> map = arrayList.get(arrayList.size() - 1);
                                        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE)))).title(login).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                        googleMap.addMarker(markerOptions);
                                    }
                                }
                            }
                        }
                        dbr.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                try{
                    sleep(15000);
                }catch(Exception e){
                    Log.e(TAG,e+"");
                }
            }
        }
    }

    private static void getCurrentUserActivities(final Context context){

        /*if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder().apiKey(context.getString(R.string.apiKey)).build();
        }*/

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
                try {
                    String markerTitle = marker.getTitle();
                    if (markerTitle.contains("Id")) {
                        // this marker is a meeting
                        String meetingId = markerTitle.substring(markerTitle.indexOf(":") + 1).trim();
                        Log.d(TAG, "onMarkerClick: " + meetingId);
                        showFinishActivitySheet(meetingId, context, theme);
                        //calculateDirections(marker);
                    }
                }catch(Exception e){}
                return false;
            }
        });

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.CONSTANTS, Context.MODE_PRIVATE);
        final String currentUserId = sharedPreferences.getString(Constants.CURRENT_USER_ID, "");

        final BitmapDescriptor yellowMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


        class Refresher extends Thread{
            @Override
            public void run(){
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
                        databaseReference.removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                try {
                    sleep(15000);
                }catch(Exception e){
                    Log.e(TAG,e+"");
                }
            }
        }
        Refresher r=new Refresher();
        r.start();
    }

    private static String getActivityTime(int totalSeconds){
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void getLastActivity(){
        class LastActivityGetter extends Thread{
            @Override
            public void run(){
                while(loggedin){
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                            Iterator<DataSnapshot> iterator = iterable.iterator();
                            while(iterator.hasNext()){
                                DataSnapshot next=iterator.next();
                                if(FirebaseAuth.getInstance().getCurrentUser()!=null && next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])){
                                    Iterable<DataSnapshot> activities=next.child("activities").getChildren();
                                    Iterator<DataSnapshot> activitiesIterator=activities.iterator();
                                    DataSnapshot last=null;
                                    while(activitiesIterator.hasNext()){
                                        last=activitiesIterator.next();
                                        meetings.add(new LatLng(Double.parseDouble(last.child("coordinates").child("latitude").getValue().toString()),Double.parseDouble(last.child("coordinates").child("longitude").getValue().toString())));
                                    }
                                    try {
                                        LatLng markerPosition = new LatLng(Double.parseDouble(last.child("coordinates").child("latitude").getValue().toString()), Double.parseDouble(last.child("coordinates").child("longitude").getValue().toString()));
                                        googleMap.addMarker(new MarkerOptions().position(markerPosition).position(markerPosition));
                                    }catch (Exception e){}
                                }
                            }
                            databaseReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                    try{
                        sleep(15000);
                    }catch(Exception e){
                        Log.e(TAG,e+"");
                    }
                }
            }
        }
        LastActivityGetter lag=new LastActivityGetter();
        lag.start();
    }

    @SuppressLint("MissingPermission")
    public static void drawRouteToMeeting(){
        coordinates=new ArrayList<>();
        FusedLocationProviderClient fusedLocation= LocationServices.getFusedLocationProviderClient(context);
        fusedLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                coordinates.add(new LatLng(task.getResult().getLatitude(),task.getResult().getLongitude()));
                Log.e(TAG,"Current position: "+task.getResult().getLatitude()+" "+task.getResult().getLongitude());
                class Drawer extends Thread{
                    @Override
                    public void run(){
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
                        databaseReference.child(Constants.SOLUTION).child(Constants.AGENTS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                                Iterator<DataSnapshot> iterator = iterable.iterator();
                                while(iterator.hasNext()){
                                    DataSnapshot next=iterator.next();
                                    if(loggedin && FirebaseAuth.getInstance().getCurrentUser()!=null) {
                                        if (next.child("agent").child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                                            Iterable<DataSnapshot> activities = next.child("activities").getChildren();
                                            Iterator<DataSnapshot> activitiesIterator = activities.iterator();
                                            if (activitiesIterator.hasNext()) {
                                                try {
                                                    DataSnapshot activity = activitiesIterator.next();
                                                    Map<String, String> map = new HashMap<>();
                                                    map.put("latitude", activity.child("coordinates").child("latitude").getValue().toString());
                                                    map.put("longitude", activity.child("coordinates").child("longitude").getValue().toString());
                                                    LatLng position = new LatLng(Double.parseDouble(activity.child("coordinates").child("latitude").getValue().toString()), Double.parseDouble(activity.child("coordinates").child("longitude").getValue().toString()));
                                                    DatabaseReference endCoordinates = FirebaseDatabase.getInstance().getReference().child(date).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("end_coordinates");
                                                    endCoordinates.setValue(map);
                                                    coordinates.add(position);
                                                    Log.e(TAG, coordinates.toString());
                                                    PolylineOptions polylineOptions = new PolylineOptions().addAll(coordinates).width(15).color(Color.rgb(255, 128, 0));
                                                    googleMap.addPolyline(polylineOptions);
                                                } catch (Exception e) {
                                                }
                                            }
                                        }
                                    }
                                }
                                databaseReference.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    }
                }
                Drawer d=new Drawer();
                d.start();
            }
        });
    }

    private static void showFinishActivitySheet(String meetingId, Context context, String theme){
        FinishActivityBottomSheetDialogFragment dialogFragment = new FinishActivityBottomSheetDialogFragment(meetingId);
        dialogFragment.show(((DaggerAppCompatActivity) context).getSupportFragmentManager(), "showFragment");
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

    public static void drawAll(){
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference();
        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.child(date).child(Constants.USERS).getChildren();
                Iterator iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    DataSnapshot string = (DataSnapshot) iterator.next();
                    if (!others.contains(string.getKey())) {
                        if (!string.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
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
                    for (String name : others) {
                        if (name != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                            DataSnapshot userValues = dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.HISTORY);
                            String login = dataSnapshot.child(date).child(Constants.USERS).child(name).child(Constants.LOGIN).getValue().toString();
                            ArrayList<Map<String, String>> arrayList = (ArrayList) userValues.getValue();
                            if (arrayList != null) {
                                Map<String, String> map = arrayList.get(arrayList.size() - 1);
                                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(Double.parseDouble(map.get(Constants.LATITUDE)), Double.parseDouble(map.get(Constants.LONGITTUDE)))).title(login).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                googleMap.addMarker(markerOptions);
                            }
                        }
                    }
                }
                dbr.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /*private static void calculateDirections(Marker marker){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        43.4002391,
                        39.9667929
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    private static void addPolylinesToMap(final DirectionsResult result){
        Log.d(TAG, "addPolylinesToMap: called");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(Color.RED);
                    polyline.setClickable(true);

                }
            }
        });
    }*/

}
