package de.kaviedes.thinkofyou3.controller;

import de.kaviedes.thinkofyou3.dto.ConnectionDTO;
import de.kaviedes.thinkofyou3.dto.ThinkRequest;
import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.service.ConnectionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionControllerTest {

    @Mock
    private ConnectionService connectionService;

    @InjectMocks
    private ConnectionController connectionController;

    @Test
    void requestConnection_returnsOk() {
        Map<String, String> body = new HashMap<>();
        body.put("username", "bob");

        var response = connectionController.request(body, auth());

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(connectionService).requestConnection("alice", "bob");
    }

    @Test
    void getAccepted_returnsList() {
        List<ConnectionDTO> data = List.of(
                new ConnectionDTO("c1", "bob", 2, 3, Connection.Status.ACCEPTED, null, null)
        );
        when(connectionService.getAcceptedConnections("alice")).thenReturn(data);

        var response = connectionController.getAccepted(auth());

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getPartnerUsername()).isEqualTo("bob");
    }

    @Test
    void getRequests_returnsList() {
        when(connectionService.getPendingRequests(anyString()))
                .thenReturn(List.of(new ConnectionDTO("c2", "bob", 0, 0, Connection.Status.PENDING, null, null)));

        var response = connectionController.getRequests(auth());

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getPartnerUsername()).isEqualTo("bob");
    }

    @Test
    void getSent_returnsList() {
        when(connectionService.getSentRequests(anyString()))
                .thenReturn(List.of(new ConnectionDTO("c3", "carol", 0, 0, Connection.Status.PENDING, null, null)));

        var response = connectionController.getSentRequests(auth());

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getPartnerUsername()).isEqualTo("carol");
    }

    @Test
    void accept_reject_cancel_delete_think_returnOk() {
        assertThat(connectionController.accept("c1", auth()).getStatusCode().is2xxSuccessful()).isTrue();
        verify(connectionService).acceptConnection("c1", "alice");

        assertThat(connectionController.reject("c1", auth()).getStatusCode().is2xxSuccessful()).isTrue();
        verify(connectionService).rejectConnection("c1", "alice");

        assertThat(connectionController.cancel("c1", auth()).getStatusCode().is2xxSuccessful()).isTrue();
        verify(connectionService).cancelConnection("c1", "alice");

        ThinkRequest request = new ThinkRequest();
        request.setMood("happy");
        assertThat(connectionController.think("c1", request, auth()).getStatusCode().is2xxSuccessful()).isTrue();
        verify(connectionService).thinkOfPartner("c1", "alice", "happy");

        assertThat(connectionController.delete("c1", auth()).getStatusCode().is2xxSuccessful()).isTrue();
        verify(connectionService).deleteConnection("c1", "alice");
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("alice", "n/a");
    }
}
