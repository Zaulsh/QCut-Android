package com.qcut.customer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.qcut.customer.R;
import com.qcut.customer.model.BarberShop;
import com.qcut.customer.utils.AppUtils;
import com.qcut.customer.utils.ShopStatus;

import java.util.List;

public class BarberShopAdapter extends BaseAdapter {
    Context mContext;
    List<BarberShop> mListBarberShop;

    public BarberShopAdapter(Context context, List<BarberShop> list) {
        mContext = context;
        mListBarberShop = list;
    }

    @Override
    public int getCount() {
        return mListBarberShop.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_barbershop, null);
//        LinearLayout cardBackground = convertView.findViewById(R.id.card_background);
//        if (position % 3 == 0) {
//            cardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.card_gradient));
//        } else if (position % 3 == 1) {
//            cardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.card_gradient_blue));
//        } else if (position % 3 == 2) {
//            cardBackground.setBackground(mContext.getResources().getDrawable(R.drawable.card_gradient_green));
//        }
        TextView shopName = convertView.findViewById(R.id.shop_name);
        TextView txtDistance = convertView.findViewById(R.id.txt_distance);
        TextView txtAddress1 = convertView.findViewById(R.id.txt_address1);
        TextView txtAddress2 = convertView.findViewById(R.id.txt_address2);
        ImageView searchShopStatus = convertView.findViewById(R.id.search_shop_status);
        if (mListBarberShop.get(position).status.equalsIgnoreCase(ShopStatus.ONLINE.name())) {
            searchShopStatus.setImageResource(R.drawable.circle_green);
        }
//        TextView cityCountry = convertView.findViewById(R.id.city_country);

        Drawable img = mContext.getResources().getDrawable(R.drawable.ic_location_white);
        img.setBounds(0, 0, 60, 60);
        txtDistance.setCompoundDrawables(img, null, null, null);


        txtAddress1.setText(mListBarberShop.get(position).addressLine1);
        txtAddress2.setText(mListBarberShop.get(position).addressLine2+", "+
                mListBarberShop.get(position).city);
        shopName.setText(mListBarberShop.get(position).shopName);
//
        String distance = String.format("%.1f",mListBarberShop.get(position).distance) + "Km";
        txtDistance.setText(distance);
        return convertView;
    }
}
