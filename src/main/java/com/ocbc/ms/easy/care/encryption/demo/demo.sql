CREATE TABLE demo_encrypted_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plain_keyword VARCHAR(120) NOT NULL,
    national_id_enc TEXT NOT NULL,
    monthly_salary_enc TEXT NOT NULL,
    hire_date_enc TEXT NOT NULL,
    child_count_enc TEXT NOT NULL,
    has_allowance_enc TEXT NOT NULL,
    retirement_account_enc TEXT NOT NULL,
    new_account_enc TEXT NOT NULL
);