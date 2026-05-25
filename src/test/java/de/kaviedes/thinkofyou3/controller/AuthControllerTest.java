package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.AuthResponse;
import de.kaviedes.thinkofyou3.dto.LoginRequest;
import de.kaviedes.thinkofyou3.dto.RecoverPasswordRequest;
import de.kaviedes.thinkofyou3.dto.RecoveryCodeResponse;
import de.kaviedes.thinkofyou3.security.JwtFilter;
import de.kaviedes.thinkofyou3.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void register_returnsOk() throws Exception {
        when(authService.register(any(LoginRequest.class)))
                .thenReturn(new RecoveryCodeResponse("TOY-AAAAA-BBBBB-CCCCC-DDDDD", false));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret\",\"recoveryEmail\":\"alice@example.test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recoveryCode").value("TOY-AAAAA-BBBBB-CCCCC-DDDDD"))
                .andExpect(jsonPath("$.recoveryEmailSent").value(false));

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).register(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("alice");
        assertThat(captor.getValue().getRecoveryEmail()).isEqualTo("alice@example.test");
    }

    @Test
    void register_returnsBadRequestWhenValidationFails() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Username already exists"))
                .when(authService).register(any(LoginRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void login_returnsTokenAndUsername() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("token123", "alice"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void refresh_returnsRotatedTokens() throws Exception {
        when(authService.refresh(eq("refresh123")))
                .thenReturn(new AuthResponse("token456", "alice", "refresh456"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token456"))
                .andExpect(jsonPath("$.refreshToken").value("refresh456"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void recoverPassword_returnsNewRecoveryCode() throws Exception {
        when(authService.recoverPassword(any(RecoverPasswordRequest.class)))
                .thenReturn(new RecoveryCodeResponse("TOY-FFFFF-GGGGG-HHHHH-JJJJJ", false));

        mockMvc.perform(post("/api/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"alice",
                                  "recoveryCode":"TOY-AAAAA-BBBBB-CCCCC-DDDDD",
                                  "newPassword":"new-secret",
                                  "confirmPassword":"new-secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recoveryCode").value("TOY-FFFFF-GGGGG-HHHHH-JJJJJ"));
    }
}
