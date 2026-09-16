package com.nastolka.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LocationTest {

    @Test
    void touchUpdatedAt_setsUpdatedAtToNow_onCreate() {
        Location location = new Location();
        assertThat(location.getUpdatedAt()).isNull();

        ReflectionTestUtils.invokeMethod(location, "touchUpdatedAt");

        assertThat(location.getUpdatedAt()).isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void touchUpdatedAt_advancesUpdatedAt_onUpdate() {
        Location location = new Location();
        ReflectionTestUtils.setField(location, "updatedAt", Instant.now().minus(1, ChronoUnit.DAYS));
        Instant previousUpdatedAt = location.getUpdatedAt();

        ReflectionTestUtils.invokeMethod(location, "touchUpdatedAt");

        assertThat(location.getUpdatedAt()).isAfter(previousUpdatedAt);
    }
}
