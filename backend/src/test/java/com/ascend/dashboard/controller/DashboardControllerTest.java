package com.ascend.dashboard.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.dashboard.dto.*;
import com.ascend.dashboard.service.DashboardService;
import com.ascend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController")
class DashboardControllerTest {

    @Mock private DashboardService dashboardService;
    @Mock private AuthService authService;

    private DashboardController controller;
    private User user;
    private FirebasePrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(dashboardService, authService);
        user = User.builder().id(UUID.randomUUID()).username("alice").level(5).xp(1000L).build();
        principal = new FirebasePrincipal("uid-123", "alice@test.com", "password", Map.of());
        when(authService.getCurrentUser("uid-123")).thenReturn(user);
    }

    @Test
    @DisplayName("should return 200 with dashboard response")
    void shouldReturn200() {
        var response = DashboardResponse.builder()
                .user(new DashboardUserSection("alice", 5, null, false))
                .xp(new DashboardXpSection(1000, 5, 200, 50, 1100, 1.0))
                .streak(new DashboardStreakSection(3, 10, true, false))
                .dailyStats(new DashboardDailyStatsSection(2, 5, 40))
                .quests(List.of())
                .activeArc(null)
                .notifications(new DashboardNotificationSection(2))
                .build();

        when(dashboardService.getDashboard(user)).thenReturn(response);

        var result = controller.getDashboard(principal);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().user().displayName()).isEqualTo("alice");
        assertThat(result.getBody().getData().activeArc()).isNull();
    }
}
