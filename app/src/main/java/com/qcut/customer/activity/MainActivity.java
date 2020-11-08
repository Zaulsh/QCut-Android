package com.qcut.customer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qcut.customer.R;
import com.qcut.customer.fragment.JoinFragment;
import com.qcut.customer.fragment.ProfileFragment;
import com.qcut.customer.fragment.QueueFragment;
import com.qcut.customer.fragment.SearchFrgament;
import com.qcut.customer.model.BarberShop;
import com.qcut.customer.utils.AppUtils;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public BottomNavigationView bottomNavigationView;

    private int currentFrg = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppUtils.initUIActivity(this);
        initUIView();
        initUIEvent();

        onGoSearchFrg();
    }

    private void initUIView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }


    private void initUIEvent() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                onGoSearchFrg();
                break;
            case R.id.action_queue:
                onGoQueueFrg(new BarberShop());
                break;
            case R.id.action_profile:
                boolean isLoggedIn = AppUtils.preferences.getBoolean(AppUtils.IS_LOGGED_IN, false);
                if (isLoggedIn) {
                    onGoProfileFrg();
                } else {
                    AppUtils.showOtherActivity(MainActivity.this, LoginActivity.class, 0);
                }
                break;
        }
        return true;
    }

    public void onGoSearchFrg() {
        currentFrg = 0;
        Fragment frg = new SearchFrgament(MainActivity.this);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction frgTran = fm.beginTransaction();
        frgTran.replace(R.id.frg_main, frg);
        frgTran.commit();
    }

    public void onGoQueueFrg(BarberShop barberShop) {
        currentFrg = 1;
        Fragment frg = new QueueFragment(MainActivity.this, barberShop);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction frgTran = fm.beginTransaction();
        frgTran.replace(R.id.frg_main, frg);
        frgTran.commit();
    }

    public void onGoProfileFrg() {
        currentFrg = 2;
        Fragment frg = new ProfileFragment(MainActivity.this);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction frgTran = fm.beginTransaction();
        frgTran.replace(R.id.frg_main, frg);
        frgTran.commit();
    }

    public void onGoPageViewFragment(BarberShop barberShop) {

        Fragment frg = new JoinFragment(MainActivity.this, barberShop);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction frgTran = fm.beginTransaction();
        frgTran.replace(R.id.frg_main, frg);
        frgTran.commit();
    }

    public void onGoLoginActivity() {
        AppUtils.showOtherActivity(MainActivity.this, LoginActivity.class, 1);
    }
}
