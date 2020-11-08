package com.qcut.customer.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.customer.model.BarberShop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GetShopsBarberStatusesTask implements Continuation<List<BarberShop>, Task<List<BarberShop>>> {
    private String OPEN = "OPEN";
    private String STOP = "STOP";
    @Override
    public Task<List<BarberShop>> then(@NonNull final Task<List<BarberShop>> task) throws Exception {
        final TaskCompletionSource<List<BarberShop>> tcs = new TaskCompletionSource<>();
        final List<BarberShop> shops = task.getResult();
        FireManager.mainRef.child(FireManager.RootNames.BARBERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (BarberShop shop : shops) {
                        //if (shop.status.equalsIgnoreCase(ShopStatus.ONLINE.name())) {
                            List<BarberStatus> barberStatuses = new ArrayList<>();
                            boolean atLeastOneBarberOnline = false;
                            Iterator<DataSnapshot> iterator = dataSnapshot.child(shop.key).getChildren().iterator();
                            while (iterator.hasNext()) {
                                DataSnapshot barber = iterator.next();
                                BarberStatus barberStatus = new BarberStatus();
                                barberStatus.key = barber.getKey();
                                if (barber.child("name").exists()) {
                                    barberStatus.name = String.valueOf(barber.child("name").getValue());
                                }
                                if (barber.child("queueStatus").exists()) {
                                    String queueStatus = String.valueOf(barber.child("queueStatus").getValue());
                                    if (queueStatus.equalsIgnoreCase(OPEN)) {
                                        atLeastOneBarberOnline = true;
                                        barberStatus.available = true;
                                    } else {
                                        barberStatus.available = false;
                                    }
                                } else {
                                    barberStatus.available = false;
                                }
                                barberStatuses.add(barberStatus);
                            }

                            if (atLeastOneBarberOnline) {
                                shop.status = ShopStatus.ONLINE.name();
                            } else {
                                shop.status = ShopStatus.OFFLINE.name();
                            }

                    }
                }
                tcs.setResult(shops);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return tcs.getTask();
    }
}
