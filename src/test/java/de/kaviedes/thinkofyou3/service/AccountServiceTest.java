package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.DeviceTokenRepository;
import de.kaviedes.thinkofyou3.repository.RefreshTokenRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private ThoughtEventRepository thoughtEventRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AccountService accountService;

    @Test
    void deleteAccount_removesUserRelatedDataAndNotifiesPartners() {
        User user = new User("alice", "hash");
        user.setId("u1");
        User partner = new User("bob", "other-hash");
        partner.setId("u2");

        Connection accepted = new Connection("u1", "u2");
        accepted.setId("c1");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(connectionRepository.findByRequesterIdOrRecipientId("u1", "u1")).thenReturn(List.of(accepted));
        when(userRepository.findById("u2")).thenReturn(Optional.of(partner));

        accountService.deleteAccount("alice", "secret");

        verify(thoughtEventRepository).deleteByConnectionIdIn(List.of("c1"));
        verify(thoughtEventRepository).deleteBySenderIdOrRecipientId("u1", "u1");
        verify(connectionRepository).deleteAll(List.of(accepted));
        verify(refreshTokenRepository).deleteByUserId("u1");
        verify(deviceTokenRepository).deleteByUserId("u1");
        verify(userRepository).delete(user);
        verify(messagingTemplate).convertAndSend("/topic/updates/bob", "refresh");
    }

    @Test
    void deleteAccount_throwsWhenPasswordInvalid() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-secret", "hash")).thenReturn(false);

        assertThatThrownBy(() -> accountService.deleteAccount("alice", "wrong-secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Current password is invalid");
    }
}
