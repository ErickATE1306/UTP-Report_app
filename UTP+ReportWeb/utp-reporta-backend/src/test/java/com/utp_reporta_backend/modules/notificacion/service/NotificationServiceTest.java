package com.utp_reporta_backend.modules.notificacion.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.utp_reporta_backend.modules.notificacion.dto.NotificationMessage;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.mapper.ReporteMapper;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import com.utp_reporta_backend.modules.reporte.model.ReporteGestion;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.repository.ReporteRepository;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.repository.RolRepository;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.mapper.ZonaMapper;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.model.enums.EstadoZona;

import jakarta.mail.MessagingException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Long ID = 10L;

    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ReporteRepository reporteRepository;
    @Mock private EmailService emailService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private ZonaMapper zonaMapper;
    @Mock private ReporteMapper reporteMapper;

    @InjectMocks private NotificationService service;

    private Zona zona;
    private Reporte reporte;
    private Usuario dueno;
    private final ReporteDTO reporteDto = new ReporteDTO();
    private final ZonaResponse zonaDto = new ZonaResponse();

    @BeforeEach
    void preparar() {
        zona = new Zona();
        zona.setNombre("Patio");
        zona.setEstado(EstadoZona.ZONA_PRECAUCION);

        dueno = new Usuario();
        dueno.setUsername("ana");
        dueno.setCorreo("ana@utp.pe");

        ReporteGestion gestion = new ReporteGestion();
        gestion.setEstado(EstadoReporte.UBICANDO);

        reporte = new Reporte();
        reporte.setId(ID);
        reporte.setUsuario(dueno);
        reporte.setZona(zona);
        reporte.setTipoIncidente(new TipoIncidente(1L, "Robo", null));
        reporte.setReporteGestion(gestion);

        lenient().when(zonaMapper.toResponse(zona)).thenReturn(zonaDto);
        lenient().when(reporteMapper.toResponse(reporte)).thenReturn(reporteDto);
    }

    private Usuario usuarioConCorreo(String correo) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        return usuario;
    }

    private void conUsuarios(String... correos) {
        when(rolRepository.findByNombre(ERol.ROLE_USUARIO)).thenReturn(Optional.of(new Rol(1L, ERol.ROLE_USUARIO)));
        when(usuarioRepository.findByRoles_Nombre(ERol.ROLE_USUARIO))
                .thenReturn(java.util.Arrays.stream(correos).map(this::usuarioConCorreo).toList());
    }

    @Test
    @DisplayName("el cambio de zona se publica en /topic/zone-status")
    void publicaCambioDeZona() {
        when(rolRepository.findByNombre(ERol.ROLE_USUARIO)).thenReturn(Optional.empty());

        service.notifyZoneStatusChange(zona, "Zona peligrosa, evitar");

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/zone-status"), captor.capture());
        assertEquals("Zona peligrosa, evitar", captor.getValue().getMessage());
        assertSame(zonaDto, captor.getValue().getZona());
    }

    @Test
    @DisplayName("el cambio de zona envía un correo a cada usuario")
    void correoCambioDeZona() throws MessagingException {
        conUsuarios("a@utp.pe", "b@utp.pe");

        service.notifyZoneStatusChange(zona, "mensaje");

        verify(emailService).sendHtmlEmail(eq("a@utp.pe"), eq("Actualización de Estado de Zona: Patio"), eq("zone_status_change.html"), any());
        verify(emailService).sendHtmlEmail(eq("b@utp.pe"), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("un correo fallido no impide enviar los siguientes")
    void correoFallidoContinua() throws MessagingException {
        conUsuarios("a@utp.pe", "b@utp.pe");
        doThrow(new MessagingException("x")).when(emailService).sendHtmlEmail(eq("a@utp.pe"), anyString(), anyString(), any());

        assertDoesNotThrow(() -> service.notifyZoneStatusChange(zona, "mensaje"));

        verify(emailService, times(2)).sendHtmlEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("un nuevo reporte notifica al dueño por WebSocket y correo")
    void nuevoReporte() throws MessagingException {
        ReporteGestionDTO gestion = new ReporteGestionDTO();
        gestion.setEstado(EstadoReporte.PENDIENTE);

        service.notifyNewReport(reporte, gestion);

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(messagingTemplate).convertAndSendToUser(eq("ana"), eq("/queue/notifications"), captor.capture());
        assertSame(reporteDto, captor.getValue().getReporte());
        assertSame(gestion, reporteDto.getReporteGestion());
        verify(messagingTemplate).convertAndSend(eq("/topic/report-status.ana"), any(NotificationMessage.class));
        verify(emailService).sendHtmlEmail(eq("ana@utp.pe"), eq("Nuevo Reporte Creado - Robo en Patio"), eq("new_report_created.html"), any());
    }

    @Test
    @DisplayName("un nuevo reporte no envía correo si el dueño no tiene uno")
    void nuevoReporteSinCorreo() throws MessagingException {
        dueno.setCorreo("  ");

        service.notifyNewReport(reporte, new ReporteGestionDTO());

        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("un fallo de correo no interrumpe la notificación de un nuevo reporte")
    void nuevoReporteCorreoFallido() throws MessagingException {
        doThrow(new MessagingException("x")).when(emailService).sendHtmlEmail(anyString(), anyString(), anyString(), any());

        assertDoesNotThrow(() -> service.notifyNewReport(reporte, new ReporteGestionDTO()));
    }

    @Test
    @DisplayName("el cambio de estado notifica al dueño por WebSocket y correo")
    void cambioDeEstado() throws MessagingException {
        when(reporteRepository.findById(ID)).thenReturn(Optional.of(reporte));

        service.notifyReportStatusChange(ID, "Tu reporte fue atendido");

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(messagingTemplate).convertAndSendToUser(eq("ana"), eq("/queue/notifications"), captor.capture());
        assertEquals("Tu reporte fue atendido", captor.getValue().getMessage());
        verify(emailService).sendHtmlEmail(eq("ana@utp.pe"), eq("Actualización de Reporte - Robo en Patio"), eq("report_status_change.html"), any());
    }

    @Test
    @DisplayName("el cambio de estado no hace nada si el reporte no existe")
    void cambioDeEstadoReporteInexistente() {
        when(reporteRepository.findById(ID)).thenReturn(Optional.empty());

        service.notifyReportStatusChange(ID, "mensaje");

        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("B02: el cambio de estado no falla si el reporte aún no tiene gestión")
    void cambioDeEstadoSinGestion() throws MessagingException {
        reporte.setReporteGestion(null);
        when(reporteRepository.findById(ID)).thenReturn(Optional.of(reporte));

        assertDoesNotThrow(() -> service.notifyReportStatusChange(ID, "mensaje"));
        verify(emailService).sendHtmlEmail(eq("ana@utp.pe"), anyString(), eq("report_status_change.html"), any());
    }
}
