package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.AdminDashboard;
import com.escuelaing.usuarios.domain.port.outbound.AdminStatisticsPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private AdminStatisticsPort adminStatistics;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboard_returnsSortedCarrerasAndRecentSignups() {
        UUID id = UUID.randomUUID();
        when(adminStatistics.countUsuarios()).thenReturn(42L);
        when(adminStatistics.countPerfilesByCarrera()).thenReturn(Map.of(
                "Ing. Sistemas", 5L,
                "Diseño", 10L,
                "Mecánica", 2L
        ));
        when(adminStatistics.findRecentSignups(10)).thenReturn(List.of(
                new AdminDashboard.RecentSignup(id.toString(), "Karol", "Diseño", "2026-07-17T00:00:00Z")
        ));

        AdminDashboard result = adminDashboardService.getDashboard();

        assertThat(result.getTotalUsuarios()).isEqualTo(42L);
        assertThat(result.getCarreraBreakdown()).extracting(AdminDashboard.CarreraCount::getCarrera)
                .containsExactly("Diseño", "Ing. Sistemas", "Mecánica");
        assertThat(result.getCarreraBreakdown()).extracting(AdminDashboard.CarreraCount::getCount)
                .containsExactly(10L, 5L, 2L);
        assertThat(result.getRecentSignups()).hasSize(1);
        assertThat(result.getRecentSignups().get(0).getName()).isEqualTo("Karol");
    }
}
