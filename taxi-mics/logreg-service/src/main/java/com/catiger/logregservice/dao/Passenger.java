package com.catiger.logregservice.dao;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "passenger")
public class Passenger {
    @Id
    @Column(length = 11)
    String account;

    @NonNull
    String password;

    @Column(length = 1)
    String gender;

    String nickname;

    String realname;

    public Passenger() {}
}
