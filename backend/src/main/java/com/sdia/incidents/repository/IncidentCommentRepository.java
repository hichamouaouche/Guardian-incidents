package com.sdia.incidents.repository;

import com.sdia.incidents.entity.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {
}
