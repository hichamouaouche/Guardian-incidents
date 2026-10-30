package com.sdia.incidents.repository;

import com.sdia.incidents.entity.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {
}
