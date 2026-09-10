CREATE TABLE po_approval (

    id BIGSERIAL PRIMARY KEY,

    po_case_id BIGINT NOT NULL,

    maker_id VARCHAR(100) NOT NULL,

    checker_id VARCHAR(100),

    status VARCHAR(30) NOT NULL,

    comments VARCHAR(1000),

    created_at TIMESTAMP NOT NULL,

    action_at TIMESTAMP,

    CONSTRAINT fk_po_approval_case
        FOREIGN KEY (po_case_id)
        REFERENCES po_case(id)
);