package com.qcut.customer.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.qcut.customer.R;
import com.qcut.customer.adapter.ServiceAdapter;
import com.qcut.customer.model.BarberShop;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceFragment extends Fragment {

    private ListView lst_service;

    ServiceAdapter adapter;
    BarberShop barberShop;
    public ServiceFragment(BarberShop barberShop) {
        this.barberShop = barberShop;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        lst_service = view.findViewById(R.id.lst_service);
        adapter = new ServiceAdapter(getContext(), this.barberShop.services);
        lst_service.setAdapter(adapter);
        return view;
    }

}
