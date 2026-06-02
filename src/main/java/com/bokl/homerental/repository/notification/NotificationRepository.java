package com.bokl.homerental.repository.notification;

import com.bokl.homerental.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
