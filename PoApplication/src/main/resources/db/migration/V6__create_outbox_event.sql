CREATE TABLE outbox_event (

              id BIGSERIAL PRIMARY KEY,

              event_id UUID NOT NULL UNIQUE,

              event_type VARCHAR(100) NOT NULL,

              aggregate_type VARCHAR(100) NOT NULL,

              aggregate_id VARCHAR(100) NOT NULL,

              payload TEXT NOT NULL,

              status VARCHAR(50) NOT NULL,

              attempts INTEGER NOT NULL DEFAULT 0,

              available_at TIMESTAMP NOT NULL,

              locked_at TIMESTAMP,

              last_error TEXT,

              created_at TIMESTAMP NOT NULL,

              updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_outbox_status_available
    ON outbox_event(status, available_at);

CREATE INDEX idx_outbox_aggregate
    ON outbox_event(aggregate_id);