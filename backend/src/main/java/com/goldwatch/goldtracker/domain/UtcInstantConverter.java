package com.goldwatch.goldtracker.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Stores an {@link Instant} in a MySQL DATETIME column as UTC wall-clock time.
 *
 * <p>Binding a {@link LocalDateTime} avoids {@code java.util.Calendar} entirely, so the value is correct regardless
 * of the JVM time zone or locale (with a Thai FORMAT locale, Calendar-based binding writes Buddhist-era years).
 */
@Converter
public class UtcInstantConverter implements AttributeConverter<Instant, LocalDateTime> {

    @Override
    public LocalDateTime convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Override
    public Instant convertToEntityAttribute(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
