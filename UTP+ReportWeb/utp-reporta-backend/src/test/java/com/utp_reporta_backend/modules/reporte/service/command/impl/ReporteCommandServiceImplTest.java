package com.utp_reporta_backend.modules.reporte.service.command.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.utp_reporta_backend.common.exception.BadRequestException;
import com.utp_reporta_backend.modules.notificacion.service.NotificationService;
import com.utp_reporta_backend.modules.reporte.dto.request.ReporteGestionRequest;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.repository.ReporteRepository;
import com.utp_reporta_backend.modules.reporte.service.ReporteGestionService;
import com.utp_reporta_backend.modules.reporte.service.validation.ReporteFileValidator;
import com.utp_reporta_backend.modules.time.service.TimeService;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import com.utp_reporta_backend.modules.tipoincidente.repository.TipoIncidenteRepository;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class ReporteCommandServiceImplTest {

    private static final LocalDate HOY = LocalDate.of(2025, 10, 11);
    private static final LocalDateTime AHORA = HOY.atTime(10, 0);

    @Mock private ReporteRepository reporteRepository;
    @Mock private TipoIncidenteRepository tipoIncidenteRepository;
    @Mock private ZonaRepository zonaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TimeService timeService;
    @Mock private ReporteGestionService reporteGestionService;
    @Mock private NotificationService notificationService;
    @Mock private ReporteFileValidator fileValidator;

    @InjectMocks private ReporteCommandServiceImpl service;

    private TipoIncidente tipo;
    private Zona zona;
    private Usuario usuario;
    private ReporteGestionDTO gestion;

    @BeforeEach
    void preparar() {
        tipo = new TipoIncidente(1L, "Robo", null);
        zona = new Zona();
        zona.setId(2L);
        usuario = new Usuario();
        usuario.setId(3L);
        usuario.getRoles().add(new Rol(1L, ERol.ROLE_USUARIO));
        gestion = new ReporteGestionDTO();
        gestion.setEstado(EstadoReporte.PENDIENTE);

        lenient().when(tipoIncidenteRepository.findById(1L)).thenReturn(Optional.of(tipo));
        lenient().when(zonaRepository.findById(2L)).thenReturn(Optional.of(zona));
        lenient().when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        lenient().when(timeService.getCurrentLocalDatePeru()).thenReturn(HOY);
        lenient().when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);
        lenient().when(reporteRepository.save(any(Reporte.class))).thenAnswer(i -> {
            Reporte r = i.getArgument(0);
            r.setId(10L);
            lenient().when(reporteRepository.findById(10L)).thenReturn(Optional.of(r));
            return r;
        });
        lenient().when(reporteGestionService.updateReporteGestion(eq(10L), any(ReporteGestionRequest.class))).thenReturn(gestion);
    }

    private ReporteDTO crear(MultipartFile foto) {
        return service.createReporte(1L, 2L, "Robo de celular", foto, false, "999", 3L);
    }

    @Nested
    @DisplayName("createReporte")
    class CreateReporte {

        @Test
        @DisplayName("devuelve el reporte creado con su gestión inicial")
        void creaReporte() {
            ReporteDTO dto = crear(null);

            assertEquals(10L, dto.getId());
            assertEquals(1L, dto.getTipoIncidenteId());
            assertEquals(2L, dto.getZonaId());
            assertEquals(3L, dto.getUsuarioId());
            assertEquals("Robo de celular", dto.getDescripcion());
            assertEquals("999", dto.getContacto());
            assertEquals(AHORA, dto.getFechaCreacion());
            assertEquals(gestion, dto.getReporteGestion());
        }

        @Test
        @DisplayName("la gestión inicial queda en estado PENDIENTE")
        void gestionPendiente() {
            crear(null);

            ArgumentCaptor<ReporteGestionRequest> captor = ArgumentCaptor.forClass(ReporteGestionRequest.class);
            verify(reporteGestionService).updateReporteGestion(eq(10L), captor.capture());
            assertEquals(EstadoReporte.PENDIENTE, captor.getValue().getEstado());
        }

        @Test
        @DisplayName("notifica la creación del reporte")
        void notificaCreacion() {
            crear(null);

            verify(notificationService).notifyNewReport(any(Reporte.class), eq(gestion));
        }

        @Test
        @DisplayName("guarda los bytes de la foto")
        void guardaFoto() {
            byte[] bytes = {1, 2, 3};

            ReporteDTO dto = crear(new MockMultipartFile("foto", "f.png", "image/png", bytes));

            assertArrayEquals(bytes, dto.getFoto());
        }

        @Test
        @DisplayName("no guarda una foto vacía")
        void fotoVacia() {
            ReporteDTO dto = crear(new MockMultipartFile("foto", new byte[0]));

            assertNull(dto.getFoto());
        }

        @Test
        @DisplayName("falla si la foto no se puede leer")
        void fotoIlegible() throws IOException {
            MultipartFile foto = mock(MultipartFile.class);
            when(foto.isEmpty()).thenReturn(false);
            when(foto.getBytes()).thenThrow(new IOException("x"));

            RuntimeException error = assertThrows(RuntimeException.class, () -> crear(foto));

            assertEquals("Error al procesar la foto", error.getMessage());
        }

        @Test
        @DisplayName("no continúa si la foto no pasa la validación")
        void fotoInvalida() {
            MultipartFile foto = new MockMultipartFile("foto", new byte[] {1});
            doThrow(new BadRequestException("La foto debe ser JPG o PNG")).when(fileValidator).validate(foto);

            assertThrows(BadRequestException.class, () -> crear(foto));
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("falla si no existe el tipo de incidente, la zona o el usuario")
        void referenciasInexistentes() {
            when(tipoIncidenteRepository.findById(1L)).thenReturn(Optional.empty());
            assertEquals("TipoIncidente not found", assertThrows(RuntimeException.class, () -> crear(null)).getMessage());

            when(tipoIncidenteRepository.findById(1L)).thenReturn(Optional.of(tipo));
            when(zonaRepository.findById(2L)).thenReturn(Optional.empty());
            assertEquals("Zona not found", assertThrows(RuntimeException.class, () -> crear(null)).getMessage());

            when(zonaRepository.findById(2L)).thenReturn(Optional.of(zona));
            when(usuarioRepository.findById(3L)).thenReturn(Optional.empty());
            assertEquals("Usuario not found", assertThrows(RuntimeException.class, () -> crear(null)).getMessage());
        }

        @Test
        @DisplayName("conserva el reporte aunque falle la gestión inicial, sin notificar")
        void falloDeGestion() {
            when(reporteGestionService.updateReporteGestion(eq(10L), any(ReporteGestionRequest.class)))
                    .thenThrow(new RuntimeException("fallo"));

            ReporteDTO dto = crear(null);

            assertEquals(10L, dto.getId());
            assertNull(dto.getReporteGestion());
            verify(notificationService, never()).notifyNewReport(any(), any());
        }
    }

    @Nested
    @DisplayName("longitud de la descripción")
    class LongitudDescripcion {
        private ReporteDTO crearCon(String descripcion) {
            return service.createReporte(1L, 2L, descripcion, null, false, "999", 3L);
        }

        @Test
        @DisplayName("acepta 255 caracteres, el tamaño de la columna")
        void acepta255() {
            assertEquals(255, crearCon("x".repeat(255)).getDescripcion().length());
        }

        @Test
        @DisplayName("rechaza 256 caracteres al crear, sin guardar ni gastar un intento")
        void rechaza256AlCrear() {
            BadRequestException error = assertThrows(BadRequestException.class, () -> crearCon("x".repeat(256)));

            assertThat(error.getMessage()).contains("255");
            verify(reporteRepository, never()).save(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("rechaza 256 caracteres al actualizar")
        void rechaza256AlActualizar() {
            assertThrows(BadRequestException.class,
                    () -> service.updateReporte(10L, 1L, 2L, "x".repeat(256), null, false, null, 3L));

            verify(reporteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("límite de 3 reportes por día")
    class LimiteDiario {

        @Test
        @DisplayName("el primer reporte del día inicia el contador en 1")
        void primerReporteDelDia() {
            usuario.setFechaUltimoReporte(HOY.minusDays(1));
            usuario.setIntentosReporte(3);

            crear(null);

            assertEquals(1, usuario.getIntentosReporte());
            assertEquals(HOY, usuario.getFechaUltimoReporte());
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("un usuario sin reportes previos inicia el contador en 1")
        void sinReportesPrevios() {
            crear(null);

            assertEquals(1, usuario.getIntentosReporte());
        }

        @Test
        @DisplayName("permite el tercer reporte del día")
        void tercerReporte() {
            usuario.setFechaUltimoReporte(HOY);
            usuario.setIntentosReporte(2);

            crear(null);

            assertEquals(3, usuario.getIntentosReporte());
        }

        @Test
        @DisplayName("rechaza el cuarto reporte del día")
        void cuartoReporte() {
            usuario.setFechaUltimoReporte(HOY);
            usuario.setIntentosReporte(3);

            BadRequestException error = assertThrows(BadRequestException.class, () -> crear(null));

            assertThat(error.getMessage()).contains("límite de 3 reportes");
            verify(reporteRepository, never()).save(any());
        }

        @Test
        @DisplayName("no aplica a roles distintos de USUARIO")
        void otrosRoles() {
            usuario.getRoles().clear();
            usuario.getRoles().add(new Rol(2L, ERol.ROLE_ADMIN));
            usuario.setFechaUltimoReporte(HOY);
            usuario.setIntentosReporte(3);

            crear(null);

            assertEquals(3, usuario.getIntentosReporte());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateReporte y deleteReporte")
    class ActualizarYEliminar {

        private Reporte existente() {
            Reporte reporte = new Reporte();
            reporte.setId(10L);
            reporte.setTipoIncidente(tipo);
            reporte.setZona(zona);
            reporte.setUsuario(usuario);
            reporte.setFoto(new byte[] {9});
            when(reporteRepository.findById(10L)).thenReturn(Optional.of(reporte));
            return reporte;
        }

        @Test
        @DisplayName("devuelve null si el reporte no existe")
        void inexistente() {
            when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

            assertNull(service.updateReporte(99L, 1L, 2L, "x", null, false, null, 3L));
        }

        @Test
        @DisplayName("actualiza los datos y conserva la foto si no llega una nueva")
        void actualizaConservandoFoto() {
            existente();

            ReporteDTO dto = service.updateReporte(10L, 1L, 2L, "Nueva", null, true, "111", 3L);

            assertEquals("Nueva", dto.getDescripcion());
            assertEquals("111", dto.getContacto());
            assertEquals(true, dto.getIsAnonimo());
            assertArrayEquals(new byte[] {9}, dto.getFoto());
        }

        @Test
        @DisplayName("reemplaza la foto si llega una nueva")
        void reemplazaFoto() {
            existente();

            ReporteDTO dto = service.updateReporte(10L, 1L, 2L, "Nueva",
                    new MockMultipartFile("foto", "f.png", "image/png", new byte[] {1}), true, null, 3L);

            assertArrayEquals(new byte[] {1}, dto.getFoto());
        }

        @Test
        @DisplayName("deleteReporte elimina por id")
        void elimina() {
            service.deleteReporte(10L);

            verify(reporteRepository).deleteById(10L);
        }
    }
}
