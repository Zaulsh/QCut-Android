package com.qcut.customer.fragment;


import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.qcut.customer.R;
import com.qcut.customer.activity.MainActivity;
import com.qcut.customer.model.User;
import com.qcut.customer.utils.AppUtils;
import com.qcut.customer.utils.FireManager;
import com.qcut.customer.utils.SharedPrefManager;
import com.volobot.stringchooser.StringChooser;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    MainActivity mainActivity;

    Boolean isShowEdit = false;
    int editSelectIndex = 0;

    private LinearLayout llt_profile_view, llt_profile_edit, llt_logout, llt_profile_name, llt_profile_password, llt_cancel, llt_save;
    private ImageView imgEditName, userProfileImage;
    private TextView txtTitle, userName, userEmail, userCity, imgEditLocation, imgEditPassword;
    private StringChooser locationChooser;

    private GoogleSignInClient mGoogleSignInClient;

    public ProfileFragment(MainActivity activity) {
        mainActivity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        llt_profile_view = view.findViewById(R.id.llt_profile_view);
        llt_profile_edit = view.findViewById(R.id.llt_edit_profile);
        llt_logout = view.findViewById(R.id.llt_logout);
        llt_profile_name = view.findViewById(R.id.llt_edit_name);
        llt_profile_password = view.findViewById(R.id.llt_edit_password);
        llt_cancel = view.findViewById(R.id.llt_cacel);
        llt_save = view.findViewById(R.id.llt_save);
        userProfileImage = view.findViewById(R.id.user_profile_image);

//        imgEditName = view.findViewById(R.id.img_edit_name);
        imgEditPassword = view.findViewById(R.id.img_edit_password);
        imgEditLocation = view.findViewById(R.id.img_edit_location);

        txtTitle = view.findViewById(R.id.txt_title);
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);
        userCity = view.findViewById(R.id.user_city);
        locationChooser = view.findViewById(R.id.location_string_choose);

        List<String> strings = new ArrayList<>();
        strings.add("Doublin 11");
        strings.add("Doublin 12");
        strings.add("Doublin 13");
        strings.add("Doublin 14");
        strings.add("Doublin 15");

        locationChooser.setStrings(strings);

        initUIView();
        initUIEvent();
        initUIData();
        return view;
    }

    private void initUIData() {
        final String userID = AppUtils.preferences.getString(AppUtils.USER_ID, null);
        if (!StringUtils.isEmpty(userID)) {
            FireManager.getDataFromFirebase("Customers/" + userID, new FireManager.getInfoCallback() {
                @Override
                public void onGetDataCallback(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        userName.setText(user.name);
                        userEmail.setText(user.email);
                        userCity.setText(user.city);
                        if (user.registeredInApp) {
                            imgEditPassword.setVisibility(View.VISIBLE);
                        }
//                        Glide.with(mainActivity).load(user.photo).into(userProfileImage);
                    }
                }

                @Override
                public void notFound() {

                }
            });

        }
    }

    private void initUIView() {
        if (!isShowEdit) {
            llt_profile_view.setVisibility(View.VISIBLE);
            llt_profile_edit.setVisibility(View.GONE);
            mainActivity.bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            llt_profile_view.setVisibility(View.GONE);
            llt_profile_edit.setVisibility(View.VISIBLE);
            if (editSelectIndex == 0) {
                txtTitle.setText("Name");
                llt_profile_name.setVisibility(View.VISIBLE);
                llt_profile_password.setVisibility(View.GONE);
                locationChooser.setVisibility(View.GONE);
            } else if (editSelectIndex == 1) {
                txtTitle.setText("Password");
                llt_profile_name.setVisibility(View.GONE);
                llt_profile_password.setVisibility(View.VISIBLE);
                locationChooser.setVisibility(View.GONE);
            } else if (editSelectIndex == 2){
                txtTitle.setText("Location");
                llt_profile_name.setVisibility(View.GONE);
                llt_profile_password.setVisibility(View.GONE);
                locationChooser.setVisibility(View.VISIBLE);
            }
            mainActivity.bottomNavigationView.setVisibility(View.GONE);
        }



    }

    private void initUIEvent() {
//        imgEditName.setOnClickListener(this);
        imgEditPassword.setOnClickListener(this);
        imgEditLocation.setOnClickListener(this);
        llt_logout.setOnClickListener(this);
        llt_cancel.setOnClickListener(this);
        llt_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llt_save) {
            String userId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
            if (!StringUtils.isEmpty(userId)) {
                locationChooser.getSelectedString();
                Map<String, Object> location = new HashMap<>();
                location.put("city", locationChooser.getSelectedString());
                FireManager.updateDataToFirebase(location, "Customers/" + userId, new FireManager.updateInfoCallback() {
                    @Override
                    public void onSetDataCallback(Map<String, Object> params) {
                        initUIData();
                    }
                });
            }
            isShowEdit = false;
            initUIView();
        } else if ( id == R.id.llt_cacel) {
            isShowEdit = false;
            initUIView();
        }
//        else if (id == R.id.img_edit_name) {
//            isShowEdit = true;
//            editSelectIndex = 0;
//            initUIView();
//        }
        else if (id == R.id.img_edit_password) {
            isShowEdit = true;
            editSelectIndex = 1;
            initUIView();
        } else if (id == R.id.img_edit_location) {
            isShowEdit = true;
            editSelectIndex = 2;
            initUIView();
        } else {
            String userId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
            FirebaseAuth.getInstance().signOut();
            FireManager.removeFirebaseToken(userId, mainActivity);

//            SharedPrefManager sharedPrefManager = new SharedPrefManager(mainActivity);
//            if (sharedPrefManager.getStringSharedPref("type").equals("google")){
//                mGoogleSignInClient.signOut().addOnCompleteListener(mainActivity,
//                        new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                mainActivity.onGoLoginActivity();
//                            }
//                        });
//            } else if (sharedPrefManager.getStringSharedPref("type").equals("facebook")) {
//                LoginManager.getInstance().logOut();
//                mainActivity.onGoLoginActivity();
//            }
            AppUtils.preferences.edit().putBoolean(AppUtils.IS_LOGGED_IN, false).apply();
            AppUtils.preferences.edit().putString(AppUtils.USER_ID, null).apply();
            AppUtils.preferences.edit().putString(AppUtils.USER_DISPLAY_NAME, null).apply();
            AppUtils.preferences.edit().putString(AppUtils.USER_EMAIL, null).apply();
            mainActivity.onGoLoginActivity();
        }
    }
}
