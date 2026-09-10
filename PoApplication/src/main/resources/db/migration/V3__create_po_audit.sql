CREATE TABLE po_audit (

    id BIGSERIAL PRIMARY KEY,

    po_case_id BIGINT NOT NULL,

    action VARCHAR(50) NOT NULL,

    old_status VARCHAR(50),

    new_status VARCHAR(50),

    performed_by VARCHAR(100) NOT NULL,

    remarks VARCHAR(1000),

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_po_audit_case
        FOREIGN KEY (po_case_id)
        REFERENCES po_case(id)
);