package com.catiger.logregservice.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@NonNull
@Data
public class LatLon {
    private double lat, lon;
}
