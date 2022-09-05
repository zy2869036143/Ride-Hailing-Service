package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.OrderAc;
import com.catiger.logregservice.res.CountPrice;
import com.catiger.logregservice.res.LatLon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Transactional
public interface OrderAcRepo extends JpaRepository<OrderAc, Long> {

    @Query("SELECT new com.catiger.logregservice.res.LatLon(s.lat, s.lon) FROM OrderAc c, Pos s WHERE c.oid=s.oid  AND c.oid=?1")
    List<LatLon> findTraceByOid(Long oid);

    @Query("SELECT new com.catiger.logregservice.res.CountPrice(count(o.oid), sum(o.dprice)) FROM OrderAc o WHERE o.daccount=?1 AND o.endTime between ?2 and ?3")
    CountPrice findTotalPriceOfDriver(String account, LocalDateTime begin, LocalDateTime end);

    @Query("SELECT new com.catiger.logregservice.repo.SimpleOrder(od.id, od.time, od.splace, od.eplace) FROM OrderAc oac, Order od WHERE oac.daccount=?1 AND oac.oid=od.id")
    List<SimpleOrder> findDriverOrder(String account);
    @Override
    Optional<OrderAc> findById(Long aLong);

    @Query("SELECT new com.catiger.logregservice.res.FnLicenseRate(substring(d.realname,1,1), oac.license, oac.rate) FROM OrderAc oac, Driver d WHERE oac.oid=?1 AND oac.daccount=d.account")
    Optional<OrderAc> findFirstNameLicense(Long oid);

    @Modifying
    @Query("update OrderAc o set o.rate=?2 WHERE o.oid=?1")
    int updateRate(Long oid, int rate);
}
