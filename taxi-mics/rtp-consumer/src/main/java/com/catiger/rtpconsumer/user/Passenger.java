package com.catiger.rtpconsumer.user;

import lombok.Data;

public class Passenger extends User{
    public Passenger(String account, double latitude, double longitude) {
        super(account, latitude, longitude);
    }
}
