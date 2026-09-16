CREATE TABLE integration_transaction (

                 id BIGSERIAL PRIMARY KEY,

                 case_id VARCHAR(50) NOT NULL,

                 system_name VARCHAR(50) NOT NULL,

                 operation VARCHAR(100) NOT NULL,

                 request_id VARCHAR(100) NOT NULL UNIQUE,

                 idempotency_key VARCHAR(150) NOT NULL UNIQUE,

                 status VARCHAR(50) NOT NULL,

                 retry_count INTEGER NOT NULL DEFAULT 0,

                 error_code VARCHAR(100),

                 error_message VARCHAR(2000),

                 created_at TIMESTAMP NOT NULL,

                 updated_at TIMESTAMP NOT NULL,

                 completed_at TIMESTAMP
);

CREATE INDEX idx_integration_case_id
    ON integration_transaction(case_id);

CREATE INDEX idx_integration_status
    ON integration_transaction(status);