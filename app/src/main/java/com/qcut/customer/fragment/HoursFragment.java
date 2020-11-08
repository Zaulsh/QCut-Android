package com.qcut.customer.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qcut.customer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HoursFragment extends Fragment {


    public HoursFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hours, container, false);
    }

}
