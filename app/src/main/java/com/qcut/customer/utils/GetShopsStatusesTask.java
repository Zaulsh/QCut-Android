package com.qcut.customer.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.customer.model.BarberShop;

import java.util.List;

class GetShopsStatusesTask implements Continuation<List<BarberShop>, Task<List<BarberShop>>> {
    @Override
    public Task<List<BarberShop>> then(@NonNull Task<List<BarberShop>> task) throws Exception {
        final TaskCompletionSource<List<BarberShop>> tcs = new TaskCompletionSource<>();
        final List<BarberShop> shops = task.getResult();

        FireManager.mainRef.child(FireManager.RootNames.BARBER_WAITING_QUEUES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (BarberShop shop : shops) {
                                if (dataSnapshot.child(shop.key+"_"+TimeUtil.getTodayDDMMYYYY()).exists()) {
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
