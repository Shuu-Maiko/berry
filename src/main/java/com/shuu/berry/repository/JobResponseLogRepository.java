package com.shuu.berry.repository;

import com.shuu.berry.entity.JobResponseLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobResponseLogRepository extends JpaRepository<JobResponseLog, Long> {
  List<JobResponseLog> findBySecureJobIdOrderByCreatedAtDesc(String secureJobId, Pageable pageable);
}
