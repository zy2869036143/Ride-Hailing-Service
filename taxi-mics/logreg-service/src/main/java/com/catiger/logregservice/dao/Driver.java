package com.catiger.logregservice.dao;

import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "driver")
public class Driver {
    @Id
    @Column(length = 11)
    String account;
    @NonNull
    String password;
    @Column(length = 1)
    String gender;
    String nickname;
    String realname;

    @ColumnDefault("4.0")
    double rate = 4.0;

    public Driver() {
    }
}
