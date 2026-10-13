package com.ccd.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Used in the demo to induce an error and trace it in Grafana. */
@RestController
public class DebugController {
    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    @GetMapping("/debug/error")
    public String boom() {
        log.error("Induced failure in note-service for observability demo");
        throw new IllegalStateException("Induced failure");
    }
}
