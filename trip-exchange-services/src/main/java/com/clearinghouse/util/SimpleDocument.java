package com.clearinghouse.util;

import java.util.Map;

import lombok.Builder;

@Builder
public record SimpleDocument(String id, String content, float[] embedding, Map<String, Object> metadata) {
}
