package com.anikinkirill.tinkoffsiriusmobile.ui.auth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.anikinkirill.tinkoffsiriusmobile.R;
import com.anikinkirill.tinkoffsiriusmobile.services.SenderService;
import com.anikinkirill.tinkoffsiriusmobile.ui.map.MapActivity;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * CREATED BY ARTEM
 *
 * Activity where user can sign in to his account
 */

public class AuthActivity extends DaggerAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AuthActivity";

    // Injections
    @Inject
    ViewModelProviderFactory providerFactory;

    // UI
    private EditText userLogin;
    private EditText userPassword;
    private Button authUserButton;
    private RelativeLayout relativeLayout;
    private Switch switchDark;
    private TextView appName;

    // Vars
    private AuthViewModel viewModel;
    private static final int REQUEST_LOCATION_PERMISSIONS_CODE = 1;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_signin);

        if(checkMapServices()){
            checkDevicePermissions();
        }

        init();
        initViewModel();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, SenderService.class);
        stopService(intent);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent mapIntent = new Intent(this, MapActivity.class);
            startActivity(mapIntent);
        }

        Unblocker u=new Unblocker();
        u.start();
    }

    private void init(){
        relativeLayout = findViewById(R.id.signIn_relativeLayout);
        userLogin = findViewById(R.id.userLogin);
        userPassword = findViewById(R.id.userPassword);
        authUserButton = findViewById(R.id.signInButton);
        switchDark=(Switch) findViewById(R.id.switchDark);
        appName=(TextView) findViewById(R.id.appName);

        if(getColorTheme().equals(Constants.DARK_COLOR_THEME)){
            relativeLayout.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
            userLogin.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
            userLogin.setHintTextColor(Color.parseColor(Constants.DARK_HINT_COLOR));
            userLogin.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            userLogin.setHighlightColor(getResources().getColor(R.color.colorAccent));
            userPassword.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
            userPassword.setHintTextColor(Color.parseColor(Constants.DARK_HINT_COLOR));
            userPassword.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            userPassword.setHighlightColor(getResources().getColor(R.color.colorAccent));
            switchDark.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            authUserButton.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
            authUserButton.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            appName.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
            saveColorTheme(Constants.DARK_COLOR_THEME);
            switchDark.setChecked(true);
        }else{
            relativeLayout.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
            userLogin.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
            userLogin.setHintTextColor(Color.parseColor(Constants.LIGHT_HINT_COLOR));
            userLogin.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            userLogin.setHighlightColor(getResources().getColor(R.color.colorAccent));
            userPassword.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
            userPassword.setHintTextColor(Color.parseColor(Constants.LIGHT_HINT_COLOR));
            userPassword.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            userPassword.setHighlightColor(getResources().getColor(R.color.colorAccent));
            switchDark.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            authUserButton.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
            authUserButton.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            appName.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
            saveColorTheme(Constants.LIGHT_COLOR_THEME);
            switchDark.setChecked(false);
        }

        switchDark.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                relativeLayout.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
                userLogin.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
                userLogin.setHintTextColor(Color.parseColor(Constants.DARK_HINT_COLOR));
                userLogin.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
                userLogin.setHighlightColor(getResources().getColor(R.color.colorAccent));
                userPassword.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
                userPassword.setHintTextColor(Color.parseColor(Constants.DARK_HINT_COLOR));
                userPassword.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
                userPassword.setHighlightColor(getResources().getColor(R.color.colorAccent));
                switchDark.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
                authUserButton.setBackgroundColor(Color.parseColor(Constants.DARK_BACK_COLOR));
                authUserButton.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
                appName.setTextColor(Color.parseColor(Constants.DARK_TEXT_COLOR));
                saveColorTheme(Constants.DARK_COLOR_THEME);
            }else{
                relativeLayout.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
                userLogin.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
                userLogin.setHintTextColor(Color.parseColor(Constants.LIGHT_HINT_COLOR));
                userLogin.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
                userLogin.setHighlightColor(getResources().getColor(R.color.colorAccent));
                userPassword.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
                userPassword.setHintTextColor(Color.parseColor(Constants.LIGHT_HINT_COLOR));
                userPassword.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
                userPassword.setHighlightColor(getResources().getColor(R.color.colorAccent));
                switchDark.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
                authUserButton.setBackgroundColor(Color.parseColor(Constants.LIGHT_BACK_COLOR));
                authUserButton.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
                appName.setTextColor(Color.parseColor(Constants.LIGHT_TEXT_COLOR));
                saveColorTheme(Constants.LIGHT_COLOR_THEME);
            }
        });

        authUserButton.setOnClickListener(this);
    }

    private void initViewModel(){
        viewModel = ViewModelProviders.of(this, providerFactory).get(AuthViewModel.class);
    }

    private void saveColorTheme(String colorTheme){
        try{
            FileOutputStream fos=new FileOutputStream(getCacheDir().toString()+"theme");
            fos.write(colorTheme.getBytes());
            fos.close();
        }catch(Exception e){}
    }

    private String getColorTheme(){
        String theme="light";
        try{
            FileInputStream fis=new FileInputStream(getCacheDir().toString()+"theme");
            byte[] b=new byte[fis.available()];
            fis.read(b);
            theme=new String(b);
        }catch(Exception e){}
        return theme;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signInButton:{
                if(!userPassword.getText().toString().equals("") && !userLogin.getText().toString().equals("")) {
                    viewModel.signInUser(userLogin.getText().toString().trim(), userPassword.getText().toString().trim(), relativeLayout);

                    try {
                        Button signIn = (Button) findViewById(R.id.signInButton);
                        signIn.setClickable(false);
                        FileOutputStream fos = new FileOutputStream(getCacheDir().toString() + "/unblock");
                        fos.write("blocked".getBytes());
                        fos.close();
                    }catch(Exception e){
                        Log.e(TAG,e+"");
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.CONSTANTS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.CURRENT_USER_ID, userLogin.getText().toString().trim());
                    editor.apply();

                    hideSoftKeyboard(this, view);
                }
                break;
            }
        }
    }

    private void hideSoftKeyboard(Context context, View view){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void checkDevicePermissions(){
        Log.d(TAG, "checkDevicePermissions: called");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Locations Permissions")
                    .setMessage("This permissions are needed for getting your location on the map")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(AuthActivity.this, permissions, REQUEST_LOCATION_PERMISSIONS_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
        }else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_LOCATION_PERMISSIONS_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onRequestPermissionsResult: permissions granted");
            }else{
                Log.d(TAG, "onRequestPermissionsResult: permissions denied");
            }
        }
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        if(getColorTheme().equals(Constants.LIGHT_COLOR_THEME)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.custom_alertdialog_light);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                }
            });
            builder.show();
        }else{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.custom_alertdialog_dark);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                }
            });
            AlertDialog alertDialog = builder.show();
            alertDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.back));
        }
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            Log.d(TAG, "isMapsEnabled: false");
            return false;
        }
        Log.d(TAG, "isMapsEnabled: true");
        return true;
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AuthActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AuthActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PERMISSIONS_REQUEST_ENABLE_GPS){
            checkDevicePermissions();
        }
    }

    private class Unblocker extends Thread{
        @Override
        public void run(){
            while(true) {
                try {
                    sleep(200);
                    FileInputStream fis = new FileInputStream(getCacheDir().toString() + "/unblock");
                    byte[] b = new byte[fis.available()];
                    fis.read(b);
                    fis.close();
                    String r = new String(b);
                    Log.e(TAG,r);
                    if (r.equals("unblocked")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button signIn = (Button) findViewById(R.id.signInButton);
                                signIn.setClickable(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, e + "");
                }
            }
        }
    }
}
