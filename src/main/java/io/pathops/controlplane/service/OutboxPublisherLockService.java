package io.pathops.controlplane.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxPublisherLockService {

    private static final long OUTBOX_PUBLISHER_LOCK_ID = 901202604260001L;

    private final JdbcTemplate jdbcTemplate;

    public boolean tryLock() {
        Boolean locked = jdbcTemplate.queryForObject(
            "select pg_try_advisory_lock(?)",
            Boolean.class,
            OUTBOX_PUBLISHER_LOCK_ID
        );
        return Boolean.TRUE.equals(locked);
    }

    public void unlock() {
        jdbcTemplate.queryForObject(
            "select pg_advisory_unlock(?)",
            Boolean.class,
            OUTBOX_PUBLISHER_LOCK_ID
        );
    }
}