package com.astis.studyplan.model;

import com.astis.settings.entity.DailyStudyCapacity;
import java.math.BigDecimal;

public final class DailyCapacityPolicy {

    private static final BigDecimal LIGHT_HOURS = new BigDecimal("1.50");
    private static final BigDecimal MEDIUM_HOURS = new BigDecimal("3.00");
    private static final BigDecimal HEAVY_HOURS = new BigDecimal("5.00");

    private DailyCapacityPolicy() {
    }

    public static BigDecimal hoursFor(DailyStudyCapacity capacity) {
        DailyStudyCapacity effectiveCapacity = capacity == null
                ? DailyStudyCapacity.MEDIUM
                : capacity;

        return switch (effectiveCapacity) {
            case LIGHT -> LIGHT_HOURS;
            case MEDIUM -> MEDIUM_HOURS;
            case HEAVY -> HEAVY_HOURS;
        };
    }
}
