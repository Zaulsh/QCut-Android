package com.qcut.customer.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.customer.model.UserQueueData;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

class GetCustomerQueueStatusTask implements Continuation<Void, Task<UserQueueData>> {
    private String userId;
    public GetCustomerQueueStatusTask(String userId) {
        this.userId = userId;
    }

    @Override
    public Task<UserQueueData> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<UserQueueData> tcs = new TaskCompletionSource<>();
        FireManager.mainRef.child(FireManager.RootNames.BARBER_WAITING_QUEUES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        UserQueueData queueData = getQueueData(dataSnapshot);
                        if (!StringUtils.isEmpty(queueData.barberKey)) {
                            tcs.setResult(queueData);
                        } else {
                            oneMoreAttempt(tcs);
                        }
                    } catch (Exception e) {
                        oneMoreAttempt(tcs);
                    }

                } else {
                    tcs.setResult(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                oneMoreAttempt(tcs);
            }
        });
        return tcs.getTask();
    }

    private void oneMoreAttempt(final TaskCompletionSource<UserQueueData> tcs) {
        FireManager.mainRef.child(FireManager.RootNames.BARBER_WAITING_QUEUES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            tcs.setResult(getQueueData(dataSnapshot));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private UserQueueData getQueueData(@NonNull DataSnapshot dataSnapshot) {
        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            DataSnapshot shopDate = iterator.next();

            if (shopDate.getKey().endsWith("_"+ TimeUtil.getTodayDDMMYYYY()) ) {

                Iterator<DataSnapshot> iterator1 = shopDate.getChildren().iterator();
                while (iterator1.hasNext()) {
                    DataSnapshot barber = iterator1.next();
                    if (barber.child(userId).exists()
                            && ( barber.child(userId).child("status").getValue().toString()
                            .equalsIgnoreCase(CustomerStatus.QUEUE.name())
                            || barber.child(userId).child("status").getValue().toString()
                            .equalsIgnoreCase(CustomerStatus.PROGRESS.name()) )) {

                        UserQueueData userQueueData = new UserQueueData();
                        String[] splittedShopDate = shopDate.getKey().split("_");
                        if (splittedShopDate.length == 2) {
                            userQueueData.shopKey = splittedShopDate[0];
                        }
                        userQueueData.barberKey = barber.getKey();
                        userQueueData.customerKey = userId;

                        if (barber.child(userId).child("arrivalTime").exists()) {
                            userQueueData.arrivalTime = Long.valueOf(String.valueOf(barber.child(userId).child("arrivalTime").getValue()));
                        }
                        if (barber.child(userId).child("expectedWaitingTime").exists()) {
                            userQueueData.expectedWaitingTime = Long.valueOf(String.valueOf(barber.child(userId).child("expectedWaitingTime").getValue()));
                        }
                        if (barber.child(userId).child("placeInQueue").exists()) {
                            userQueueData.placeInQueue = Integer.valueOf(String.valueOf(barber.child(userId).child("placeInQueue").getValue()));
                        }
                        return (userQueueData);
                    }
                }
            }


        }
        return new UserQueueData();
    }
}
