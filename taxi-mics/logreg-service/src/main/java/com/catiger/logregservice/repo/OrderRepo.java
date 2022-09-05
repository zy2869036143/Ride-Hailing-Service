package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepo extends JpaRepository<Order, String> {

    // 查询乘客的订单接口
    @Query("SELECT new com.catiger.logregservice.repo.SimpleOrder(o.id, o.time, o.splace, o.eplace) FROM Order o WHERE o.account=?1 AND (o.id in (SELECT ac.oid FROM OrderAc ac))" )
    public List<SimpleOrder> findByAccount(String account);

}
