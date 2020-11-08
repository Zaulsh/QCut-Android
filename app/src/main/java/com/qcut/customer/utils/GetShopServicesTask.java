package com.qcut.customer.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.qcut.customer.model.BarberService;
import com.qcut.customer.model.BarberShop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GetShopServicesTask implements Continuation<List<BarberShop>, Task<List<BarberShop>>> {

    @Override
    public Task<List<BarberShop>> then(@NonNull final Task<List<BarberShop>> task) throws Exception {
        final TaskCompletionSource<List<BarberShop>> tcs = new TaskCompletionSource<>();
        final List<BarberShop> shops = task.getResult();
        FireManager.getDataFromFirebase(FireManager.RootNames.SERVICES_AVAILABLE, new FireManager.getInfoCallback() {
            @Override
            public void onGetDataCallback(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for(BarberShop shop : shops) {

                        if (snapshot.child(shop.key).exists()) {
                            List<BarberService> services = new ArrayList<>();
                            Iterator<DataSnapshot> iterator = snapshot.child(shop.key).getChildren().iterator();
                            while (iterator.hasNext()) {
                                DataSnapshot serviceKey = iterator.next();
                                String serviceName = String.valueOf(serviceKey.child("serviceName").getValue());
                                String servicePrice = String.valueOf(serviceKey.child("servicePrice").getValue());
                                services.add(new BarberService(serviceName, servicePrice));
                            }
                            shop.services = services;
                        }
                    }
                }
                tcs.setResult(shops);
            }

            @Override
            public void notFound() {
            }
        });
        return tcs.getTask();
    }
}
