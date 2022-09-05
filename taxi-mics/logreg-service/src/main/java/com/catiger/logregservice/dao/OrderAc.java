package com.catiger.logregservice.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_ac")
@Data
public class OrderAc {
    @Id
    Long oid;
    @NonNull
    String daccount;
    @NonNull
    String license;
    @NonNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTime;
    @NonNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endTime;
    double dprice;
    double pprice;
    int rate;

    public OrderAc() {
    }
}
