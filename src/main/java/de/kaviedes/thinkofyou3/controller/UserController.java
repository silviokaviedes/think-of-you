package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.ChangePasswordRequest;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String username) {
        return authService.findByUsername(username)
                .map(u -> {
                    Map<String, String> res = new HashMap<>();
                    res.put("username", u.getUsername());
                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        try {
            authService.changePassword(auth.getName(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}
