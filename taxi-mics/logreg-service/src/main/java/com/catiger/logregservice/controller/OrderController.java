package com.catiger.logregservice.controller;

import com.catiger.logregservice.dao.AppointmentOrder;
import com.catiger.logregservice.dao.Order;
import com.catiger.logregservice.dao.OrderAc;
import com.catiger.logregservice.repo.AppOrderRepo;
import com.catiger.logregservice.repo.OrderAcRepo;
import com.catiger.logregservice.repo.OrderRepo;
import com.catiger.logregservice.repo.SimpleOrder;
import com.catiger.logregservice.res.CountPrice;
import com.catiger.logregservice.res.Response;
import com.catiger.logregservice.res.ResponseBrief;
import org.apache.http.params.SyncBasicHttpParams;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderRepo orderRepo;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OrderAcRepo orderAcRepo;
    @Autowired
    AppOrderRepo appOrderRepo;

    @PostMapping("/est")
    public ResponseEntity<?> establishOrder(@RequestBody Order order) {
        order.setTime(LocalDateTime.now());
        orderRepo.save(order);
        orderRepo.flush();
        return ResponseEntity.ok(new Response(200, "订单已创建", order.getId()));
    }

    @PostMapping("/check")
    public ResponseEntity<?> searchOrder(@RequestParam("account") String account) {
        List<SimpleOrder> list1 = appOrderRepo.findByAccountFinish(account);
        List<SimpleOrder> list2 = orderRepo.findByAccount(account);
        list1.addAll(list2);
        return ResponseEntity.ok(new Response(200, "查询成功",list1 ));
    }

    @PostMapping("/checkapp")
    public ResponseEntity<?> searchAppOrder(@RequestParam("account") String account) {
        return ResponseEntity.ok(new Response(200, "0001", appOrderRepo.findByAccount(account)));
    }

    @PostMapping("/finish")
    public ResponseEntity<?> finishedOrder(@RequestBody @Validated OrderAc orderAc) {
        logger.info("OrderAc: oid" + orderAc.getOid());
        orderAcRepo.save(orderAc);
        return ResponseEntity.ok(new ResponseBrief(200, "已保存成功"));
    }

    @PostMapping("/trace")
    public ResponseEntity<?> trace(@RequestParam(name = "oid") long oid) {
        return ResponseEntity.ok(new ResponseBrief(200, "请求轨迹成功", orderAcRepo.findTraceByOid(oid)));
    }

    @PostMapping("/setRate")
    public ResponseEntity<?> updateOrder(@RequestParam("oid") Long id, @RequestParam("rate") int rate) {
        orderAcRepo.updateRate(id, rate);
        return ResponseEntity.ok(new Response(200, "订单状态已更新"));
    }

    @PostMapping("/end")
    public ResponseEntity<?> finished(@RequestParam("oid") Long id) {
        return ResponseEntity.ok(new Response(200, "订单已完成"));
    }
    @PostMapping("/dprice")
    public ResponseEntity<?> totalPricePerDay(@RequestParam("account") String account) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime begin = LocalDate.now().plusDays(0).atStartOfDay();
        CountPrice result = orderAcRepo.findTotalPriceOfDriver(account, begin, end);
        return ResponseEntity.ok(new ResponseBrief(200, "已返回今日收入", result));
    }
    @PostMapping("/dorder")
    public ResponseEntity<?> driverOrder(@RequestParam("account") String account) {
        List<SimpleOrder> list1 = appOrderRepo.findByDriverFinished(account);
        List<SimpleOrder> list2 = orderAcRepo.findDriverOrder(account);
        list1.addAll(list2);
        return ResponseEntity.ok(new Response(200, "查询成功", list1));
    }

    @PostMapping("/dapporder")
    public ResponseEntity<?> driverAppOrder(@RequestParam("account") String account) {
        logger.info("查询司机预约单");
        return ResponseEntity.ok(new Response(200, "0001", appOrderRepo.findByDriver(account)));
    }

    @PostMapping("/detailOrder")
    public ResponseEntity<?> orderDetail(@RequestParam("oid") long oid) {
        return ResponseEntity.ok(new ResponseBrief(200, "查询成功", orderAcRepo.findById(oid)));
    }

    @PostMapping("/briefHis")
    public ResponseEntity<?> briefHis(@RequestParam("oid") long oid) {
        return ResponseEntity.ok(new ResponseBrief(200, "查询成功", orderAcRepo.findFirstNameLicense(oid)));
    }

    @PostMapping("/appointment")
    public ResponseEntity<?> briefHis(@RequestBody AppointmentOrder order) {
        appOrderRepo.save(order);
        return ResponseEntity.ok(new ResponseBrief(200, "成功", order.getId()));
    }

    @PostMapping("/getapp")
    public ResponseEntity<?> getApp(@RequestParam("id") long id) {
        AppointmentOrder app = appOrderRepo.findById(id).get();
        return ResponseEntity.ok(app);
    }

    @PostMapping("/appapp")
    public ResponseEntity<?> getApp(@RequestParam("oid") long oid, @RequestParam("account") String acc) {
        appOrderRepo.updateRate(oid, acc);
        return ResponseEntity.ok(new ResponseBrief(200, "已接受"));
    }

}
