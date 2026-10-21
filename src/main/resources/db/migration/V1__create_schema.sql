CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    DATETIME     NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE loan_calculations (
    id                   BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT        NOT NULL,
    loan_name            VARCHAR(100)  NOT NULL,
    loan_type            VARCHAR(20)   NOT NULL,
    principal_amount     DECIMAL(15,2) NOT NULL,
    annual_interest_rate DECIMAL(5,2)  NOT NULL,
    tenure_months        INT           NOT NULL,
    monthly_emi          DECIMAL(15,2) NOT NULL,
    total_interest       DECIMAL(15,2) NOT NULL,
    total_payment        DECIMAL(16,2) NOT NULL,
    created_at           DATETIME      NOT NULL,
    CONSTRAINT fk_calc_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_calc_user_created ON loan_calculations (user_id, created_at);
CREATE INDEX idx_calc_user_type ON loan_calculations (user_id, loan_type);

-- Saved comparisons: schema is ready; the current UI compares on the fly without saving.
CREATE TABLE loan_comparisons (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    comparison_name VARCHAR(100) NOT NULL,
    created_at      DATETIME     NOT NULL,
    CONSTRAINT fk_cmp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_cmp_user ON loan_comparisons (user_id);

CREATE TABLE loan_comparison_items (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    comparison_id       BIGINT NOT NULL,
    loan_calculation_id BIGINT NOT NULL,
    CONSTRAINT fk_item_cmp  FOREIGN KEY (comparison_id) REFERENCES loan_comparisons (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_calc FOREIGN KEY (loan_calculation_id) REFERENCES loan_calculations (id) ON DELETE CASCADE,
    CONSTRAINT uk_item UNIQUE (comparison_id, loan_calculation_id)
);
