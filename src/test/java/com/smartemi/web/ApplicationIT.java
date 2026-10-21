package com.smartemi.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartemi.entity.User;
import com.smartemi.repository.LoanCalculationRepository;
import com.smartemi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIT {
    private static final String LOAN = "{\"loanAmount\":100000,\"annualRate\":12,\"tenure\":12,\"tenureUnit\":\"MONTHS\",\"loanType\":\"PERSONAL\",\"loanName\":\"Test\"}";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired LoanCalculationRepository calcs;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper om;

    @BeforeEach
    void setUp() {
        calcs.deleteAll();
        users.deleteAll();
        users.save(new User("Alice", "alice@test.com", encoder.encode("Password1")));
        users.save(new User("Bob", "bob@test.com", encoder.encode("Password1")));
    }

    @Test
    void calculate_returnsKnownEmi() throws Exception {
        mvc.perform(post("/api/emi/calculate").contentType(MediaType.APPLICATION_JSON).content(LOAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEmi").value(8884.88));
    }

    @Test
    void calculate_rejectsNegativeAmount() throws Exception {
        String bad = LOAN.replace("100000", "-5");
        mvc.perform(post("/api/emi/calculate").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.loanAmount").exists());
    }

    @Test
    void register_thenDuplicateIsConflict() throws Exception {
        String body = "{\"fullName\":\"Carol\",\"email\":\"carol@test.com\",\"password\":\"Password1\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        org.assertj.core.api.Assertions.assertThat(users.findByEmail("carol@test.com").orElseThrow().getPasswordHash())
                .startsWith("$2"); // BCrypt hash, never plain text
    }

    @Test
    void login_succeedsWithValidAndFailsWithInvalidCredentials() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@test.com\",\"password\":\"Password1\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@test.com\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void savedCalculations_requireAuthentication() throws Exception {
        mvc.perform(get("/api/calculations")).andExpect(status().isUnauthorized());
    }

    @Test
    void user_cannotReadOrDeleteAnotherUsersCalculation() throws Exception {
        String res = mvc.perform(post("/api/calculations").with(user("alice@test.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(LOAN))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = om.readTree(res).get("id").asLong();

        mvc.perform(get("/api/calculations/" + id).with(user("alice@test.com"))).andExpect(status().isOk());
        mvc.perform(get("/api/calculations/" + id).with(user("bob@test.com"))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/calculations/" + id).with(user("bob@test.com")).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(delete("/api/calculations/" + id).with(user("alice@test.com")).with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    void meta_isPublicAndListsLoanTypes() throws Exception {
        mvc.perform(get("/api/meta")).andExpect(status().isOk())
                .andExpect(jsonPath("$.loanTypes.length()").value(6))
                .andExpect(jsonPath("$.loanTypes[0].code").value("HOME"));
    }

    @Test
    void full_returnsSummaryExplanationAndSchedule() throws Exception {
        mvc.perform(post("/api/emi/full").contentType(MediaType.APPLICATION_JSON).content(LOAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.monthlyEmi").value(8884.88))
                .andExpect(jsonPath("$.explanation.months").value(12))
                .andExpect(jsonPath("$.schedule.length()").value(12));
    }

    @Test
    void me_requiresLogin() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").with(user("alice@test.com"))).andExpect(status().isOk());
    }

    @Test
    void dashboard_requiresLogin() throws Exception {
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard").with(user("alice@test.com"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
}
