package com.anikinkirill.tinkoffsiriusmobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class SignUpActivity extends Activity {

    EditText et;
    EditText pw;
    EditText pw2;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        et=(EditText) findViewById(R.id.et);
        pw=(EditText) findViewById(R.id.pw);
        pw2=(EditText) findViewById(R.id.pw2);
    }

    public void su(View v){
        if(new File(getCacheDir()+"user").exists()){
            Toast.makeText(this,"Sorry, you are already signed up. Please sign in",Toast.LENGTH_LONG).show();
        }else{
            try{
                if(pw.getText().toString().equals(pw2.getText().toString())) {
                    FileOutputStream fos = new FileOutputStream(getCacheDir().toString() + "user");
                    String s = "{\n  \"login\": \"" + et.getText().toString() + "\"\n  \"password\": \"" + pw.getText().toString() + "\"\n}";
                    fos.write(s.getBytes());
                    fos.close();
                }else{
                    Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                Toast.makeText(this,e+"",Toast.LENGTH_LONG).show();
            }
        }
    }

}
