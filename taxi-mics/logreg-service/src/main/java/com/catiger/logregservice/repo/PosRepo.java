package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.Pos;
import com.catiger.logregservice.dao.PosID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosRepo extends JpaRepository<Pos, PosID> {


}
