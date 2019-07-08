package com.anikinkirill.tinkoffsiriusmobile.ui.auth;

import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.anikinkirill.tinkoffsiriusmobile.services.SenderService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

/**
 * CREATED BY ANIKINKIRILL
 */

public class AuthViewModel extends ViewModel {

    private static final String TAG = "AuthViewModel";

    // Vars
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private RelativeLayout view;
    private String date = "";
    private String login;

    @Inject
    public AuthViewModel(){
        setDate();
        Log.d(TAG, "AuthViewModel: viewmodel is working...");
    }

    /**
     * After user pressed 'signIn' button in {@link AuthActivity}
     * using his login from {@link AuthActivity} this method build
     * email and create user in Firebase Authentication.
     * If user already created in DB he just become logged in in the Application
     * @param login     his login in {@link AuthActivity}
     * @param password  his password
     * @param view      view
     */

    public void signInUser(String login, final String password, final RelativeLayout view){
        this.view = view;
        this.login = login;
        final String email = login + "@mail.ru";
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: user created account");
                    goToMainActivity();
                }else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                    Log.d(TAG, "onComplete: user's been already created");
                    authUser(email, password);
                }else if(!task.isSuccessful()){
                    Log.d(TAG, "onComplete: error while signing in : " + task.getException().getMessage());
                    Snackbar.make(view, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * If user's been already created in DB
     * then check credentials
     * If credentials correct -> user logged in successfully go to MainActivity
     * If credentials incorrect -> user received a message with 'Login or password is incorrect'
     * @param email     user email
     * @param password  user password
     */

    private void authUser(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: user logged in successfully");
                    Snackbar.make(view, "User has logged in", Snackbar.LENGTH_LONG).show();
                    goToMainActivity();
                }
                if(!task.isSuccessful()){
                    Log.d(TAG, "onComplete: user's credentials're incorrect");
                    Snackbar.make(view, "Login or password is incorrect", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * After user logged in successfully
     */

    private void goToMainActivity(){
        Log.d(TAG, "goToMainActivity: called");
        saveUserLogin(login);

        Intent intent = new Intent("android.intent.action.MapActivity");
        view.getContext().startActivity(intent);
    }

    public void saveUserLogin(String login){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(date)
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("login")
                .setValue(login);
    }

    public void setDate(){
        Log.d(TAG, "setDate: called");
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

}
