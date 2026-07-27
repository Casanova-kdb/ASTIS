# Sprint 11: Redis Recommendation Caching

## Goal

Reduce repeated recommendation database work when the same user opens the
Dashboard, Recommendations page, or AI Advice workflow without changing task
data.

## Problem

Sprint 10 removed repeated statistics queries inside one recommendation
request. However, separate frontend workflows can still request the same
recommendation list several times. The ranking remains unchanged until the
user changes a task, so recalculating it on every request is unnecessary.

## Scope

- Add Spring Cache and Spring Data Redis to the backend.
- Cache each user's complete ranked recommendation list.
- Use the internal user ID as the cache key instead of storing an email address.
- Set a configurable 10-minute default TTL.
- Clear the user's cache after task creation, detail update, status update, or deletion.
- Perform invalidation only after the database transaction commits.
- Continue calculating recommendations from MySQL when Redis operations fail.
- Keep automated tests independent from an external Redis process.

## Cache Design

```text
Cache name: user-recommendations
Redis prefix: astis::user-recommendations::
Key: internal user ID
Value: ranked recommendation list as JSON
Default TTL: 10 minutes
```

`RecommendationQueryService` authenticates the user and delegates ranking to a
separate cached service. The first request calculates and stores the list.
Repeated requests return the cached list without loading task statistics or
active tasks again.

## Invalidation Design

Task operations publish a `TaskChangedEvent`. A transactional event listener
removes the recommendation entry after the task transaction commits.

This covers:

- Creating a task
- Editing task details or scoring criteria
- Completing, reopening, or otherwise changing task status
- Deleting a task

Post-commit invalidation prevents a failed database transaction from removing a
valid cache entry.

## Failure Handling

Redis is an optimisation, not the source of truth. Cache read, write, eviction,
or clear failures are logged and do not fail the recommendation API. The
service calculates the current result from MySQL when no cached value can be
read.

Synchronous cache loading is not enabled because Spring's synchronous cache
path propagates Redis connection failures before the normal cache error handler
can fall back. At the current project scale, availability is more important
than preventing rare simultaneous cache misses.

The test profile uses an in-memory cache manager so CI remains reproducible
without an external Redis service. A separate JSON round-trip test verifies
that the cached recommendation DTO can be restored correctly.

## Environment Variables

```text
REDIS_ENABLED=true
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_CONNECT_TIMEOUT=2s
REDIS_COMMAND_TIMEOUT=2s
REDIS_RECOMMENDATION_TTL=10m
```

## Acceptance Criteria

- The first recommendation request calculates the ranking and stores it in cache.
- A repeated request for the same user reuses the cached list.
- Different users have different cache entries.
- A task mutation clears only the affected user's recommendation cache.
- Cache entries expire after the configured TTL.
- Redis failures do not make the recommendation API unavailable.
- Backend tests pass without requiring Redis in GitHub Actions.

## Definition of Done

- Redis cache configuration and JSON serialization are implemented.
- Recommendation calculation is isolated behind a cacheable service.
- Task changes trigger post-commit cache invalidation.
- Cache hit, invalidation, and serialization tests pass.
- Local Redis key and TTL are verified.
- README, technical stack, backlog, and changelog are updated.
