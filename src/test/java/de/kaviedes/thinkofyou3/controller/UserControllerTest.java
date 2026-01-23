package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.security.JwtFilter;
import de.kaviedes.thinkofyou3.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
