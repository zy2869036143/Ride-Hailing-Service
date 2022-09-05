package com.catiger.rtpconsumer.inter;

import java.time.LocalDateTime;

public class PriceStrategy {
    private static final double length = 2.5;
    private static final double minutes = 7;
    private static final double normalInitPrice = 6.8;
    private static final double specialInitPrice = 7.8;
    // [ 00:00~06:00, 06:00~09:00, 09:00~17:00, 17:00~19:00, 19:00~21:00, 21:00~23:00, 23:00~00:00 ]
    private static final double[] timeZonePricePerKm = {1.40, 1.08, 1.04, 1.10, 1.08, 1.10, 1.40};
    private static final int[] hours = {6, 3, 8, 2, 2, 2, 1};
    public static double driverPrice(long minutes, double km) {
        // 每公里1.8元，每分钟0.4元
        return minutes*0.8+km*1.8;
    }

    public static double passengerPrice(long minutes, double km, LocalDateTime start, LocalDateTime end) {
        int startHour = start.getHour();
        double price = normalInitPrice;
        if((startHour>=0 && startHour<=6) || startHour==23) {
            price = specialInitPrice;
        }
        minutes -= PriceStrategy.minutes;
        km -=PriceStrategy.length;
        if(minutes <= 0 && km <= 0)
            return price;
        int endHour = end.getHour();
        int b = 0, e = 0;
        if(startHour == 23)
            b = 6;
        else if(startHour>=21)
            b = 5;
        else if(startHour>=19)
            b = 4;
        else if(startHour>=17)
            b = 3;
        else if(startHour>=9)
            b = 2;
        else if (startHour>=6)
            b = 1;
        else
            b = 0;

        if(endHour == 23)
            e = 6;
        else if(endHour>=21)
            e = 5;
        else if(endHour>=19)
            e = 4;
        else if(endHour>=17)
            e = 3;
        else if(endHour>=9)
            e = 2;
        else if (endHour>=6)
            e = 1;
        else
            e = 0;

        if(km>0)
            price += km*1.3;

        if(minutes>0)
            price += minutes*0.23;
        return price += km * 1.3;
    }
}
