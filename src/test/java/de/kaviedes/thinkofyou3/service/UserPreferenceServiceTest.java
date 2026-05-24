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

    @Test
    void getDashboardPreference_defaultsToCounts() {
        User user = new User("alice", "hash");
        user.setDashboardDisplayMode(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var result = userPreferenceService.getDashboardPreference("alice");

        assertThat(result.getMode()).isEqualTo("counts");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateDashboardPreference_savesLastEventMode() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var result = userPreferenceService.updateDashboardPreference("alice", "last_event");

        assertThat(result.getMode()).isEqualTo("last_event");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getEnergyLevels_defaultsMissingValuesForExistingUsers() {
        User user = new User("alice", "hash");
        user.setBodyEnergy(null);
        user.setMindEnergy(null);
        user.setHeartEnergy(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var result = userPreferenceService.getEnergyLevels("alice");

        assertThat(result.getBody()).isEqualTo(50);
        assertThat(result.getMind()).isEqualTo(50);
        assertThat(result.getHeart()).isEqualTo(50);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateEnergyLevels_savesValidatedValues() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var result = userPreferenceService.updateEnergyLevels(
                "alice",
                new de.kaviedes.thinkofyou3.dto.EnergyLevelsDTO(20, 60, 90));

        assertThat(result.getBody()).isEqualTo(20);
        assertThat(result.getMind()).isEqualTo(60);
        assertThat(result.getHeart()).isEqualTo(90);
        assertThat(user.getBodyEnergy()).isEqualTo(20);
        assertThat(user.getMindEnergy()).isEqualTo(60);
        assertThat(user.getHeartEnergy()).isEqualTo(90);
        verify(userRepository).save(user);
    }

    @Test
    void updateEnergyLevels_rejectsValuesOutsideRange() {
        User user = new User("alice", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userPreferenceService.updateEnergyLevels(
                "alice",
                new de.kaviedes.thinkofyou3.dto.EnergyLevelsDTO(101, 60, 90)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("between 0 and 100");
    }
}
