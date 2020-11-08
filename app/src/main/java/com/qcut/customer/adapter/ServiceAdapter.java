package com.qcut.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qcut.customer.R;
import com.qcut.customer.model.BarberService;

import java.util.List;

public class ServiceAdapter extends BaseAdapter {

    Context mContext;
    List<BarberService> services;
    public ServiceAdapter(Context context, List<BarberService> services) {
        mContext = context;
        this.services = services;
    }
    @Override
    public int getCount() {
        return services.size();
    }

    @Override
    public BarberService getItem(int position) {
        return services.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BarberService barberService = this.services.get(position);
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_service, null);
        TextView serviceName = convertView.findViewById(R.id.service_name);
        TextView servicePrice = convertView.findViewById(R.id.service_price);
        serviceName.setText(barberService.getName());
        String euro = "\u20ac";
        servicePrice.setText(euro +" "+barberService.getPrice());
        return convertView;
    }
}
