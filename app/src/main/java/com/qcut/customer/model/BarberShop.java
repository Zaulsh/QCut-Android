package com.qcut.customer.model;

import com.qcut.customer.utils.BarberStatus;

import java.util.ArrayList;
import java.util.List;

public class BarberShop {
    public String key = "";
    public String addressLine1 = "";
    public String addressLine2 = "";
    public String gmapLink = "";
    public String shopName = "";
    public String status = "";
    public String city = "";
    public String country = "";
    public double distance = 0;
    public String email ="";
    public List<BarberService> services = new ArrayList<>();
    public List<BarberStatus> barberStatuses = new ArrayList<>();
}
