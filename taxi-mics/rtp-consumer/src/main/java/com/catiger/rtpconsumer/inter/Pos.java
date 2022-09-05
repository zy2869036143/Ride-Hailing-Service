package com.catiger.rtpconsumer.inter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pos {
    private Long oid;
    private String account;
    private double lat;
    private double lon;
}
