package com.utp_reporta_backend.modules.reporte.service.command.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utp_reporta_backend.common.exception.ResourceNotFoundException;
import com.utp_reporta_backend.modules.notificacion.service.NotificationService;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import com.utp_reporta_backend.modules.reporte.model.ReporteGestion;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import com.utp_reporta_backend.modules.reporte.repository.ReporteGestionRepository;
import com.utp_reporta_backend.modules.reporte.repository.ReporteRepository;
import com.utp_reporta_backend.modules.time.service.TimeService;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.model.enums.EstadoZona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class ReporteGestionCommandServiceImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2025, 10, 11, 10, 0);
    private static final Long ID = 1L;

    @Mock private ReporteGestionRepository reporteGestionRepository;
    @Mock private ReporteRepository reporteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TimeService timeService;
    @Mock private ZonaRepository zonaRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private ReporteGestionCommandServiceImpl service;

    private Reporte reporte;
    private Zona zona;

    @BeforeEach
    void preparar() {
        zona = new Zona();
        zona.setId(5L);
        reporte = new Reporte();
        reporte.setId(ID);
        reporte.setZona(zona);

        lenient().when(reporteRepository.findById(ID)).thenReturn(Optional.of(reporte));
        lenient().when(reporteGestionRepository.findByReporte(reporte)).thenReturn(Optional.empty());
        lenient().when(reporteGestionRepository.save(any(ReporteGestion.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);
    }

    private void verificarSinAlertaDeZona() {
        verify(notificationService, never()).notifyZoneStatusChange(any(Zona.class), anyString());
    }

    @Nested
    @DisplayName("updateReporteGestion")
    class UpdateReporteGestion {

        @Test
        @DisplayName("falla si el reporte no existe")
        void reporteInexistente() {
            when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateReporteGestion(99L, EstadoReporte.UBICANDO, null, null));
        }

        @Test
        @DisplayName("falla si el agente de seguridad no existe")
        void seguridadInexistente() {
            when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateReporteGestion(ID, EstadoReporte.EN_PROCESO, null, 9L));
            verify(reporteGestionRepository, never()).save(any());
        }

        @Test
        @DisplayName("asigna al agente de seguridad indicado")
        void asignaSeguridad() {
            Usuario agente = new Usuario();
            when(usuarioRepository.findById(9L)).thenReturn(Optional.of(agente));

            service.updateReporteGestion(ID, EstadoReporte.EN_PROCESO, PrioridadReporte.ALTA, 9L);

            assertThat(reporte.getSeguridadAsignado()).isSameAs(agente);
            verify(reporteRepository).save(reporte);
        }

        @Test
        @DisplayName("crea la gestión si el reporte aún no tiene una")
        void creaGestion() {
            ReporteGestionDTO dto = service.updateReporteGestion(ID, EstadoReporte.PENDIENTE, PrioridadReporte.MEDIA, null);

            assertEquals(EstadoReporte.PENDIENTE, dto.getEstado());
            assertEquals(PrioridadReporte.MEDIA, dto.getPrioridad());
            assertEquals(AHORA, dto.getFechaActualizacion());
        }

        @Test
        @DisplayName("conserva la prioridad existente si no se envía una nueva")
        void conservaPrioridad() {
            ReporteGestion existente = new ReporteGestion();
            existente.setId(3L);
            existente.setPrioridad(PrioridadReporte.ALTA);
            when(reporteGestionRepository.findByReporte(reporte)).thenReturn(Optional.of(existente));

            ReporteGestionDTO dto = service.updateReporteGestion(ID, EstadoReporte.EN_PROCESO, null, null);

            assertEquals(3L, dto.getId());
            assertEquals(PrioridadReporte.ALTA, dto.getPrioridad());
            assertEquals(EstadoReporte.EN_PROCESO, dto.getEstado());
        }

        @Test
        @DisplayName("reemplaza la prioridad existente si se envía una nueva")
        void reemplazaPrioridad() {
            ReporteGestion existente = new ReporteGestion();
            existente.setPrioridad(PrioridadReporte.ALTA);
            when(reporteGestionRepository.findByReporte(reporte)).thenReturn(Optional.of(existente));

            ReporteGestionDTO dto = service.updateReporteGestion(ID, EstadoReporte.EN_PROCESO, PrioridadReporte.BAJA, null);

            assertEquals(PrioridadReporte.BAJA, dto.getPrioridad());
        }
    }

    @Nested
    @DisplayName("notificaciones al dueño del reporte")
    class NotificacionesDeReporte {

        @ParameterizedTest(name = "{0} => \"{1}\"")
        @CsvSource({
                "UBICANDO, se dirige a la zona",
                "INVESTIGANDO, está siendo investigado",
                "RESUELTO, ha sido resuelto",
                "CANCELADO, ha sido cancelado"
        })
        void notificaSegunElEstado(EstadoReporte estado, String mensaje) {
            service.updateReporteGestion(ID, estado, null, null);

            verify(notificationService).notifyReportStatusChange(eq(ID), contains(mensaje));
        }

        @ParameterizedTest
        @CsvSource({"PENDIENTE", "EN_PROCESO", "PENDIENTE_APROBACION"})
        @DisplayName("no notifica en los estados internos")
        void noNotificaEstadosInternos(EstadoReporte estado) {
            service.updateReporteGestion(ID, estado, null, null);

            verify(notificationService, never()).notifyReportStatusChange(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("estado de la zona al resolver un reporte")
    class EstadoDeZona {

        @Test
        @DisplayName("el primer reporte inicia la ventana semanal")
        void primerReporte() {
            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            assertEquals(AHORA, zona.getFirstReportDate());
            assertEquals(1, zona.getReportCount());
            assertEquals(EstadoZona.ZONA_SEGURA, zona.getEstado());
            verify(zonaRepository).save(zona);
        }

        @ParameterizedTest(name = "con {0} previos queda {1} reportes => {2}")
        @CsvSource({
                "1, 2, ZONA_SEGURA",
                "4, 5, ZONA_SEGURA",
                "5, 6, ZONA_PRECAUCION",
                "9, 10, ZONA_PRECAUCION",
                "10, 11, ZONA_PELIGROSA"
        })
        void aplicaLosUmbrales(int previos, int total, EstadoZona esperado) {
            zona.setFirstReportDate(AHORA.minusDays(1));
            zona.setReportCount(previos);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            assertEquals(total, zona.getReportCount());
            assertEquals(esperado, zona.getEstado());
        }

        @Test
        @DisplayName("reinicia la ventana si pasó más de una semana")
        void reiniciaVentana() {
            zona.setFirstReportDate(AHORA.minusWeeks(1).minusMinutes(1));
            zona.setReportCount(11);
            zona.setEstado(EstadoZona.ZONA_PELIGROSA);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            assertEquals(AHORA, zona.getFirstReportDate());
            assertEquals(1, zona.getReportCount());
            assertEquals(EstadoZona.ZONA_SEGURA, zona.getEstado());
            verificarSinAlertaDeZona();
        }

        @Test
        @DisplayName("no reinicia la ventana al cumplirse exactamente una semana")
        void mismaSemana() {
            zona.setFirstReportDate(AHORA.minusWeeks(1));
            zona.setReportCount(3);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            assertEquals(4, zona.getReportCount());
        }

        @Test
        @DisplayName("alerta al pasar a precaución")
        void alertaPrecaucion() {
            zona.setFirstReportDate(AHORA.minusDays(1));
            zona.setReportCount(5);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            verify(notificationService).notifyZoneStatusChange(zona, "Zona en precaución, mantener cuidado");
        }

        @Test
        @DisplayName("alerta al pasar a peligrosa")
        void alertaPeligrosa() {
            zona.setEstado(EstadoZona.ZONA_PRECAUCION);
            zona.setFirstReportDate(AHORA.minusDays(1));
            zona.setReportCount(10);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            verify(notificationService).notifyZoneStatusChange(zona, "Zona peligrosa, evitar");
        }

        @Test
        @DisplayName("no alerta si el estado de la zona no cambia")
        void sinCambioSinAlerta() {
            zona.setEstado(EstadoZona.ZONA_PRECAUCION);
            zona.setFirstReportDate(AHORA.minusDays(1));
            zona.setReportCount(6);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            verificarSinAlertaDeZona();
        }

        @Test
        @DisplayName("otros estados no modifican la zona")
        void otrosEstadosNoAfectan() {
            service.updateReporteGestion(ID, EstadoReporte.INVESTIGANDO, null, null);

            assertEquals(0, zona.getReportCount());
            verify(zonaRepository, never()).save(any());
        }

        @Test
        @DisplayName("B01: resolver dos veces el mismo reporte no suma dos veces en la zona")
        void noCuentaDosVecesElMismoReporte() {
            ReporteGestion yaResuelto = new ReporteGestion();
            yaResuelto.setEstado(EstadoReporte.RESUELTO);
            when(reporteGestionRepository.findByReporte(reporte)).thenReturn(Optional.of(yaResuelto));
            zona.setFirstReportDate(AHORA.minusDays(1));
            zona.setReportCount(3);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            assertEquals(3, zona.getReportCount());
            verify(zonaRepository, never()).save(any());
        }

        @Test
        @DisplayName("B01: resolver un reporte con gestión previa sí suma en la zona")
        void cuentaAlResolverPorPrimeraVez() {
            ReporteGestion enAprobacion = new ReporteGestion();
            enAprobacion.setEstado(EstadoReporte.PENDIENTE_APROBACION);
            when(reporteGestionRepository.findByReporte(reporte)).thenReturn(Optional.of(enAprobacion));
            zona.setFirstReportDate(AHORA.minusDays(1));
            zona.setReportCount(3);

            service.updateReporteGestion(ID, EstadoReporte.RESUELTO, null, null);

            assertEquals(4, zona.getReportCount());
        }
    }

    @Nested
    @DisplayName("flujo de atención")
    class FlujoDeAtencion {

        @Test
        @DisplayName("irAZona pasa a UBICANDO")
        void irAZona() {
            assertEquals(EstadoReporte.UBICANDO, service.irAZona(ID).getEstado());
        }

        @Test
        @DisplayName("zonaUbicada pasa a INVESTIGANDO")
        void zonaUbicada() {
            assertEquals(EstadoReporte.INVESTIGANDO, service.zonaUbicada(ID).getEstado());
        }

        @Test
        @DisplayName("completarReporte guarda el parte y queda pendiente de aprobación")
        void completar() {
            ReporteGestionDTO dto = service.completarReporte(ID, "Atendido");

            assertEquals("Atendido", reporte.getMensajeSeguridad());
            assertEquals(EstadoReporte.PENDIENTE_APROBACION, dto.getEstado());
        }

        @Test
        @DisplayName("marcarComoResueltoPorAdmin guarda el mensaje y resuelve")
        void resolver() {
            ReporteGestionDTO dto = service.marcarComoResueltoPorAdmin(ID, "Conforme");

            assertEquals("Conforme", reporte.getMensajeAdmin());
            assertEquals(EstadoReporte.RESUELTO, dto.getEstado());
        }

        @Test
        @DisplayName("marcarComoResueltoPorAdmin conserva el mensaje anterior si llega vacío")
        void resolverSinMensaje() {
            reporte.setMensajeAdmin("Previo");

            service.marcarComoResueltoPorAdmin(ID, "");

            assertEquals("Previo", reporte.getMensajeAdmin());
        }

        @Test
        @DisplayName("rechazarPorAdmin devuelve el reporte a INVESTIGANDO")
        void rechazar() {
            ReporteGestionDTO dto = service.rechazarPorAdmin(ID, "Falta evidencia");

            assertEquals("Falta evidencia", reporte.getMensajeAdmin());
            assertEquals(EstadoReporte.INVESTIGANDO, dto.getEstado());
        }

        @Test
        @DisplayName("las acciones fallan si el reporte no existe")
        void reporteInexistente() {
            when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> service.completarReporte(99L, "x"));
            assertThrows(RuntimeException.class, () -> service.marcarComoResueltoPorAdmin(99L, "x"));
            assertThrows(RuntimeException.class, () -> service.rechazarPorAdmin(99L, "x"));
        }
    }
}
