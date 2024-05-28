package com.microservices.projectfinal.repository;

import com.microservices.projectfinal.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    Optional<BookingEntity> findByStudentIdAndAvailabilityIdContaining(String studentId, List<Long> availabilityIds);
}