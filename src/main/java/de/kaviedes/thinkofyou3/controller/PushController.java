package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.PushRegisterRequest;
import de.kaviedes.thinkofyou3.service.PushNotificationService;
import de.kaviedes.thinkofyou3.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/push")
public class PushController {
    private final PushNotificationService pushNotificationService;
    private final AuthService authService;

    public PushController(PushNotificationService pushNotificationService, AuthService authService) {
        this.pushNotificationService = pushNotificationService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody PushRegisterRequest request, Principal principal) {
        String username = principal.getName();
        String userId = authService.getUserIdByUsername(username);
        pushNotificationService.registerToken(userId, request.getToken(), request.getPlatform());
        return ResponseEntity.ok().build();
    }
}
