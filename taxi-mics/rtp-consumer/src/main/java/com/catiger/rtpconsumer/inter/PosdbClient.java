package com.catiger.rtpconsumer.inter;

import com.catiger.rtpconsumer.request.Order;
import com.catiger.rtpconsumer.request.TimeOrder;
import com.catiger.rtpconsumer.user.FinishOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "logreg-service")
public interface PosdbClient {
    @PostMapping("/pos/save")
    String postTest(Pos pos);

    @PostMapping("/order/est")
    String postOrder(Order order);

    @PostMapping("/order/finish")
    String finishOrder(FinishOrder fo);

    @PostMapping("/order/appointment")
    String appOrder(TimeOrder timeOrder);

    @PostMapping("/order/getapp")
    TimeOrder getOrder(@RequestParam("id")long id);
}
