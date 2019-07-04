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

}
