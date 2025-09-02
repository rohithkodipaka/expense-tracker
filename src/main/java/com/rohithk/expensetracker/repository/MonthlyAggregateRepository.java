package com.rohithk.expensetracker.repository;

import com.rohithk.expensetracker.entity.MonthlyAggregate;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyAggregateRepository extends JpaRepository<MonthlyAggregate, UUID> {
    public Optional<MonthlyAggregate> findByUserIdAndYearAndMonthAndCategory(UUID userId, int year, int month, String category);
}
