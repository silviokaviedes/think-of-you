package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.dto.LoginRequest;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import de.kaviedes.thinkofyou3.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_createsUserWhenAvailable() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("alice");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void register_throwsWhenUsernameExists() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User("alice", "hash")));

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void login_returnsTokenWhenValid() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtUtil.generateToken("alice")).thenReturn("token123");

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        var response = authService.login(request);

        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getToken()).isEqualTo("token123");
    }

    @Test
    void login_throwsWhenInvalidPassword() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid password");
    }
}
