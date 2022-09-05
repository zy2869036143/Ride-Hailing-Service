package com.catiger.taxi.data;

public class Driver {
    private String account;
    private String license;
    public Driver(String account) {
        this.account =account;
    }
    public Driver(String account, String license) {
        this.account = account;
        this.license = license;
    }
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
