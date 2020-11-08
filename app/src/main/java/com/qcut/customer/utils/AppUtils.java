package com.qcut.customer.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.qcut.customer.R;
import com.qcut.customer.model.User;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class AppUtils {

//    public static boolean isQueued = false;

    public static double gLat, gLon;

    public static User gUser = new User();

    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String USER_DISPLAY_NAME = "userName";
    public static final String USER_EMAIL = "userEmail";
    public static final String USER_ID = "userID";

    public static SharedPreferences preferences ;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    static public ProgressDialog onShowProgressDialog(final Context mActivity, final String message, boolean isCancelable) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.show();
        progressDialog.setCancelable(isCancelable);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    static public void onDismissProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static void showOtherActivity (Activity activity, Class<?> cls, int direction) {
        Intent myIntent = new Intent(activity, cls);
        ActivityOptions options;
        switch (direction) {
            case 0:
                options = ActivityOptions.makeCustomAnimation(activity, R.anim.slide_in_right, R.anim.slide_out_left);
                activity.startActivity(myIntent, options.toBundle());
                break;
            case 1:
                options = ActivityOptions.makeCustomAnimation(activity, R.anim.slide_in_left, R.anim.slide_out_right);
                activity.startActivity(myIntent, options.toBundle());
                break;
            default:
                activity.startActivity(myIntent);
                break;
        }
    }

    static public double onCalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km

        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    public static void initUIActivity (Activity activity) {

        preferences = activity.getSharedPreferences("login", MODE_PRIVATE);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.secondaryColor, activity.getTheme()));
        } else {
//            activity.getWindow().setStatusBarColor(activity.getColor(R.color.back_green));
        }

        // Hide Navigation Bar (Full Screen)
//        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//
//        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
//        final View decorView = activity.getWindow().getDecorView();
//
//
//        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int visibility) {
//                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
//                {
//                    decorView.setSystemUiVisibility(flags);
//                }
//            }
//        });
    }
}
