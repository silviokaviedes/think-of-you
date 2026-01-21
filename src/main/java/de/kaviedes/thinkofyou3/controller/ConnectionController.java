package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.ConnectionDTO;
import de.kaviedes.thinkofyou3.dto.ThinkRequest;
import de.kaviedes.thinkofyou3.service.ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody Map<String, String> body, Authentication auth) {
        connectionService.requestConnection(auth.getName(), body.get("username"));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDTO>> getAccepted(Authentication auth) {
        return ResponseEntity.ok(connectionService.getAcceptedConnections(auth.getName()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ConnectionDTO>> getRequests(Authentication auth) {
        return ResponseEntity.ok(connectionService.getPendingRequests(auth.getName()));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable String id, Authentication auth) {
        connectionService.acceptConnection(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id, Authentication auth) {
        connectionService.rejectConnection(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/think")
    public ResponseEntity<?> think(@PathVariable String id, @RequestBody ThinkRequest request, Authentication auth) {
        connectionService.thinkOfPartner(id, auth.getName(), request.getMood());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication auth) {
        connectionService.deleteConnection(id, auth.getName());
        return ResponseEntity.ok().build();
    }
}
