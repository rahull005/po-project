# Pay-Order Audit / Corrections

## Scope
Reviewed the uploaded Pay-Order project before continuing implementation.
Current services remain:
- `PoApplication` (8080)
- `fakeflex` (8081)

No Docker was introduced.

## Critical corrections made

1. Removed the duplicated/legacy Flex integration path.
   - Deleted the old `FlexClient` implementation.
   - Deleted the duplicate `FlexConfig` WebClient bean.
   - Removed legacy `pocase.dto.FlexPORequest/FlexPOResponse`.
   - Removed empty `FlexService` and `FlexExceptionHandler` placeholders.
   - `POProcessingService` now depends on the `FlexGateway` port and domain DTOs.

2. Fixed a compile-breaking repository/API mismatch.
   - The outbox processor was calling `findByEventId(Long)` while the repository also had a UUID event identifier.
   - Processing now uses the entity primary key via `findById(Long)`.

3. Completed the outbox result API.
   - Added explicit handling for completed, business failure, reconciliation-required, and retryable failures.

4. Added bounded asynchronous processing correctly.
   - `@Async("outboxTaskExecutor")` is used only after a durable outbox claim.
   - Executor uses bounded pool/queue, graceful shutdown, and caller-runs backpressure.
   - MDC task decoration preserves correlation context across async threads.

5. Added production-oriented correlation logging.
   - Incoming requests receive/propagate a correlation ID.
   - Async outbox processing gets an `OUTBOX-<eventId>` correlation ID when no request context exists.
   - Console logging includes correlation IDs.
   - Account/token/credential values are not logged by the current code path.

6. Corrected Flex idempotency scope.
   - Idempotency key is now tied to the approval attempt:
     `<caseId>:FLEX:CREATE_PO:APPROVAL:<approvalId>`
   - Retries of the same approval reuse the key.
   - A repaired/resubmitted case creates a new approval and therefore a new Flex operation key.

7. Hardened the Fake Flex service.
   - Idempotency is keyed by `X-Idempotency-Key`.
   - Timeout simulation creates the Flex result first, then delays the response, allowing status inquiry to discover the successful operation.
   - Business failure, system failure, timeout, and duplicate-request scenarios remain available.

8. Added consistent REST error responses.
   - Validation, missing headers, malformed bodies, not-found, conflicts, and unexpected errors return a structured `ApiError` containing the correlation ID.

9. Removed the hard-coded database password from configuration.
   - `DB_PASSWORD` must be supplied through the environment/IDE run configuration.

10. Added workflow indexes in `V7__add_workflow_indexes.sql`.

## Important architectural decisions retained

- Transactional outbox remains the reliability boundary.
- Business/application code depends on ports (for example `FlexGateway`), not `WebClient`.
- HTTP details stay in infrastructure adapters.
- External HTTP calls are not held inside the approval database transaction.
- PostgreSQL `FOR UPDATE SKIP LOCKED` is used for outbox claiming.
- Eureka/service discovery is intentionally not added yet because only two services currently exist and there is no dynamic multi-instance discovery requirement.

## Verification limitation

A full Maven compile/test run could not be executed in this audit environment because the Maven wrapper needed to download Maven 3.9.16 and external Maven Central access was unavailable. Static verification was performed instead:
- XML validation for both POM files.
- Internal import/package consistency check: no missing internal imports.
- Legacy Flex references removed.
- Duplicate WebClient bean removed.
- Repository/result-service call sites reconciled.
- Source/migration structure reviewed.

Run `./mvnw test` in the user's local environment for the final compiler/test verification before committing these corrections.
