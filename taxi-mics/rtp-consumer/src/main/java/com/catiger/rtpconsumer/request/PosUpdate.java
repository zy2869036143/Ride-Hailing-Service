package com.catiger.rtpconsumer.request;

import lombok.Data;

@Data
public class PosUpdate {
    private double lat;
    private double lon;
    private String account;
}
