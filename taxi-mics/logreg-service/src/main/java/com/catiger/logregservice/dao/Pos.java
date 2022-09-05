package com.catiger.logregservice.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "pos")
@AllArgsConstructor
@NoArgsConstructor
@IdClass(PosID.class)
public class Pos {
    @Id
    private Long oid;
    @Id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    @NotNull
    private String account;
    @NonNull
    private double lat;
    @NonNull
    private double lon;
    @ManyToOne
    private OrderAc orderAc;
}
