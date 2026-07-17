package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.AdminDashboard;
import com.escuelaing.usuarios.domain.port.in.AdminDashboardUseCase;
import com.escuelaing.usuarios.domain.port.outbound.AdminStatisticsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService implements AdminDashboardUseCase {

    private static final int RECENT_SIGNUPS_LIMIT = 10;

    private final AdminStatisticsPort adminStatistics;

    public AdminDashboardService(AdminStatisticsPort adminStatistics) {
        this.adminStatistics = adminStatistics;
    }

    @Override
    public AdminDashboard getDashboard() {
        long totalUsuarios = adminStatistics.countUsuarios();

        List<AdminDashboard.CarreraCount> carreraBreakdown = adminStatistics.countPerfilesByCarrera()
                .entrySet().stream()
                .map(e -> new AdminDashboard.CarreraCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();

        List<AdminDashboard.RecentSignup> recentSignups = adminStatistics.findRecentSignups(RECENT_SIGNUPS_LIMIT);

        return AdminDashboard.builder()
                .totalUsuarios(totalUsuarios)
                .carreraBreakdown(carreraBreakdown)
                .recentSignups(recentSignups)
                .build();
    }
}
