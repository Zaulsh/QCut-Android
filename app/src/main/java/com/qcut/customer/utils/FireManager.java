package com.qcut.customer.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.qcut.customer.R;
import com.qcut.customer.model.BarberService;
import com.qcut.customer.model.BarberShop;
import com.qcut.customer.model.User;
import com.qcut.customer.model.UserQueueData;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FireManager {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    public static final DatabaseReference mainRef = database.getReference();

    public interface RootNames {
        String BARBER_WAITING_QUEUES = "barberWaitingQueues";
        String CUSTOMERS = "Customers";
        String BARBERS = "barbers";
        String SHOP_DETAILS = "shopDetails";
        String CUSTOMER_VIEW = "customerView";
        String SERVICES_AVAILABLE = "servicesAvailable";
    }

    public static String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String id = currentUser.getUid();
            return id;
        }


        return null;
    }

    public static void saveDataToFirebase(final Map<String, String> params, String url, final saveInfoCallback callback) {
        mainRef.child(url).setValue(params).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onSetDataCallback(params);
            }
        });
    }

    public static void saveDataToFirebase(final Map<String, Object> params, String url, final saveObjectCallback callback) {
        mainRef.child(url).setValue(params).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onSetDataCallback(params);
            }
        });
    }

    public static void updateDataToFirebase(final Map<String, Object> params, String url, final updateInfoCallback callback) {
        mainRef.child(url).updateChildren(params).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onSetDataCallback(params);
            }
        });
    }

    public interface saveInfoCallback {
        void onSetDataCallback(Map<String, String> params);
    }

    public interface saveObjectCallback {
        void onSetDataCallback(Map<String, Object> params);
    }

    public interface updateInfoCallback {
        void onSetDataCallback(Map<String, Object> params);
    }

    public static void getDataFromFirebase(String url, final getInfoCallback callback) {
        mainRef.child(url).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onGetDataCallback(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.notFound();
            }
        });
    }

    public interface getInfoCallback {
        void onGetDataCallback(DataSnapshot snapshot);
        void notFound();
    }

    /*public static void queryDataFromFirebase(String url, String key, String value,
                                             final getInfoCallback callback) {
        Query query = mainRef.child(url).orderByChild(key).equalTo(value);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onGetDataCallback(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.notFound();
            }
        });

    }*/

    public interface AllShopsCallBack {
        void onGetAllShops(List<BarberShop> shops);
        void notFound();
    }

    public interface ShopInfoCallBack {
        void onGetShopDetails(BarberShop snapshot);
        void notFound();
    }

    public static void joinTheQueue(final String shopKey, final UserJoinedQueueCallBack callBack){



        FireManager.getDataFromFirebase(
                RootNames.BARBER_WAITING_QUEUES+"/"+shopKey+"_"+ TimeUtil.getTodayDDMMYYYY(),
                new FireManager.getInfoCallback() {
                    @Override
                    public void onGetDataCallback(DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                            String desiredBarberKey = "";
                            long latestArrivalTimeLong = Long.MIN_VALUE;
                            long maxWaitingTime = 0;
                            int placeInQueue = 0;
                            while (iterator.hasNext()) {
                                DataSnapshot aBarber = iterator.next();
                                Iterator<DataSnapshot> customerIt = aBarber.getChildren().iterator();
                                while (customerIt.hasNext()) {
                                    DataSnapshot customer = customerIt.next();
                                    desiredBarberKey = aBarber.getKey();
                                    if (customer.child("status").exists()
                                            && String.valueOf(customer.child("status").getValue()).equalsIgnoreCase(CustomerStatus.QUEUE.name()) ) {
                                        placeInQueue++;
                                        Object arrivalTime = customer.child("arrivalTime").getValue();
                                        if (arrivalTime != null) {
                                            long arrivalTimeLong = Long.valueOf(arrivalTime.toString());
                                            if (latestArrivalTimeLong < arrivalTimeLong) {
                                                latestArrivalTimeLong = arrivalTimeLong;

                                                maxWaitingTime = Long.valueOf(String.valueOf(customer.child("expectedWaitingTime").getValue()));
                                            }
                                        }
                                    }
                                }
                            }
                            placeInQueue++;
                            maxWaitingTime = maxWaitingTime + 15;
                            pushCustomerToQueue(shopKey, desiredBarberKey, maxWaitingTime, placeInQueue, callBack);

                        } else {
                            mainRef.child(RootNames.BARBERS).child(shopKey)
                                    .orderByChild("queueStatus")
                                    .equalTo("OPEN").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                        if (iterator.hasNext()) {
                                            DataSnapshot barber = iterator.next();
                                            String desiredBarberKey = barber.getKey();
                                            pushCustomerToQueue(shopKey, desiredBarberKey, 0, 1, callBack);
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void notFound() {

                    }
                });

    }

    private static void pushCustomerToQueue(String shopKey, String desiredBarberKey, long maxWaitingTime, int placeInQueue, final UserJoinedQueueCallBack callBack) {
        final String customerKey = AppUtils.preferences.getString(AppUtils.USER_ID, null);
        if (!StringUtils.isEmpty(customerKey)) {
            final Map<String, Object> customerToQueue = new HashMap<>();
            customerToQueue.put("absent", false);
            customerToQueue.put("actualBarberId", desiredBarberKey);
            customerToQueue.put("actualProcessingTime", 0);
            customerToQueue.put("anyBarber", true);
            customerToQueue.put("addedBy", customerKey);
            customerToQueue.put("arrivalTime", new Date().getTime());
            customerToQueue.put("departureTime", 0);
            customerToQueue.put("dragAdjustedTime", 0);
            customerToQueue.put("expectedWaitingTime", maxWaitingTime);
            customerToQueue.put("key", customerKey);
            customerToQueue.put("channel", "CUSTOMER_APP");
            customerToQueue.put("lastPositionChangedTime", 0);
            customerToQueue.put("placeInQueue", placeInQueue);
            customerToQueue.put("serviceStartTime", 0);
            customerToQueue.put("serviceTime", 0);
            customerToQueue.put("status", CustomerStatus.QUEUE);
            customerToQueue.put("timeAdded", -1);
            customerToQueue.put("customerId", AppUtils.preferences.getString(AppUtils.USER_ID, ""));
            customerToQueue.put("name",
                    AppUtils.preferences.getString(AppUtils.USER_DISPLAY_NAME, "--"));
            Task<Void> voidTask = mainRef.child(RootNames.BARBER_WAITING_QUEUES)
                    .child(shopKey+"_"+TimeUtil.getTodayDDMMYYYY())
                    .child(desiredBarberKey).child(customerKey).getRef().updateChildren(customerToQueue);
            voidTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    callBack.status(true);
                }
            });
        }
    }

    public static void removeUserFromQueue(String shopKey, final UserRemovedCallBack callBack) {
        final String customerId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
        mainRef.child(FireManager.RootNames.BARBER_WAITING_QUEUES+"/" + shopKey + "_" + TimeUtil.getTodayDDMMYYYY()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> barberIT = dataSnapshot.getChildren().iterator();
                    while (barberIT.hasNext()) {
                        final DataSnapshot aBarber = barberIT.next();
                        if (aBarber.child(customerId).exists()) {
                            aBarber.child(customerId).child("status").getRef().setValue(CustomerStatus.REMOVED, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        aBarber.child(customerId).child("status").getRef().setValue(CustomerStatus.REMOVED, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if (databaseError != null) {
                                                    callBack.removed(false);
                                                } else {
                                                    callBack.removed(true);
                                                }
                                            }
                                        });
                                    } else {
                                        callBack.removed(true);
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callBack.removed(false);
            }
        });
    }

    public interface UserRemovedCallBack {
        void removed(boolean removed);
    }

    public interface UserJoinedQueueCallBack {
        void status(boolean isQueued);
    }

    public interface WaitingTimeCallBack {
        void  onGetWaitingTime(long waitingTime, CustomerStatus customerStatus);
    }

    public static void continouslyReadWaitingTime(String shopKey, final String userId, final WaitingTimeCallBack callBack) {
        mainRef.child(RootNames.BARBER_WAITING_QUEUES).child(shopKey+"_"+TimeUtil.getTodayDDMMYYYY()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot barber = iterator.next();
                        if (barber.child(userId).child("expectedWaitingTime").exists()) {
                            Object expectedWaitingTime = barber.child(userId).child("expectedWaitingTime").getValue();
                            String customerStatus = String.valueOf(barber.child(userId).child("status").getValue());
                            if (expectedWaitingTime != null && StringUtils.isNumeric(String.valueOf(expectedWaitingTime))
                                    && !StringUtils.isEmpty(customerStatus)) {
                                long waitingTime = Long.valueOf(String.valueOf(expectedWaitingTime));
                                callBack.onGetWaitingTime(waitingTime, CustomerStatus.valueOf(customerStatus));
                            } else {
                                callBack.onGetWaitingTime(-1, null);
                            }
                        } else {
                            callBack.onGetWaitingTime(-1, null);
                        }
                    }
                } else {
                    callBack.onGetWaitingTime(-1, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callBack.onGetWaitingTime(-1, null);
            }
        });

    }

    public static void userQueueStatusData(final String userId, OnSuccessListener<UserQueueData> onSuccessListener, OnFailureListener onFailureListener) {

        Tasks.<Void>forResult(null)
                .continueWithTask(new GetCustomerQueueStatusTask(userId))
                .continueWithTask(new GetLightBarberShopTask())
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public static void getAllShops(OnSuccessListener<List<BarberShop>> callBack, OnFailureListener failureListener) {
        Tasks.<Void>forResult(null)
                .continueWithTask(new GetShopsTask())
                .continueWithTask(new GetShopServicesTask())
                .continueWithTask(new GetShopsStatusesTask())
                .continueWithTask(new GetShopsBarberStatusesTask())
                .addOnSuccessListener(callBack)
                .addOnFailureListener(failureListener);
    }

    public static void saveNewFirebaseToken(final String userId, final Context context) {
        if (!StringUtils.isEmpty(userId) ) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        String android_id = Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        Map<String, Object> data = new HashMap<>();
                        data.put(android_id, token);
                        mainRef.child(RootNames.CUSTOMERS).child(userId).child("notificationFirebaseTokens").updateChildren(data);
                    }
                }
            });
        }
    }

    public static void saveNewFirebaseWithGivenToken(final String token, Context context) {
        if (!StringUtils.isEmpty(token)) {
            String userId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
            if (!StringUtils.isEmpty(userId)) {
                String android_id = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Map<String, Object> data = new HashMap<>();
                data.put(android_id, token);
                mainRef.child(RootNames.CUSTOMERS).child(userId).child("notificationFirebaseTokens").updateChildren(data);
            }
        }
    }
    public static void removeFirebaseToken(String customerId, Context context) {
        if (!StringUtils.isEmpty(customerId)) {
            String android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            mainRef.child(RootNames.CUSTOMERS).child(customerId).child("notificationFirebaseTokens").child(android_id).removeValue();
        }
    }

}
