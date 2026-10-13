package com.ccd.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummarizerServiceTest {
    @Test
    void returnsMockSummaryWhenNoApiKey() {
        SummarizerService s = new SummarizerService("", "any-model");
        assertTrue(s.summarize("hello world").startsWith("[mock summary]"));
    }
}
