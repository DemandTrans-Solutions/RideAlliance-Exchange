package com.clearinghouse.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BooleanConverter implements AttributeConverter<Boolean, Boolean> {
    @Override
    public Boolean convertToDatabaseColumn(Boolean attribute) {
        return attribute;
    }

    @Override
    public Boolean convertToEntityAttribute(Boolean dbData) {
        return dbData == null ? Boolean.FALSE : dbData;
    }
}