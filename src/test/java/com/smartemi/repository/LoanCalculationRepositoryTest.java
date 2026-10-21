package com.smartemi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartemi.entity.LoanCalculation;
import com.smartemi.entity.LoanType;
import com.smartemi.entity.User;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest
class LoanCalculationRepositoryTest {
    @Autowired UserRepository users;
    @Autowired LoanCalculationRepository calcs;

    private LoanCalculation calc(User u, String name, LoanType t) {
        return calcs.save(new LoanCalculation(u, name, t, new BigDecimal("100000.00"), new BigDecimal("12.00"), 12,
                new BigDecimal("8884.88"), new BigDecimal("6618.56"), new BigDecimal("106618.56")));
    }

    @Test
    void findByIdAndUserId_enforcesOwnership() {
        User a = users.save(new User("A", "a@x.com", "hash"));
        User b = users.save(new User("B", "b@x.com", "hash"));
        LoanCalculation c = calc(a, "Car", LoanType.CAR);
        assertThat(calcs.findByIdAndUserId(c.getId(), a.getId())).isPresent();
        assertThat(calcs.findByIdAndUserId(c.getId(), b.getId())).isEmpty();
    }

    @Test
    void filterByType_andOrdering() {
        User a = users.save(new User("A", "a@x.com", "hash"));
        calc(a, "Car", LoanType.CAR);
        calc(a, "Home", LoanType.HOME);
        Specification<LoanCalculation> spec = (r, q, cb) -> cb.and(
                cb.equal(r.get("user").get("id"), a.getId()), cb.equal(r.get("loanType"), LoanType.HOME));
        List<LoanCalculation> found = calcs.findAll(spec);
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getLoanName()).isEqualTo("Home");
        assertThat(calcs.findByUserIdOrderByCreatedAtDesc(a.getId())).hasSize(2);
    }
}
