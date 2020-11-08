package com.qcut.customer.fragment;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.qcut.customer.R;
import com.qcut.customer.activity.MainActivity;
import com.qcut.customer.model.BarberShop;
import com.qcut.customer.model.UserQueueData;
import com.qcut.customer.utils.AppUtils;
import com.qcut.customer.utils.CustomerStatus;
import com.qcut.customer.utils.SharedPrefManager;
import com.qcut.customer.utils.ShopStatus;
import com.qcut.customer.utils.FireManager;
import com.qcut.customer.utils.TimeUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class QueueFragment extends Fragment implements View.OnClickListener {

    MainActivity mainActivity;

    private RelativeLayout rlt_unqueue;
    private LinearLayout llt_queue, llt_leave_queue, llt_select_barber;
    private BarberShop barberShop;
    private TextView shopName, addressLine1, addressLine2;
    private TextView distance, likes, status;
    private TextView customerName, waitingTime, noteMessage;
    private ProgressDialog dialog;

    public QueueFragment(MainActivity activity, BarberShop barberShop) {
        mainActivity = activity;
        this.barberShop = barberShop;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialog = AppUtils.onShowProgressDialog(getActivity(), "Loading..", false);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
        shopName = view.findViewById(R.id.shop_name);
        addressLine1 = view.findViewById(R.id.txt_address1);
        addressLine2 = view.findViewById(R.id.txt_address2);
        distance = view.findViewById(R.id.txt_distance);
        likes = view.findViewById(R.id.txt_likes);
        status = view.findViewById(R.id.txt_status);
        customerName = view.findViewById(R.id.queue_view_customer_name);
        waitingTime = view.findViewById(R.id.queue_view_waiting_time);
        noteMessage = view.findViewById(R.id.queue_note_message);
        initUIView(view);
        return view;
    }

    private void initUIView(View view) {
        rlt_unqueue = view.findViewById(R.id.rlt_unqueu_view);
        llt_queue = view.findViewById(R.id.llt_queue_view);
        llt_leave_queue = view.findViewById(R.id.llt_leave_queue);
        llt_select_barber = view.findViewById(R.id.llt_select_barber);
        final String userId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
        if (!StringUtils.isEmpty(userId)) {
            FireManager.userQueueStatusData(userId, new OnSuccessListener<UserQueueData>() {
                @Override
                public void onSuccess(UserQueueData userQueueData) {

                    if (userQueueData != null && !StringUtils.isEmpty(userQueueData.barberKey)) {

                        rlt_unqueue.setVisibility(View.GONE);
                        llt_queue.setVisibility(View.VISIBLE);

                        shopName.setText(userQueueData.barberShop.shopName);
                        addressLine1.setText(userQueueData.barberShop.addressLine1);
                        addressLine2.setText(userQueueData.barberShop.addressLine2 + ", " + barberShop.city);
                        distance.setText(String.format("%.1f", userQueueData.barberShop.distance) + "Km");

                        likes.setText("21 likes");
                        Drawable distanceIcon = mainActivity.getResources().getDrawable(R.drawable.ic_location_white);
                        distanceIcon.setBounds(0, 0, 60, 60);
                        distance.setCompoundDrawables(distanceIcon, null, null, null);

                        Drawable likesIcon = mainActivity.getResources().getDrawable(R.drawable.ic_favorite_white);
                        likesIcon.setBounds(0, 0, 60, 60);
                        likes.setCompoundDrawables(likesIcon, null, null, null);

                        final String customerDisplayName = AppUtils.preferences.getString(AppUtils.USER_DISPLAY_NAME, null);
                        if (!StringUtils.isEmpty(customerDisplayName)) {
                            customerName.setText(customerDisplayName);
                        }

                        status.setText(ShopStatus.ONLINE.name());
                        Drawable statusIcon = mainActivity.getResources().getDrawable(R.drawable.circle_green);
                        statusIcon.setBounds(0, 0, 30, 30);
                        status.setCompoundDrawables(statusIcon, null, null, null);

                        FireManager.continouslyReadWaitingTime(userQueueData.barberShop.key, userId,  new FireManager.WaitingTimeCallBack() {
                            @Override
                            public void onGetWaitingTime(long waitingTimeReceived, CustomerStatus customerStatus) {
                                if (customerStatus == null) {
                                    return;
                                }
                                if (customerStatus.equals(CustomerStatus.PROGRESS)) {
                                    waitingTime.setText("On Chair");
                                    noteMessage.setText("Your service is in progress");
                                    llt_leave_queue.setVisibility(View.GONE);
                                } else if (customerStatus.equals(CustomerStatus.DONE)){
                                    waitingTime.setText("DONE");
                                    llt_leave_queue.setVisibility(View.VISIBLE);
                                    rlt_unqueue.setVisibility(View.VISIBLE);
                                    llt_queue.setVisibility(View.GONE);
                                } else {
                                    llt_leave_queue.setVisibility(View.VISIBLE);
                                    if (waitingTimeReceived >= 0) {
                                        if (waitingTimeReceived == 0) {
                                            waitingTime.setText("Ready");
                                            noteMessage.setText("Your turn has come and barber is waiting for you.");
                                        } else {
                                            String displayWaitingTime = TimeUtil.getDisplayWaitingTime(waitingTimeReceived);
                                            waitingTime.setText(displayWaitingTime);
                                            if (waitingTimeReceived <= 15) {
                                                noteMessage.setText("Your turn is about to come. Please arrive at the shop.");
                                            }
                                        }
                                    }
                                }
                            }
                        });

                        llt_leave_queue.setTag(userQueueData.shopKey);
                        AppUtils.onDismissProgressDialog(dialog);
                    } else {
                        rlt_unqueue.setVisibility(View.VISIBLE);
                        llt_queue.setVisibility(View.GONE);
                        AppUtils.onDismissProgressDialog(dialog);
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    rlt_unqueue.setVisibility(View.VISIBLE);
                    llt_queue.setVisibility(View.GONE);
                    AppUtils.onDismissProgressDialog(dialog);
                }
            });
        } else {
            Toast.makeText(getActivity(), "You are not Logged in - Queue details not found", Toast.LENGTH_SHORT).show();
            rlt_unqueue.setVisibility(View.VISIBLE);
            llt_queue.setVisibility(View.GONE);
            AppUtils.onDismissProgressDialog(dialog);
        }

        llt_leave_queue.setOnClickListener(this);
        llt_select_barber.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llt_select_barber:
                mainActivity.bottomNavigationView.setSelectedItemId(R.id.action_search);

                break;
            case R.id.llt_leave_queue:
                final ProgressDialog dialog = AppUtils.onShowProgressDialog(getActivity(), "Removing from the queue", false);
                Object tag = llt_leave_queue.getTag();
                if (tag!= null && !StringUtils.isEmpty(tag.toString())) {
                    String shopKey = tag.toString();
                    FireManager.removeUserFromQueue(shopKey, new FireManager.UserRemovedCallBack() {
                        @Override
                        public void removed(boolean removed) {
                            AppUtils.onDismissProgressDialog(dialog);
                            if (removed) {
                                mainActivity.bottomNavigationView.setSelectedItemId(R.id.action_search);
                            } else {
                                Toast.makeText(getActivity(), "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    AppUtils.onDismissProgressDialog(dialog);
                    Toast.makeText(getActivity(), "Error fetching shop details. Try again.", Toast.LENGTH_SHORT).show();
                }
                /*FireManager.userQueueStatusData(new FireManager.UserQueueInfoCallBack() {
                    @Override
                    public void onUserQueueDetails(UserQueueData userQueueData) {
                        FireManager.removeUserFromQueue(userQueueData.shopKey, new FireManager.UserRemovedCallBack() {
                            @Override
                            public void removed(boolean removed) {
                                AppUtils.onDismissProgressDialog(dialog);
                                if (removed) {
                                    mainActivity.bottomNavigationView.setSelectedItemId(R.id.action_search);
                                } else {
                                    Toast.makeText(getActivity(), "Something went wront. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void notFound() {
                        AppUtils.onDismissProgressDialog(dialog);
                        Toast.makeText(getActivity(), "Something went wront. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });*/

                break;
        }
    }
}
