package com.clearinghouse;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Configuration
@EntityScan(basePackages = {"com.clearinghouse.entity"})
public class TestConfig {
    // Minimal test configuration: only entity scanning. Do NOT component-scan DAOs here
    // so we avoid instantiating DAOs that have constructor dependencies (they will be
    // imported explicitly in the tests that need them and their dependencies mocked).
}
