package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.AdminDashboard;
import com.escuelaing.usuarios.domain.port.outbound.AdminStatisticsPort;
import com.escuelaing.usuarios.infrastructure.persistence.repository.PerfilJpaRepository;
import com.escuelaing.usuarios.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AdminStatisticsAdapter implements AdminStatisticsPort {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final PerfilJpaRepository perfilJpaRepository;

    public AdminStatisticsAdapter(UsuarioJpaRepository usuarioJpaRepository,
                                  PerfilJpaRepository perfilJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.perfilJpaRepository = perfilJpaRepository;
    }

    @Override
    public long countUsuarios() {
        return usuarioJpaRepository.count();
    }

    @Override
    public Map<String, Long> countPerfilesByCarrera() {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : perfilJpaRepository.countByCarrera()) {
            String carrera = (String) row[0];
            Long count = (Long) row[1];
            if (carrera != null && !carrera.isBlank()) {
                result.put(carrera, count);
            }
        }
        return result;
    }

    @Override
    public List<AdminDashboard.RecentSignup> findRecentSignups(int limit) {
        return usuarioJpaRepository.findRecentSignups(PageRequest.of(0, limit))
                .stream()
                .map(row -> {
                    UUID id = (UUID) row[0];
                    String nombre = (String) row[1];
                    String apellidos = (String) row[2];
                    String carrera = (String) row[3];
                    Instant fechaCreacion = (Instant) row[4];
                    String name = (nombre != null ? nombre : "") + (apellidos != null ? " " + apellidos : "");
                    name = name.isBlank() ? "Sin nombre" : name.trim();
                    String carreraLabel = carrera != null && !carrera.isBlank() ? carrera : "Sin carrera";
                    return new AdminDashboard.RecentSignup(
                            id.toString(),
                            name,
                            carreraLabel,
                            fechaCreacion.toString()
                    );
                })
                .toList();
    }
}
