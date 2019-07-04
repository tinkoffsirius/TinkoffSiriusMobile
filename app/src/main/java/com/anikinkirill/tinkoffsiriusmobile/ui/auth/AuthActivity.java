package com.anikinkirill.tinkoffsiriusmobile.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.lifecycle.ViewModelProviders;

import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ARTEM
 *
 * Activity where user can sign in to his account
 */

public class AuthActivity extends DaggerAppCompatActivity implements View.OnClickListener {

    // Injections
    @Inject
    ViewModelProviderFactory providerFactory;

    // UI
    private EditText userLogin;
    private EditText userPassword;
    private Button authUserButton;
    private RelativeLayout relativeLayout;

    // Vars
    private AuthViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        init();
        initViewModel();
    }

    private void init(){
        relativeLayout = findViewById(R.id.signIn_relativeLayout);
        userLogin = findViewById(R.id.userLogin);
        userPassword = findViewById(R.id.userPassword);
        authUserButton = findViewById(R.id.signInButton);

        authUserButton.setOnClickListener(this);
    }

    private void initViewModel(){
        viewModel = ViewModelProviders.of(this, providerFactory).get(AuthViewModel.class);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signInButton:{
                viewModel.signInUser(userLogin.getText().toString().trim(), userPassword.getText().toString().trim(), relativeLayout);
                hideSoftKeyboard(this, view);
                break;
            }
        }
    }

    private void hideSoftKeyboard(Context context, View view){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
