package com.example.po.integration.entity;

public enum OutboxStatus {
    NEW,
    PROCESSING,
    COMPLETED,
    RETRY,
    RECONCILIATION_REQUIRED
}
