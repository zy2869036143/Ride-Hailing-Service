package com.catiger.logregservice.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Generated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Table(name = "appoint")
public class AppointmentOrder {
    @Id
    @GeneratedValue
    private long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime apptime;
    private String account;
    private double slat, slon;
    private double elat, elon;
    private String splace;
    private String eplace;
    private double km;
    private String appdriver;
    private double minute;
}
