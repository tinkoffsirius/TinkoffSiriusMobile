package com.anikinkirill.tinkoffsiriusmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sn(View v){
        try {
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
        }catch(Exception e){
            Toast.makeText(this,e+"",Toast.LENGTH_SHORT).show();
        }
    }

    public void sp(View v){
        Intent i=new Intent(this,SignUpActivity.class);
        startActivity(i);
    }

}
