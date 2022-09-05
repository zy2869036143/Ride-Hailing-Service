package com.catiger.taxi.tool;

import java.util.Date;

public class Order {
    private long oid;
    private String apptime;
    private String account;
    private double slat, slon;
    private double elat, elon;
    private String splace, eplace;
    private double km, minutes;
    private double price;
    public Order(String account) {
        this.account = account;
    }
    public Order(String account, double myLat, double myLon, double toLat, double toLon) {
        this.account = account;
        this.slat = myLat;
        this.slon = myLon;
        this.elat = toLat;
        this.elon = toLon;
    }

    public void setApptime(String apptime) {
        this.apptime = apptime;
    }

    public String getApptime() {
        return apptime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public long getOid() {
        return oid;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public void setMinutes(double minutes) {
        this.minutes = minutes;
    }

    public double getKm() {
        return km;
    }

    public double getMinutes() {
        return minutes;
    }

    public String getAccount() {
        return account;
    }

    public String getSplace() {
        return splace;
    }

    public String getEplace() {
        return eplace;
    }

    public double getElat() {
        return elat;
    }

    public double getElon() {
        return elon;
    }

    public double getSlat() {
        return slat;
    }


    public double getSlon() {
        return slon;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setSplace(String splace) {
        this.splace = splace;
    }

    public void setEplace(String eplace) {
        this.eplace = eplace;
    }

    public void setElat(double elat) {
        this.elat = elat;
    }

    public void setElon(double elon) {
        this.elon = elon;
    }

    public void setSlat(double slat) {
        this.slat = slat;
    }

    public void setSlon(double slon) {
        this.slon = slon;
    }
}
