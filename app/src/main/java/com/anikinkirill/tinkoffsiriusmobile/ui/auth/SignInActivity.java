package com.anikinkirill.tinkoffsiriusmobile.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;

import java.io.File;
import java.io.FileInputStream;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ARTEM
 *
 * Activity where user can sign in to his account
 */

public class SignInActivity extends DaggerAppCompatActivity {

    // Injections
    @Inject
    ViewModelProviderFactory providerFactory;

    // UI
    private EditText userLogin;
    private EditText userPassword;

    // Vars
    private SignInViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        init();
        initViewModel();
    }

    private void init(){
        userLogin = findViewById(R.id.userLogin);
        userPassword = findViewById(R.id.userPassword);
    }

    private void initViewModel(){
        viewModel = ViewModelProviders.of(this, providerFactory).get(SignInViewModel.class);
    }

    public void signIn(View v){
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
                String s2 = "{\n  \"login\": \"" + userLogin.getText().toString() + "\"\n  \"password\": \"" + userPassword.getText().toString() + "\"\n}";
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
