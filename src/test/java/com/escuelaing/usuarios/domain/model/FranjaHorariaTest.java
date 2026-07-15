package com.escuelaing.usuarios.domain.model;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranjaHorariaTest {

    private static final UUID PERFIL_ID = UUID.randomUUID();

    // ── crear ─────────────────────────────────────────────────────────────────

    @Test
    void crear_conDatosValidos_asignaIdUnico() {
        FranjaHoraria f1 = FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(10, 0));
        FranjaHoraria f2 = FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertThat(f1.getId()).isNotNull();
        assertThat(f1.getId()).isNotEqualTo(f2.getId());
    }

    @Test
    void crear_conDatosValidos_persisteTodasLasPropiedades() {
        LocalTime inicio = LocalTime.of(14, 30);
        LocalTime fin    = LocalTime.of(16, 0);

        FranjaHoraria f = FranjaHoraria.crear(PERFIL_ID, DayOfWeek.FRIDAY, inicio, fin);

        assertThat(f.getPerfilId()).isEqualTo(PERFIL_ID);
        assertThat(f.getDiaSemana()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(f.getHoraInicio()).isEqualTo(inicio);
        assertThat(f.getHoraFin()).isEqualTo(fin);
    }

    // ── validación horas ──────────────────────────────────────────────────────

    @Test
    void crear_horaInicioIgualAFin_lanzaDominioInvalido() {
        LocalTime mismaHora = LocalTime.of(9, 0);

        assertThatThrownBy(() ->
                FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY, mismaHora, mismaHora))
                .isInstanceOf(DominioInvalidoException.class)
                .hasMessageContaining("inicio debe ser anterior");
    }

    @Test
    void crear_horaInicioMayorAFin_lanzaDominioInvalido() {
        assertThatThrownBy(() ->
                FranjaHoraria.crear(PERFIL_ID, DayOfWeek.TUESDAY,
                        LocalTime.of(18, 0), LocalTime.of(8, 0)))
                .isInstanceOf(DominioInvalidoException.class);
    }

    @Test
    void crear_diaNulo_lanzaNullPointer() {
        assertThatThrownBy(() ->
                FranjaHoraria.crear(PERFIL_ID, null,
                        LocalTime.of(8, 0), LocalTime.of(10, 0)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void crear_horaInicioNula_lanzaNullPointer() {
        assertThatThrownBy(() ->
                FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY, null, LocalTime.of(10, 0)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void crear_horaFinNula_lanzaNullPointer() {
        assertThatThrownBy(() ->
                FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY, LocalTime.of(8, 0), null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── reconstruir ───────────────────────────────────────────────────────────

    @Test
    void reconstruir_preservaIdExistente() {
        UUID id = UUID.randomUUID();
        FranjaHoraria f = FranjaHoraria.reconstruir(id, PERFIL_ID, DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertThat(f.getId()).isEqualTo(id);
    }

    // ── equals / hashCode ─────────────────────────────────────────────────────

    @Test
    void dosInstanciasConMismoId_sonIguales() {
        UUID id = UUID.randomUUID();
        FranjaHoraria f1 = FranjaHoraria.reconstruir(id, PERFIL_ID, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(10, 0));
        FranjaHoraria f2 = FranjaHoraria.reconstruir(id, PERFIL_ID, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0));

        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
    }

    @Test
    void dosInstanciasConDistintoId_noSonIguales() {
        FranjaHoraria f1 = FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(10, 0));
        FranjaHoraria f2 = FranjaHoraria.crear(PERFIL_ID, DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertThat(f1).isNotEqualTo(f2);
    }
}
