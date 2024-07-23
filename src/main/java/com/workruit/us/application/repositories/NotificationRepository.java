package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Notification;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface NotificationRepository extends PagingAndSortingRepository<Notification, Long> {
}
