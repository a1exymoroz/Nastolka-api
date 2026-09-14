package com.nastolka.repository.projection;

public interface ExpansionCountProjection {

    Long getExpansionId();

    String getExpansionName();

    Long getUseCount();
}
