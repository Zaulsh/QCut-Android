package com.qcut.customer.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.qcut.customer.model.BarberShop;
import com.qcut.customer.model.LightBarberShop;
import com.qcut.customer.model.UserQueueData;

class GetLightBarberShopTask implements Continuation<UserQueueData, Task<UserQueueData>> {
    @Override
    public Task<UserQueueData> then(@NonNull Task<UserQueueData> task) throws Exception {
        final TaskCompletionSource<UserQueueData> tcs = new TaskCompletionSource<>();
        final UserQueueData userQueueData = task.getResult();

        FireManager.mainRef.child(FireManager.RootNames.SHOP_DETAILS+"/"+userQueueData.shopKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            LightBarberShop shop = new LightBarberShop();
                            if (snapshot.child("key").exists()) {
                                shop.key = String.valueOf(snapshot.child("key").getValue());
                            }
                            if (snapshot.child("addressLine1").exists()) {
                                shop.addressLine1 = String.valueOf(snapshot.child("addressLine1").getValue());
                            }
                            if (snapshot.child("addressLine2").exists()) {
                                shop.addressLine2 = String.valueOf(snapshot.child("addressLine2").getValue());
                            }
                            if (snapshot.child("gmapLink").exists()) {
                                shop.gmapLink = String.valueOf(snapshot.child("gmapLink").getValue());
                                String destLocation = shop.gmapLink;
                                if (destLocation.length() > 0) {
                                    double lat = Double.parseDouble(destLocation.split(",")[0]);
                                    double lon = Double.parseDouble(destLocation.split(",")[1]);
                                    LatLng p1 = new LatLng(AppUtils.gLat, AppUtils.gLon);
                                    LatLng p2 = new LatLng(lat, lon);
                                    shop.distance = AppUtils.onCalculationByDistance(p1, p2);
                                } else {
                                    shop.distance = 0.0;
                                }


                            }
                            if (snapshot.child("shopName").exists()) {
                                shop.shopName = String.valueOf(snapshot.child("shopName").getValue());
                            }
                            shop.status = ShopStatus.ONLINE.name();
                            if (snapshot.child("city").exists()) {
                                shop.city = String.valueOf(snapshot.child("city").getValue());
                            }
                            if (snapshot.child("country").exists()) {
                                shop.country = String.valueOf(snapshot.child("country").getValue());
                            }
                            userQueueData.barberShop = shop;
                        }
                        tcs.setResult(userQueueData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        return tcs.getTask();
    }
}
