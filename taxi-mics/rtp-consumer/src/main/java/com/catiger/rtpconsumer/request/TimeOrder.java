package com.catiger.rtpconsumer.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeOrder {
    private long id;
    private String apptime;
    private String account;
    private double slat, slon;
    private double elat, elon;
    private String splace;
    private String eplace;
    private double km;
    private double minute;
}
