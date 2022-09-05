package com.catiger.logregservice.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FnLicenseRate {
    public String first;
    public String license;
    public int rate;
}
