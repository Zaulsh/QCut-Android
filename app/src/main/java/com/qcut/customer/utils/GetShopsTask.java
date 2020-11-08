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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GetShopsTask implements Continuation<Void, Task<List<BarberShop>>> {
    @Override
    public Task<List<BarberShop>> then(@NonNull Task<Void> task) throws Exception {
        final TaskCompletionSource<List<BarberShop>> tcs = new TaskCompletionSource<>();
        FireManager.mainRef.child(FireManager.RootNames.SHOP_DETAILS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarberShop> shops = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        BarberShop shop = new BarberShop();
                        DataSnapshot snapshot = iterator.next();
                        if (snapshot.child("addressLine1").exists()) {
                            shop.addressLine1 = String.valueOf(snapshot.child("addressLine1").getValue());
                        }
                        if (snapshot.child("addressLine2").exists()) {
                            shop.addressLine2 = String.valueOf(snapshot.child("addressLine2").getValue());
                        }
                        if (snapshot.child("city").exists()) {
                            shop.city = String.valueOf(snapshot.child("city").getValue());
                        }
                        if (snapshot.child("country").exists()) {
                            shop.country = String.valueOf(snapshot.child("country").getValue());
                        }
                        if (snapshot.child("email").exists()) {
                            shop.email = String.valueOf(snapshot.child("email").getValue());
                        }
                        if (snapshot.child("gmapLink").exists()) {
                            shop.gmapLink = String.valueOf(snapshot.child("gmapLink").getValue());
                            String destLocation = shop.gmapLink;
                            destLocation.replace(" ", "");
                            if (destLocation.length() > 0){
                                double lat = Double.parseDouble(destLocation.split(",")[0]);
                                double lon = Double.parseDouble(destLocation.split(",")[1]);
                                LatLng p1 = new LatLng(AppUtils.gLat, AppUtils.gLon);
                                LatLng p2 = new LatLng(lat, lon);
                                shop.distance = AppUtils.onCalculationByDistance(p1, p2);
                            } else {
                                shop.distance = 0;
                            }
                        }
                        if (snapshot.child("key").exists()) {
                            shop.key = String.valueOf(snapshot.child("key").getValue());
                        }
                        if (snapshot.child("shopName").exists()) {
                            shop.shopName = String.valueOf(snapshot.child("shopName").getValue());
                        }
                        shop.status = ShopStatus.OFFLINE.name();
                        shops.add(shop);

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
