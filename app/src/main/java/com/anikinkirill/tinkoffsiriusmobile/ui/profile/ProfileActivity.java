package com.anikinkirill.tinkoffsiriusmobile.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.ui.map.MapActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //UI
    LinearLayout layout;
    TextView profile;
    TextView id;
    TextView name;
    TextView coordinates;
    CheckBox car;
    EditText startlat;
    EditText startlon;
    Button save;
    FloatingActionButton back;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
    }

    public void initViews(){
        layout=(LinearLayout) findViewById(R.id.layout);
        profile=(TextView) findViewById(R.id.profile);
        id=(TextView) findViewById(R.id.id);
        save=(Button) findViewById(R.id.save);
        id.setText(id.getText().toString()+" "+initId());
        name=(TextView) findViewById(R.id.name);
        NameInitializer ni=new NameInitializer(name);
        ni.execute();
        coordinates=(TextView) findViewById(R.id.coordinates);
        car=(CheckBox) findViewById(R.id.car);
        CarInitializer ci=new CarInitializer(car);
        ci.execute();
        startlat=(EditText) findViewById(R.id.startlat);
        startlon=(EditText) findViewById(R.id.startlon);
        back=(FloatingActionButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent=new Intent(getApplicationContext(), MapActivity.class);
                startActivity(mapIntent);
            }
        });
        if(colorTheme().equals(Constants.LIGHT_COLOR_THEME)){
            layout.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
            profile.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            coordinates.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            id.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            name.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            car.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            startlat.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            startlon.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            startlat.setHintTextColor(Color.parseColor(Constants.LIGHT_HINT_COLOR));
            startlon.setHintTextColor(Color.parseColor(Constants.LIGHT_HINT_COLOR));
            save.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            save.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
        }else{
            layout.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
            profile.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            coordinates.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            id.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            name.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            car.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            startlat.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            startlon.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            startlat.setHintTextColor(Color.parseColor(Constants.DARK_HINT_COLOR));
            startlon.setHintTextColor(Color.parseColor(Constants.DARK_HINT_COLOR));
            save.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            save.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
        }
    }

    public String colorTheme(){
        String theme="";
        try{
            FileInputStream fis=new FileInputStream(getCacheDir().toString()+"theme");
            byte[] b=new byte[fis.available()];
            fis.read(b);
            fis.close();
            theme=new String(b);
        }catch(Exception e){
            Log.e("Failed to init theme",e+"");
        }
        return theme;
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

    public String initId(){
        String id=FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
        return id;
    }

    class NameInitializer extends AsyncTask<Void,Void,Void>{
        TextView textView;
        String name="";
        public NameInitializer(TextView textView){
            this.textView=textView;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection huc = (HttpURLConnection) new URL("https://tinkoffsiriusmobile.firebaseio.com/agents/" + FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0] + ".json").openConnection();
                huc.setRequestMethod("GET");
                huc.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                name = br.readLine();
                br.close();
                Log.e("Name", name);
            } catch (Exception e) {
                Log.e("Failed to init name", e + "");
                name=e+"";
            }
            return null;
        }
        @Override
        public void onPostExecute(Void v){
            super.onPostExecute(v);
            textView.setText("Name: "+name);
        }
    }

    class CarInitializer extends AsyncTask<Void,Void,Void>{
        CheckBox checkBox;
        boolean car=false;
        public CarInitializer(CheckBox checkBox){
            this.checkBox=checkBox;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection huc = (HttpURLConnection) new URL("https://tinkoffsiriusmobile.firebaseio.com/"+ date() + "/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile/car.json").openConnection();
                huc.setRequestMethod("GET");
                huc.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                String res=br.readLine();
                if(res.length()<7){
                    car=true;
                }
                br.close();
            } catch (Exception e) {
                Log.e("Failed to init name", e + "");
            }
            return null;
        }
        @Override
        public void onPostExecute(Void v){
            super.onPostExecute(v);
            checkBox.setChecked(car);
        }
    }

    public void save(View v){
        Saver s=new Saver();
        s.start();
    }

    class Saver extends Thread{
        @Override
        public void run() {
            DatabaseReference dbr= FirebaseDatabase.getInstance().getReference().child(date()).child(Constants.USERS).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile");
            Map<String,String> map=new HashMap<>();
            map.put("start_latitude",startlat.getText().toString());
            map.put("start_longitude",startlon.getText().toString());
            map.put("car",car.isChecked()+"");
            dbr.setValue(map);
        }
    }

    //TODO start coordinates

}
