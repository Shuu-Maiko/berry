package com.shuu.berry.repository;

import com.shuu.berry.entity.Job;
import com.shuu.berry.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
  List<Job> findByUser(User user);

  Optional<Job> findBySecureJobId(String secureJobId);
}
