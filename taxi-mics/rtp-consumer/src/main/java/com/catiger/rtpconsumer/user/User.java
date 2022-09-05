package com.catiger.rtpconsumer.user;


import java.util.Optional;

public class User {
    protected String account;
    protected double latitude, longitude;
    protected User binding;
    public User (String account, double latitude, double longitude) {
        this.account = account;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public boolean existBinding() {
        return binding != null;
    }
    public void setBinding(User user) {
        binding = user;
    }
    public String getBindingAccount(){
        return binding.account;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public User getBinding() {
        return binding;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
