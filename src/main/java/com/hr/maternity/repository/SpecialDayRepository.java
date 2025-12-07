package com.hr.maternity.repository;

import com.hr.maternity.entity.SpecialDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDay, Integer> {
    long countByDateBetweenAndTypeAndIsPublicHoliday(LocalDate startDate, LocalDate endDate, Integer type, Boolean isPublicHoliday);
}
