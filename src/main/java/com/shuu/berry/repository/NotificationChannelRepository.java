package com.shuu.berry.repository;

import com.shuu.berry.entity.NotificationChannel;
import com.shuu.berry.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
  List<NotificationChannel> findByUser(User user);
}
