CREATE TABLE po_case (
    id BIGSERIAL PRIMARY KEY,

    case_id VARCHAR(50) NOT NULL UNIQUE,

    request_type VARCHAR(30) NOT NULL,

    channel VARCHAR(30) NOT NULL,

    department VARCHAR(50) NOT NULL,

    purpose VARCHAR(100) NOT NULL,

    amount NUMERIC(19, 4) NOT NULL,

    currency VARCHAR(3) NOT NULL,

    debit_account VARCHAR(50) NOT NULL,

    delivery_type VARCHAR(30) NOT NULL,

    pay_order_required BOOLEAN NOT NULL,

    status VARCHAR(40) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    version BIGINT NOT NULL DEFAULT 0
);