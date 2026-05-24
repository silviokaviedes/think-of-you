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

import java.util.List;
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

    @Mock
    private PushNotificationService pushNotificationService;

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
        requester.setBodyEnergy(25);
        requester.setMindEnergy(60);
        requester.setHeartEnergy(85);
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

        ArgumentCaptor<ThoughtEvent> eventCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(thoughtEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getBodyEnergy()).isEqualTo(25);
        assertThat(eventCaptor.getValue().getMindEnergy()).isEqualTo(60);
        assertThat(eventCaptor.getValue().getHeartEnergy()).isEqualTo(85);
        verify(messagingTemplate).convertAndSend(contains("/topic/updates/bob"), contains("\"mood\":\"happy\""));
        verify(messagingTemplate).convertAndSend(contains("/topic/updates/bob"), contains("\"energy\":{\"body\":25,\"mind\":60,\"heart\":85}"));
        verify(messagingTemplate).convertAndSend("/topic/updates/alice", "refresh");
        verify(pushNotificationService).sendThoughtNotification("u2", "alice", Mood.HAPPY);
    }

    @Test
    void thinkOfPartner_defaultsEnergyForOlderUsers() {
        User requester = new User("alice", "hash");
        requester.setId("u1");
        requester.setBodyEnergy(null);
        requester.setMindEnergy(null);
        requester.setHeartEnergy(null);
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

        ArgumentCaptor<ThoughtEvent> eventCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(thoughtEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getBodyEnergy()).isEqualTo(50);
        assertThat(eventCaptor.getValue().getMindEnergy()).isEqualTo(50);
        assertThat(eventCaptor.getValue().getHeartEnergy()).isEqualTo(50);
        verify(userRepository).save(requester);
    }

    @Test
    void getAcceptedConnections_includesEnergySnapshotsWhenPresent() {
        User user = new User("alice", "hash");
        user.setId("u1");
        User partner = new User("bob", "hash");
        partner.setId("u2");

        Connection connection = new Connection("u1", "u2");
        connection.setId("c1");
        connection.setStatus(Connection.Status.ACCEPTED);

        ThoughtEvent received = new ThoughtEvent("c1", "u2", "u1", Mood.LOVE, 15, 35, 70);
        ThoughtEvent sent = new ThoughtEvent("c1", "u1", "u2", Mood.HUG, 80, 65, 45);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(connectionRepository.findAllAccepted("u1")).thenReturn(List.of(connection));
        when(userRepository.findById("u2")).thenReturn(Optional.of(partner));
        when(thoughtEventRepository.findFirstByConnectionIdAndRecipientIdOrderByOccurredAtDesc("c1", "u1"))
                .thenReturn(Optional.of(received));
        when(thoughtEventRepository.findFirstByConnectionIdAndSenderIdOrderByOccurredAtDesc("c1", "u1"))
                .thenReturn(Optional.of(sent));

        var result = connectionService.getAcceptedConnections("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastReceivedEnergy().getBody()).isEqualTo(15);
        assertThat(result.get(0).getLastReceivedEnergy().getMind()).isEqualTo(35);
        assertThat(result.get(0).getLastReceivedEnergy().getHeart()).isEqualTo(70);
        assertThat(result.get(0).getLastSentEnergy().getBody()).isEqualTo(80);
        assertThat(result.get(0).getLastSentEnergy().getMind()).isEqualTo(65);
        assertThat(result.get(0).getLastSentEnergy().getHeart()).isEqualTo(45);
    }

    @Test
    void getAcceptedConnections_keepsOlderEventsWithoutEnergyCompatible() {
        User user = new User("alice", "hash");
        user.setId("u1");
        User partner = new User("bob", "hash");
        partner.setId("u2");

        Connection connection = new Connection("u1", "u2");
        connection.setId("c1");
        connection.setStatus(Connection.Status.ACCEPTED);

        ThoughtEvent oldReceived = new ThoughtEvent("c1", "u2", "u1", Mood.LOVE);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(connectionRepository.findAllAccepted("u1")).thenReturn(List.of(connection));
        when(userRepository.findById("u2")).thenReturn(Optional.of(partner));
        when(thoughtEventRepository.findFirstByConnectionIdAndRecipientIdOrderByOccurredAtDesc("c1", "u1"))
                .thenReturn(Optional.of(oldReceived));
        when(thoughtEventRepository.findFirstByConnectionIdAndSenderIdOrderByOccurredAtDesc("c1", "u1"))
                .thenReturn(Optional.empty());

        var result = connectionService.getAcceptedConnections("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastReceivedMood()).isEqualTo(Mood.LOVE);
        assertThat(result.get(0).getLastReceivedEnergy()).isNull();
        assertThat(result.get(0).getLastSentEnergy()).isNull();
    }
}
