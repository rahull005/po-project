CREATE INDEX IF NOT EXISTS idx_po_approval_case_id
    ON po_approval(po_case_id);

CREATE INDEX IF NOT EXISTS idx_po_approval_case_status
    ON po_approval(po_case_id, status);

CREATE INDEX IF NOT EXISTS idx_po_case_status
    ON po_case(status);

CREATE INDEX IF NOT EXISTS idx_po_case_updated_at
    ON po_case(updated_at);
