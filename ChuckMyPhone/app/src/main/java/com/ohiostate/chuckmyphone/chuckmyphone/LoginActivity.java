package com.ohiostate.chuckmyphone.chuckmyphone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();

    private static boolean loginFromNewUserScreen;

    private boolean actionPending;

    private Button newUserButton;
    private Button loginButton;

    private EditText passwordEditText;
    private EditText emailEditText;

    private TextView forgotPasswordTextView;

    private Toast loggingInToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "onCreate() called");

        actionPending = false;

        loggingInToast = Toast.makeText(this.getApplicationContext(), "Logging in, please wait...", Toast.LENGTH_SHORT);

        if (SharedPreferencesHelper.hasSharedData(getApplicationContext())) {
            attemptLogin(SharedPreferencesHelper.getEmail(getApplicationContext()),
                    SharedPreferencesHelper.getPassword(getApplicationContext()));
        }

        initializeViews();
    }

    //connect all views to view instances
    private void initializeViews() {
        newUserButton = (Button) findViewById(R.id.login_new_user_button);
        newUserButton.setOnClickListener(this);

        forgotPasswordTextView = (TextView) findViewById(R.id.login_forgot_password_textview);
        forgotPasswordTextView.setOnClickListener(this);

        loginButton = (Button) findViewById(R.id.login_login_button);
        loginButton.setOnClickListener(this);

        passwordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        emailEditText = (EditText) findViewById(R.id.login_email_edit_text);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        if (!actionPending) {
            switch (v.getId()) {
                case R.id.login_login_button:
                    attemptLogin(emailEditText.getText().toString(), passwordEditText.getText().toString());
                    break;
                case R.id.login_forgot_password_textview:
                    intent = new Intent(getApplication(), ForgotPasswordActivity.class);
                    startActivity(intent);

                    break;
                default: //new user button case
                    intent = new Intent(getApplication(), NewUserActivity.class);
                    startActivity(intent);
                    break;
            }
        } else {
            Toast.makeText(this.getApplicationContext(), "Loading your previous request, please wait", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean attemptLogin(String email, String password) {
        boolean loginSuccessful = false;
        if (MiscHelperMethods.isNetworkAvailable(this)) {
            if (!email.equals("")) {
                if (!password.equals("")) {
                    actionPending = true;
                    if (!loginFromNewUserScreen) {
                        loggingInToast.show();
                    }
                    boolean firebaseWasLoaded = FirebaseHelper.getInstance().login(email, password, this);
                    if (!firebaseWasLoaded) {
                        actionPending = false;
                        Toast.makeText(this.getApplicationContext(), "App is still loading, please try to login again in a second", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this.getApplicationContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this.getApplicationContext(), "Please enter your username", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this.getApplicationContext(), "You have no internet, please try again when you get internet", Toast.LENGTH_LONG).show();
        }
        return loginSuccessful;
    }

    //called by firebase when login is successfully performed. Don't call from anywhere else
    void onSuccessfulLogin(String email, String password, String userID) {
        loggingInToast.cancel();
        if (CurrentUser.getInstance().getUsername().equals("USERNAME NOT ASSIGNED")) {
            CurrentUser.getInstance().assignUsername(FirebaseHelper.getInstance().getUsername(userID));
        }

        CurrentUser.getInstance().loadUserBadgeData();

        if(!SharedPreferencesHelper.hasSharedData(getApplicationContext())){
            SharedPreferencesHelper.createSharedPreferencesData(getApplicationContext(),
                    email, password, CurrentUser.getInstance().getUsername());
        } else {
            SharedPreferencesHelper.setSharedPreferencesData(getApplicationContext(),
                    email, password, CurrentUser.getInstance().getUsername());
        }

        CurrentUser.getInstance().loadUserSettings(SharedPreferencesHelper.getTutorialMessages(getApplicationContext()),
                SharedPreferencesHelper.getSoundEnabled(getApplicationContext()), SharedPreferencesHelper.getBadgeNotifications(getApplicationContext()),
                SharedPreferencesHelper.getGoofySoundEnabled(getApplicationContext()));

        CurrentUser.getInstance().loadUserLocation(SharedPreferencesHelper.getLatitude(getApplicationContext()),
                SharedPreferencesHelper.getLongitude(getApplicationContext()));

        actionPending = false;

        this.startActivity(new Intent(this.getApplication(), MainActivity.class));
        finish();
    }

    //called by firebase when login is not successfully performed. Don't call from anywhere else
    void onUnsuccessfulLogin(String error) {
        loggingInToast.cancel();
        SharedPreferencesHelper.clearSharedData(getApplicationContext());
        Toast.makeText(this.getApplicationContext(), "Login Unsuccessful: " + error, Toast.LENGTH_LONG).show();
        actionPending = false;
    }

    public static void setLoginFromNewUserScreen(boolean value) {
        loginFromNewUserScreen = value;
    }
}