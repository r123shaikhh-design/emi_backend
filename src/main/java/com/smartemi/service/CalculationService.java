package com.smartemi.service;

import com.smartemi.dto.*;
import com.smartemi.entity.LoanCalculation;
import com.smartemi.entity.LoanType;
import com.smartemi.entity.User;
import com.smartemi.exception.ResourceNotFoundException;
import com.smartemi.repository.LoanCalculationRepository;
import com.smartemi.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Saved-calculation use cases. Every method is scoped to the authenticated user's email. */
@Service
public class CalculationService {
    private final LoanCalculationRepository calcs;
    private final UserRepository users;
    private final EmiService emiService;

    public CalculationService(LoanCalculationRepository calcs, UserRepository users, EmiService emiService) {
        this.calcs = calcs; this.users = users; this.emiService = emiService;
    }

    @Transactional
    public CalculationResponse save(String email, EmiRequest req) {
        User user = user(email);
        EmiSummary s = emiService.calculate(req).summary();
        String name = (req.getLoanName() == null || req.getLoanName().isBlank())
                ? req.getLoanType().getLabel() + " loan" : req.getLoanName().trim();
        LoanCalculation saved = calcs.save(new LoanCalculation(user, name, req.getLoanType(), s.loanAmount(),
                s.annualRate(), s.tenureMonths(), s.monthlyEmi(), s.totalInterest(), s.totalPayment()));
        return CalculationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<CalculationResponse> search(String email, LoanType type, LocalDate from, LocalDate to, int page, int size) {
        Long uid = user(email).getId();
        Specification<LoanCalculation> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("user").get("id"), uid));
            if (type != null) p.add(cb.equal(root.get("loanType"), type));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            if (to != null) p.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(new Predicate[0]));
        };
        PageRequest pr = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return calcs.findAll(spec, pr).map(CalculationResponse::from);
    }

    @Transactional(readOnly = true)
    public CalculationResponse get(String email, Long id) {
        return CalculationResponse.from(owned(email, id));
    }

    @Transactional
    public void delete(String email, Long id) {
        calcs.delete(owned(email, id));
    }

    @Transactional(readOnly = true)
    public DashboardData dashboard(String email) {
        List<CalculationResponse> all = calcs.findByUserIdOrderByCreatedAtDesc(user(email).getId())
                .stream().map(CalculationResponse::from).toList();
        BigDecimal principal = all.stream().map(CalculationResponse::principalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interest = all.stream().map(CalculationResponse::totalInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardData(all.size(), principal, interest, all.stream().limit(5).toList());
    }

    /** Returns 404 for both "missing" and "owned by someone else" so record existence is not leaked. */
    private LoanCalculation owned(String email, Long id) {
        return calcs.findByIdAndUserId(id, user(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Calculation not found"));
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
