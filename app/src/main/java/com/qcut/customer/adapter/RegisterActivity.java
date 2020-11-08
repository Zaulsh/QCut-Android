package com.qcut.customer.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.qcut.customer.R;
import com.qcut.customer.activity.LoginActivity;
import com.qcut.customer.activity.MainActivity;
import com.qcut.customer.model.User;
import com.qcut.customer.utils.AppUtils;
import com.qcut.customer.utils.FireManager;
import com.qcut.customer.utils.SharedPrefManager;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout tll_email, tll_password, tll_name;
    private TextInputEditText txt_email, txt_password, txt_name;

    private LinearLayout llt_signin;
    private TextView loginNow;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        AppUtils.initUIActivity(this);
        initUIView();
        initUIEvent();
    }

    private void initUIView() {

        tll_email = findViewById(R.id.tll_login_email);
        tll_password = findViewById(R.id.tll_login_password);
        txt_email = findViewById(R.id.txt_login_email);
        txt_password = findViewById(R.id.txt_login_password);
        llt_signin = findViewById(R.id.llt_signin);
        tll_name = findViewById(R.id.tll_name);
        txt_name = findViewById(R.id.txt_name);
        loginNow = findViewById(R.id.login_now);
        loginNow.setText(
                Html.fromHtml("<font color='black'>Existing User?</font>" +
                        " <font color='blue'><b>Sign In</b></font>"));
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
        loginNow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llt_signin:
                reigsterWithEmailPassword();
                break;
            case R.id.login_now:
                AppUtils.showOtherActivity(RegisterActivity.this, LoginActivity.class, 0);
                break;
        }
    }

    private void reigsterWithEmailPassword() {
        final String strName = txt_name.getText().toString();
        final String strEmail = txt_email.getText().toString();
        final String strPassword = txt_password.getText().toString();
        if (StringUtils.isEmpty(strName)) {
            Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AppUtils.validate(strEmail)) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (strPassword.length() < 6) {
            Toast.makeText(this, "Password must be over 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog dialog = AppUtils.onShowProgressDialog(this, "Connecting", false);

        mAuth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                AppUtils.onDismissProgressDialog(dialog);
                                                AppUtils.gUser.id = FireManager.getUid();
                                                AppUtils.gUser.email = strEmail;
                                                AppUtils.gUser.photo = "";
                                                AppUtils.gUser.name = strName;

                                                Map<String, String> params = new HashMap<>();
                                                params.put("id", AppUtils.gUser.id);
                                                params.put("email", AppUtils.gUser.email);
//                                                params.put("photo", AppUtils.gUser.photo);
                                                params.put("name", AppUtils.gUser.name);



                                                FireManager.saveDataToFirebase(params, "Customers/" + AppUtils.gUser.id, new FireManager.saveInfoCallback() {
                                                    @Override
                                                    public void onSetDataCallback(Map<String, String> params) {
                                                        Toast.makeText(RegisterActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                                        AppUtils.showOtherActivity(RegisterActivity.this, LoginActivity.class, 0);

                                                    }
                                                });
                                            }
                                        }
                                    });


                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Failed - "+task.getException().getMessage(), Toast.LENGTH_LONG).show();

                            /*AuthCredential credential = EmailAuthProvider.getCredential(strEmail, strPassword);
                            Task<AuthResult> authResultTask = mAuth.signInWithCredential(credential);
                            authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    AppUtils.onDismissProgressDialog(dialog);
                                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                }
                            });
                            authResultTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    AppUtils.onDismissProgressDialog(dialog);
                                    Toast.makeText(RegisterActivity.this, "Registration Failed - "+e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });*/

                        }
                    }
                });

    }
}
