package com.catiger.logregservice.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "dporder")
@Data
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private String account;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    private double slat, slon;
    private double elat, elon;
    private String splace;
    private String eplace;
    private double km;
    private double minute;
    public Order() {}
}