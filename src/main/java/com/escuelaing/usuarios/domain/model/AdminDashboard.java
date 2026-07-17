package com.escuelaing.usuarios.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboard {
    private long totalUsuarios;
    private List<CarreraCount> carreraBreakdown;
    private List<RecentSignup> recentSignups;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarreraCount {
        private String carrera;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSignup {
        private String id;
        private String name;
        private String carrera;
        private String date;
    }
}
