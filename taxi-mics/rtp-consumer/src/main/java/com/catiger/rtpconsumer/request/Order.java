package com.catiger.rtpconsumer.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NonNull;
import org.aspectj.weaver.ast.Or;

import javax.print.attribute.standard.OrientationRequested;
import java.time.LocalDateTime;

@Data
public class Order {
    @NonNull
    private String account;
    private double slat, slon;
    private double elat, elon;
    private String splace, eplace;
    private double km;
    private double minute;
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;
    public Order() {}
}
