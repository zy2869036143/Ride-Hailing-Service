package com.catiger.rtpconsumer.request;

import lombok.Data;

@Data
public class OrderTransfer {
    private String driverAccount;
    private String passengerAccount;
}
