package com.anikinkirill.tinkoffsiriusmobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class SignInActivity extends Activity {
    EditText et;
    EditText pw;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        et=(EditText) findViewById(R.id.et);
        pw=(EditText) findViewById(R.id.pw);
    }

    public void si(View v){
        try{
            if(!new File(getCacheDir().toString()+"user").exists()){
                Toast.makeText(this, "You have not signed up yet", Toast.LENGTH_SHORT).show();
            }else {
                FileInputStream fis = new FileInputStream(getCacheDir().toString() + "user");
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                String s = new String(b);
                fis.close();
                String s2 = "{\n  \"login\": \"" + et.getText().toString() + "\"\n  \"password\": \"" + pw.getText().toString() + "\"\n}";
                if (s.equals(s2)) {
                    Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Login or password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        }catch(Exception e){
            Toast.makeText(this,e+"",Toast.LENGTH_SHORT).show();
        }
    }

}
