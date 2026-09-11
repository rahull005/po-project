ALTER TABLE po_case
    ADD COLUMN po_number VARCHAR(100);

CREATE UNIQUE INDEX uk_po_case_po_number
    ON po_case(po_number)
    WHERE po_number IS NOT NULL;

ALTER TABLE po_case
    ADD COLUMN flex_request_id VARCHAR(100);