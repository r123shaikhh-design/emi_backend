package com.smartemi.config;

import com.smartemi.dto.EmiRequest;
import com.smartemi.dto.RegisterRequest;
import com.smartemi.dto.TenureUnit;
import com.smartemi.entity.LoanType;
import com.smartemi.repository.UserRepository;
import com.smartemi.service.CalculationService;
import com.smartemi.service.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Seeds a demo account and sample calculations for local development (profile "dev" only). */
@Configuration
@Profile("dev")
public class DataInitializer {
    public static final String DEMO_EMAIL = "demo@smartemi.local";
    public static final String DEMO_PASSWORD = "Demo@1234";

    @Bean
    ApplicationRunner seedData(UserRepository users, UserService userService, CalculationService calcs) {
        return args -> {
            if (users.existsByEmail(DEMO_EMAIL)) return;
            RegisterRequest r = new RegisterRequest();
            r.setFullName("Demo User"); r.setEmail(DEMO_EMAIL); r.setPassword(DEMO_PASSWORD);
            userService.register(r);
            seed(calcs, "Dream home", "5000000", "8.5", 20, LoanType.HOME);
            seed(calcs, "Hatchback", "800000", "9.25", 5, LoanType.CAR);
            seed(calcs, "Masters abroad", "2500000", "10.5", 10, LoanType.EDUCATION);
        };
    }

    private void seed(CalculationService calcs, String name, String amount, String rate, int years, LoanType type) {
        EmiRequest req = EmiRequest.of(amount, rate, years, TenureUnit.YEARS, type);
        req.setLoanName(name);
        calcs.save(DEMO_EMAIL, req);
    }
}
