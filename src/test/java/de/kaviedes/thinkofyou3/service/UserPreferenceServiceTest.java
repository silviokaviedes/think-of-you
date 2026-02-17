package de.kaviedes.thinkofyou3.service;

import de.kaviedes.thinkofyou3.model.User;
import de.kaviedes.thinkofyou3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPreferenceService userPreferenceService;

    @Test
    void updateFavoriteMoods_savesValidatedList() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> result = userPreferenceService.updateFavoriteMoods("alice", List.of("hug", "exhausted", "hug"));

        assertThat(result).containsExactly("hug", "exhausted");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateFavoriteMoods_throwsOnUnknownMood() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userPreferenceService.updateFavoriteMoods("alice", List.of("not-real")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unknown mood");
    }
}
