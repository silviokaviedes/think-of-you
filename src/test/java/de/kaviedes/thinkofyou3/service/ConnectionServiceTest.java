package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.Mood;
import de.kaviedes.thinkofyou3.model.ThoughtEvent;
import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.ConnectionRepository;
import de.kaviedes.thinkofyou3.repository.ThoughtEventRepository;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ThoughtEventRepository thoughtEventRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ConnectionService connectionService;

    @Test
    void requestConnection_throwsWhenSelf() {
        User user = new User("alice", "hash");
        user.setId("u1");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> connectionService.requestConnection("alice", "alice"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot connect to yourself");
    }

    @Test
    void thinkOfPartner_incrementsCount_andSendsNotification() {
        User requester = new User("alice", "hash");
        requester.setId("u1");
        User recipient = new User("bob", "hash");
        recipient.setId("u2");

        Connection connection = new Connection("u1", "u2");
        connection.setId("c1");
        connection.setStatus(Connection.Status.ACCEPTED);

        when(connectionRepository.findById("c1")).thenReturn(Optional.of(connection));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(requester));
        when(userRepository.findById("u2")).thenReturn(Optional.of(recipient));
        when(thoughtEventRepository.save(any(ThoughtEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        connectionService.thinkOfPartner("c1", "alice", "happy");

        ArgumentCaptor<Connection> connectionCaptor = ArgumentCaptor.forClass(Connection.class);
        verify(connectionRepository).save(connectionCaptor.capture());
        assertThat(connectionCaptor.getValue().getRequesterToRecipientCount()).isEqualTo(1);

        verify(thoughtEventRepository).save(any(ThoughtEvent.class));
        verify(messagingTemplate).convertAndSend(contains("/topic/updates/bob"), contains("\"mood\":\"happy\""));
        verify(messagingTemplate).convertAndSend("/topic/updates/alice", "refresh");
    }
}
