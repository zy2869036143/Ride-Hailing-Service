package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.AppointmentOrder;
import com.catiger.logregservice.dao.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface AppOrderRepo extends JpaRepository<AppointmentOrder, Long> {
    @Modifying
    @Query("update AppointmentOrder o set o.appdriver=?2 WHERE o.id=?1")
    int updateRate(Long oid, String driver);
    @Query("SELECT new com.catiger.logregservice.repo.SimpleOrder(od.id, od.apptime, od.splace, od.eplace) FROM AppointmentOrder od " +
            "WHERE od.account=?1 AND od.id NOT IN (SELECT o.oid FROM OrderAc o)")
    List<SimpleOrder> findByAccount(String account);

    @Query("SELECT new com.catiger.logregservice.repo.SimpleOrder(od.id, od.apptime, od.splace, od.eplace) FROM AppointmentOrder od " +
            "WHERE od.account=?1 AND od.id IN (SELECT o.oid FROM OrderAc o)")
    List<SimpleOrder> findByAccountFinish(String account);
    @Query("SELECT new com.catiger.logregservice.repo.SimpleOrder(od.id, od.apptime, od.splace, od.eplace) FROM AppointmentOrder od " +
            "WHERE od.appdriver=?1 AND od.id NOT IN (SELECT o.oid FROM OrderAc o  WHERE o.daccount=?1)" )
    List<SimpleOrder> findByDriver(String account);

    @Query("SELECT new com.catiger.logregservice.repo.SimpleOrder(od.id, od.apptime, od.splace, od.eplace) FROM AppointmentOrder od " +
            "WHERE od.appdriver=?1 AND od.id IN (SELECT o.oid FROM OrderAc o  WHERE o.daccount=?1)" )
    List<SimpleOrder> findByDriverFinished(String account);
    void deleteById(long id);
}
