package com.qcut.customer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.qcut.customer.R;
import com.qcut.customer.adapter.RegisterActivity;
import com.qcut.customer.model.User;
import com.qcut.customer.utils.AppUtils;
import com.qcut.customer.utils.FireManager;
import com.qcut.customer.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private TextInputLayout tll_email, tll_password;
    private TextInputEditText txt_email, txt_password;

    private LinearLayout llt_signin, llt_facebook, llt_google;

    FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_FACE_IN = 9000;

    private CallbackManager callbackmanager;

    private boolean isLocation = false;

    private LocationManager mLocationManager;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            if (isLocation) {
                return;
            }
            isLocation = true;
            AppUtils.gLat = location.getLatitude();
            AppUtils.gLon = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //
        }

        @Override
        public void onProviderEnabled(String provider) {
            //
        }

        @Override
        public void onProviderDisabled(String provider) {
            //
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppUtils.initUIActivity(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        FacebookSdk.sdkInitialize(this);

        mAuth = FirebaseAuth.getInstance();

        onCheckAllPermission();
    }

    private void onCheckAllPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 100);
            return;
        }

        initUIView();
        initUIEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onCheckAllPermission();
            } else {
                Toast.makeText(this, "All permitions are not setted.", Toast.LENGTH_LONG).show();

                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void initUIView() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0
                , 0, mLocationListener);


        tll_email = findViewById(R.id.tll_login_email);
        tll_password = findViewById(R.id.tll_login_password);
        txt_email = findViewById(R.id.txt_login_email);
        txt_password = findViewById(R.id.txt_login_password);
        llt_signin = findViewById(R.id.llt_signin);
        llt_facebook = findViewById(R.id.llt_facebook);
        llt_google = findViewById(R.id.llt_google);
        ((TextView)findViewById(R.id.sign_up)).setText(Html.fromHtml("<font color='black'>New User?</font> <font color='blue'><b>Sign Up</b></font>"));
    }

    private void initUIEvent() {
        txt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tll_email.setHelperText("");
            }
        });

        txt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tll_password.setHelperText("");
            }
        });

        llt_signin.setOnClickListener(this);
        llt_google.setOnClickListener(this);
        llt_facebook.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llt_signin:
                loginWithEmailPassword();
                break;
            case R.id.llt_facebook:
                loginWithFacebook();
                break;
            case R.id.llt_google:
                loginWithGoogle();
                break;
        }
    }

    private void loginWithEmailPassword() {
        final String strEmail = txt_email.getText().toString();
        final String strPassword = txt_password.getText().toString();
        if (!AppUtils.validate(strEmail)) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (strPassword.length() < 6) {
            Toast.makeText(this, "Password must be over 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog dialog = AppUtils.onShowProgressDialog(this, "Connecting", false);



        mAuth.signInWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser().isEmailVerified()) {
                                AppUtils.onDismissProgressDialog(dialog);
                                FireManager.getDataFromFirebase("Customers/" + FireManager.getUid(), new FireManager.getInfoCallback() {
                                    @Override
                                    public void onGetDataCallback(DataSnapshot snapshot) {
                                        AppUtils.gUser = snapshot.getValue(User.class);
                                        new SharedPrefManager(LoginActivity.this);
                                        SharedPrefManager.setStringSharedPref("type", "normal");
                                        AppUtils.preferences.edit()
                                                .putString(AppUtils.USER_DISPLAY_NAME,
                                                        AppUtils.gUser.name).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_EMAIL,
                                                AppUtils.gUser.email).apply();
                                        AppUtils.preferences.edit().putBoolean(AppUtils.IS_LOGGED_IN, true).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_ID,
                                                AppUtils.gUser.id).apply();
                                        AppUtils.showOtherActivity(LoginActivity.this, MainActivity.class, 0);

                                        FireManager.saveNewFirebaseToken(AppUtils.gUser.id, LoginActivity.this);

                                    }

                                    @Override
                                    public void notFound() {
                                        Toast.makeText(LoginActivity.this, "Invalid Account.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                AppUtils.onDismissProgressDialog(dialog);
                                Toast.makeText(LoginActivity.this, "Check your mail box to verify", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            AppUtils.onDismissProgressDialog(dialog);
                            Toast.makeText(LoginActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_FACE_IN) {
            callbackmanager.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                final GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google Account has a problem.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        final ProgressDialog dialog = AppUtils.onShowProgressDialog(this, "Connecting..", false );
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Result", "signInWithCredential:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    AppUtils.onDismissProgressDialog(dialog);
                    Toast.makeText(LoginActivity.this, "Sign in Failed..", Toast.LENGTH_SHORT).show();
                } else {
                    AppUtils.onDismissProgressDialog(dialog);
                    Toast.makeText(LoginActivity.this, "Sign in Success..", Toast.LENGTH_SHORT).show();
                    AppUtils.gUser.id = FireManager.getUid();
                    AppUtils.gUser.email = account.getEmail();
                    AppUtils.gUser.name = account.getDisplayName();
                    AppUtils.gUser.googleID = account.getId();
                    AppUtils.gUser.photo = account.getPhotoUrl().toString();

                    final Map<String, Object> params = new HashMap<>();
                    params.put("name", AppUtils.gUser.name);
                    params.put("photo", AppUtils.gUser.photo);
                    params.put("name", AppUtils.gUser.name);

                    FireManager.getDataFromFirebase("Customers/" + AppUtils.gUser.id, new FireManager.getInfoCallback() {
                        @Override
                        public void onGetDataCallback(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                FireManager.updateDataToFirebase(params, "Customers/" + AppUtils.gUser.id, new FireManager.updateInfoCallback() {
                                    @Override
                                    public void onSetDataCallback(Map<String, Object> params) {
                                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        AppUtils.preferences.edit()
                                                .putString(AppUtils.USER_DISPLAY_NAME,
                                                        AppUtils.gUser.name).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_EMAIL,
                                                AppUtils.gUser.email).apply();
                                        AppUtils.preferences.edit().putBoolean(AppUtils.IS_LOGGED_IN, true).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_ID,
                                                AppUtils.gUser.id).apply();
                                    }
                                });
                            } else {
                                final Map<String, Object> params = new HashMap<>();
                                params.put("id", AppUtils.gUser.id);
                                params.put("email", AppUtils.gUser.email);
                                params.put("name", AppUtils.gUser.name);
                                params.put("googleID", AppUtils.gUser.googleID);
                                params.put("photo", AppUtils.gUser.photo);
                                params.put("registeredInApp", false);
                                FireManager.saveDataToFirebase(params, "Customers/" + AppUtils.gUser.id, new FireManager.saveObjectCallback() {
                                    @Override
                                    public void onSetDataCallback(Map<String, Object> params) {
                                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        AppUtils.preferences.edit()
                                                .putString(AppUtils.USER_DISPLAY_NAME,
                                                        AppUtils.gUser.name).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_EMAIL,
                                                AppUtils.gUser.email).apply();
                                        AppUtils.preferences.edit().putBoolean(AppUtils.IS_LOGGED_IN, true).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_ID,
                                                AppUtils.gUser.id).apply();

                                    }
                                });
                            }

                            FireManager.saveNewFirebaseToken(AppUtils.gUser.id, LoginActivity.this);

                        }

                        @Override
                        public void notFound() {

                        }
                    });


                    new SharedPrefManager(LoginActivity.this);
                    SharedPrefManager.setStringSharedPref("type", "google");
                    AppUtils.showOtherActivity(LoginActivity.this, MainActivity.class, -1);

                }
            }
        });
    }

    private void loginWithFacebook() {
        callbackmanager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","user_photos","public_profile"));
        LoginManager.getInstance().registerCallback(callbackmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LoginActivity.this, "success", Toast.LENGTH_SHORT).show();
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void firebaseAuthWithFacebook(final AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Facebook Login error", Toast.LENGTH_SHORT).show();
                } else {
                    GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {

                                String jsonresult = String.valueOf(object);
                                System.out.println("JSON Result"+jsonresult);

                                AppUtils.gUser.id = FireManager.getUid();
                                AppUtils.gUser.email = object.getString("email");
                                AppUtils.gUser.name = object.getString("name");
                                AppUtils.gUser.photo = "";

                                Map<String, String> params = new HashMap<>();
                                params.put("id", AppUtils.gUser.id);
                                params.put("email", AppUtils.gUser.email);
                                params.put("name", AppUtils.gUser.name);
                                params.put("photo", AppUtils.gUser.photo);

                                FireManager.saveDataToFirebase(params, "Customers/" + AppUtils.gUser.id, new FireManager.saveInfoCallback() {
                                    @Override
                                    public void onSetDataCallback(Map<String, String> params) {
                                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        AppUtils.preferences.edit()
                                                .putString(AppUtils.USER_DISPLAY_NAME,
                                                        AppUtils.gUser.name).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_EMAIL,
                                                AppUtils.gUser.email).apply();
                                        AppUtils.preferences.edit().putBoolean(AppUtils.IS_LOGGED_IN, true).apply();
                                        AppUtils.preferences.edit().putString(AppUtils.USER_ID,
                                                AppUtils.gUser.id).apply();
                                    }
                                });
                                new SharedPrefManager(LoginActivity.this);
                                SharedPrefManager.setStringSharedPref("type", "facebook");
                                AppUtils.showOtherActivity(LoginActivity.this, MainActivity.class, -1);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).executeAsync();
                }
            }
        });
    }

    public void onClickRegister(View view) {
        AppUtils.showOtherActivity(this, RegisterActivity.class, -1);
    }
}
