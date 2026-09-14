package com.nastolka.repository.projection;

import java.time.Instant;

public interface SessionTimingProjection {

    Instant getStartedAt();

    Instant getFinishedAt();

    Instant getPlayedAt();
}
