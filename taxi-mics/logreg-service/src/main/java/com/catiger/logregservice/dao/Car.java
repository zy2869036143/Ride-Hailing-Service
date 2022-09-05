package com.catiger.logregservice.dao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "car")
public class Car {
    @Id
    @Column(length = 10)
    String license;
    String account;
    int type;


}
