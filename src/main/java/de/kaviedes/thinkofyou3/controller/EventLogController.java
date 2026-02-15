package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.EventLogItemDTO;
import de.kaviedes.thinkofyou3.service.EventLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventLogController {
    private final EventLogService eventLogService;

    public EventLogController(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    @GetMapping
    public ResponseEntity<?> getEvents(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false, defaultValue = "all") String direction,
            @RequestParam(required = false) String connectionId,
            Authentication auth) {
        try {
            List<EventLogItemDTO> events = eventLogService.getEvents(auth.getName(), limit, direction, connectionId);
            return ResponseEntity.ok(events);
        } catch (RuntimeException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}
