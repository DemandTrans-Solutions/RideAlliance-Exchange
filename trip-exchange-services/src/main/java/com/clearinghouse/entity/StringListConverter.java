package com.clearinghouse.entity;
/**
 *
 * @author Shankar I
 */

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        // Java 8
        if (list != null) {
            return String.join(",", list);
        }
        return null;
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if (joined != null) {
            return new ArrayList<>(Arrays.asList(joined.split(",")));
        }
        return null;
    }

}
