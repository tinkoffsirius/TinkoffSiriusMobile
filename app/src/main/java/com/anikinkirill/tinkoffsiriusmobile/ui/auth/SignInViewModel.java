package com.anikinkirill.tinkoffsiriusmobile.ui.auth;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

/**
 * CREATED BY ANIKINKIRILL
 */

public class SignInViewModel extends ViewModel {

    private static final String TAG = "SignInViewModel";

    @Inject
    public SignInViewModel(){
        Log.d(TAG, "SignInViewModel: viewmodel is working...");
    }

}
