package com.smartemi.repository;

import com.smartemi.entity.LoanCalculation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanCalculationRepository
        extends JpaRepository<LoanCalculation, Long>, JpaSpecificationExecutor<LoanCalculation> {
    /** Ownership-aware lookup: returns empty if the record belongs to another user. */
    Optional<LoanCalculation> findByIdAndUserId(Long id, Long userId);
    List<LoanCalculation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
