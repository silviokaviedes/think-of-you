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

    /**
     * Push API controller for device token registration.
     *
     * @param pushNotificationService service for token management and FCM
     * @param authService             used to resolve authenticated user
     */
    public PushController(PushNotificationService pushNotificationService, AuthService authService) {
        this.pushNotificationService = pushNotificationService;
        this.authService = authService;
    }

    /**
     * Registers or refreshes a device token for the authenticated user.
     *
     * @param request   payload containing token and platform
     * @param principal authenticated principal
     * @return 200 OK on success
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody PushRegisterRequest request, Principal principal) {
        String username = principal.getName();
        String userId = authService.getUserIdByUsername(username);
        pushNotificationService.registerToken(userId, request.getToken(), request.getPlatform());
        return ResponseEntity.ok().build();
    }
}
