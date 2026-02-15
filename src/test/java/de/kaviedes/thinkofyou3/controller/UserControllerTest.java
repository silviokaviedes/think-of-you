package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.security.JwtFilter;
import de.kaviedes.thinkofyou3.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void search_returnsUserWhenFound() throws Exception {
        User user = new User("alice", "hash");
        when(authService.findByUsername("alice")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/search")
                        .param("username", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void search_returnsNotFoundWhenMissing() throws Exception {
        when(authService.findByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/search")
                        .param("username", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePassword_returnsOkWhenValid() throws Exception {
        mockMvc.perform(post("/api/users/password")
                        .principal(new UsernamePasswordAuthenticationToken("alice", "n/a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old-secret\",\"newPassword\":\"new-secret\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> currentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nextCaptor = ArgumentCaptor.forClass(String.class);
        verify(authService).changePassword(usernameCaptor.capture(), currentCaptor.capture(), nextCaptor.capture());
        assertThat(usernameCaptor.getValue()).isEqualTo("alice");
        assertThat(currentCaptor.getValue()).isEqualTo("old-secret");
        assertThat(nextCaptor.getValue()).isEqualTo("new-secret");
    }

    @Test
    void changePassword_returnsBadRequestWhenValidationFails() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Current password is invalid"))
                .when(authService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/users/password")
                        .principal(new UsernamePasswordAuthenticationToken("alice", "n/a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"new-secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is invalid"));
    }
}
