package com.microservice.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.microservice.report.model.Report;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserIdAndPeriod(String userId, String period);

    Page<Report> findByUserId(String userId, Pageable pageable);

    List<Report> findByUserIdAndPeriodBetweenOrderByPeriodAsc(
            String userId,
            String startPeriod,
            String endPeriod);
}
