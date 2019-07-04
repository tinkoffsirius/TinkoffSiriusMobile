package com.anikinkirill.tinkoffsiriusmobile.ui.auth;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

/**
 * CREATED BY ANIKINKIRILL
 */

public class AuthViewModel extends ViewModel {

    private static final String TAG = "AuthViewModel";

    @Inject
    public AuthViewModel(){
        Log.d(TAG, "AuthViewModel: viewmodel is working...");
    }

}
