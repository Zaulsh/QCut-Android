package com.qcut.customer.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.qcut.customer.R;
import com.qcut.customer.activity.LoginActivity;
import com.qcut.customer.activity.MainActivity;
import com.qcut.customer.adapter.TabAdapter;
import com.qcut.customer.model.BarberShop;
import com.qcut.customer.utils.AppUtils;
import com.qcut.customer.utils.CustomerStatus;
import com.qcut.customer.utils.FireManager;
import com.qcut.customer.utils.SharedPrefManager;
import com.qcut.customer.utils.ShopStatus;
import com.qcut.customer.utils.TimeUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class JoinFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    MainActivity mainActivity;

    TabAdapter pageAdapter;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout llt_join_btn1, llt_join_btn;
    BarberShop barberShop;
    private TextView shopName, shopAddressLine1, shopAddressLine2;
    private TextView distance, likes, status, jtq, jtq_1;
    private SharedPrefManager sharedPrefManager;

    public JoinFragment(MainActivity activity, BarberShop barberShop) {
        mainActivity = activity;
        this.barberShop = barberShop;
        sharedPrefManager = new SharedPrefManager(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join, container, false);
        initUIView(view);
        initData(barberShop);
        return view;
    }

    private void initData(BarberShop barberShop) {
        shopName.setText(barberShop.shopName);
        shopAddressLine1.setText(barberShop.addressLine1);
        shopAddressLine2.setText(barberShop.addressLine2);

        distance.setText(String.format("%.1f",barberShop.distance) + "Km");
        likes.setText("21 likes");
        status.setText(barberShop.status.toLowerCase());


    }

    private void initUIView(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        llt_join_btn = view.findViewById(R.id.llt_join_btn);
        llt_join_btn1 = view.findViewById(R.id.llt_join_btn1);
        jtq = view.findViewById(R.id.jtq);
        jtq_1 = view.findViewById(R.id.jtq_1);

        shopName = view.findViewById(R.id.shop_name);
        shopAddressLine1 = view.findViewById(R.id.txt_address1);
        shopAddressLine2 = view.findViewById(R.id.txt_address2);

        distance = view.findViewById(R.id.txt_distance);
        likes = view.findViewById(R.id.txt_likes);
        status = view.findViewById(R.id.txt_status);

        Drawable distanceIcon = mainActivity.getResources().getDrawable(R.drawable.ic_location_white);
        distanceIcon.setBounds(0, 0, 60, 60);
        distance.setCompoundDrawables(distanceIcon, null, null, null);


        Drawable likesIcon = mainActivity.getResources().getDrawable(R.drawable.ic_favorite_white);
        likesIcon.setBounds(0, 0, 60, 60);
        likes.setCompoundDrawables(likesIcon, null, null, null);



//        Drawable statusIcon = mainActivity.getResources().getDrawable(R.drawable.circle_grey);
//        statusIcon.setBounds(0, 0, 60, 60);
//        if (barberShop.status.equalsIgnoreCase("ONLINE")) {
//            statusIcon = mainActivity.getResources().getDrawable(R.drawable.circle_green);
//        }
//        status.setCompoundDrawables(statusIcon, null, null, null);


        pageAdapter = new TabAdapter(getFragmentManager());
        pageAdapter.addFragment(new ServiceFragment(barberShop), "SERVICES");
        pageAdapter.addFragment(new HoursFragment(), "HOURS");
        pageAdapter.addFragment(new DetailFragment(barberShop), "DETAILS");

        viewPager.setAdapter(pageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(this);

        llt_join_btn1.setOnClickListener(this);
        llt_join_btn.setOnClickListener(this);
        llt_join_btn.setEnabled(false);

        Drawable statusIcon = mainActivity.getResources().getDrawable(R.drawable.circle_grey);
        if (barberShop.status.equalsIgnoreCase(ShopStatus.ONLINE.name())) {
            statusIcon = mainActivity.getResources().getDrawable(R.drawable.circle_green);
        } else {
            llt_join_btn.setBackgroundColor(mainActivity.getResources().getColor(R.color.browser_actions_bg_grey));
            ((TextView)llt_join_btn.findViewById(R.id.jtq))
                    .setTextColor(mainActivity.getResources().getColor(R.color.lightGray));
            ((TextView)llt_join_btn1.findViewById(R.id.jtq_1))
                    .setTextColor(mainActivity.getResources().getColor(R.color.lightGray));
            llt_join_btn1.setBackgroundColor(mainActivity.getResources().getColor(R.color.browser_actions_bg_grey));
            llt_join_btn1.setEnabled(false);
            jtq.setText("Barber Offline");
            jtq_1.setText("Barber Offline");
        }


        statusIcon.setBounds(0, 0, 30, 30);
        status.setCompoundDrawables(statusIcon, null, null, null);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            llt_join_btn1.setVisibility(View.VISIBLE);
            llt_join_btn.setVisibility(View.GONE);
        } else {
            llt_join_btn1.setVisibility(View.GONE);
            llt_join_btn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llt_join_btn:
                onJoinTheQueue();
                break;
            case R.id.llt_join_btn1:
                onJoinTheQueue();
                break;
        }
    }

    private void onJoinTheQueue() {
        final ProgressDialog dialog = AppUtils.onShowProgressDialog(getActivity(), "Joining the queue now", false);
        if(! AppUtils.preferences.getBoolean(AppUtils.IS_LOGGED_IN,false)) {
            Intent intent = new Intent(mainActivity, LoginActivity.class);
            startActivity(intent);
        } else {

            String userId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
            if (!StringUtils.isEmpty(userId)) {
                FireManager.joinTheQueue(barberShop.key, new FireManager.UserJoinedQueueCallBack() {
                    @Override
                    public void status(boolean isQueued) {
                        if (isQueued) {
                            sharedPrefManager.setLongSharedPref(SharedPrefManager.LAST_WAITING_TIME, -1);
                            mainActivity.bottomNavigationView.setSelectedItemId(R.id.action_queue);
                            mainActivity.onGoQueueFrg(barberShop);
                            AppUtils.onDismissProgressDialog(dialog);
                        } else {
                            AppUtils.onDismissProgressDialog(dialog);
                            Toast.makeText(getActivity(), "Could not join the queue - Try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
//        AppUtils.onDismissProgressDialog(dialog);
    }
}
