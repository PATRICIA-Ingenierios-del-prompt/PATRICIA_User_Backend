package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.AdminDashboard;

import java.util.List;
import java.util.Map;

public interface AdminStatisticsPort {
    long countUsuarios();

    Map<String, Long> countPerfilesByCarrera();

    List<AdminDashboard.RecentSignup> findRecentSignups(int limit);
}
