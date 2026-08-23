package com.utp_reporta_backend.modules.notificacion.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.utp_reporta_backend.modules.notificacion.dto.NotificationMessage;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.mapper.ZonaMapper;
import com.utp_reporta_backend.modules.reporte.model.Reporte; // Import Reporte
import com.utp_reporta_backend.modules.reporte.mapper.ReporteMapper;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO; // Import ReporteDTO
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO; // Import ReporteGestionDTO
import com.utp_reporta_backend.modules.reporte.repository.ReporteRepository;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.usuario.repository.RolRepository;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.mail.MessagingException;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ReporteRepository reporteRepository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ZonaMapper zonaMapper;
    private final ReporteMapper reporteMapper;

    public NotificationService(SimpMessagingTemplate messagingTemplate, ReporteRepository reporteRepository, EmailService emailService, UsuarioRepository usuarioRepository, RolRepository rolRepository, ZonaMapper zonaMapper, ReporteMapper reporteMapper) {
        this.messagingTemplate = messagingTemplate;
        this.reporteRepository = reporteRepository;
        this.emailService = emailService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.zonaMapper = zonaMapper;
        this.reporteMapper = reporteMapper;
    }

    private ZonaResponse mapToDto(Zona z) {
        return zonaMapper.toResponse(z);
    }

    // New method to map Reporte to ReporteDTO
    private ReporteDTO mapReporteToDto(Reporte reporte) {
        return reporteMapper.toResponse(reporte);
    }

    public void notifyZoneStatusChange(Zona zona) {
        // Deprecated: se evita notificar sin mensaje explícito para no saturar
        messagingTemplate.convertAndSend("/topic/zone-status", new NotificationMessage(null, mapToDto(zona)));
    }

    public void notifyZoneStatusChange(Zona zona, String message) {
        messagingTemplate.convertAndSend("/topic/zone-status", new NotificationMessage(message, mapToDto(zona)));

        // Send email to all users with ROLE_USER
        rolRepository.findByNombre(ERol.ROLE_USUARIO).ifPresent(rol -> {
            List<String> userEmails = usuarioRepository.findByRoles_Nombre(ERol.ROLE_USUARIO).stream()
                                                    .map(usuario -> usuario.getCorreo()) // Assuming getCorreo() exists
                                                    .collect(Collectors.toList());
            String subject = "Actualización de Estado de Zona: " + zona.getNombre();
            Map<String, Object> templateVariables = new java.util.HashMap<>();
            templateVariables.put("zonaNombre", zona.getNombre());
            templateVariables.put("zonaEstado", zona.getEstado());
            templateVariables.put("message", message);

            for (String email : userEmails) {
                try {
                    emailService.sendHtmlEmail(email, subject, "zone_status_change.html", templateVariables);
                } catch (MessagingException e) {
                    System.err.println("Error al enviar correo de cambio de estado de zona a " + email + ": " + e.getMessage());
                }
            }
        });
    }

    public void notifyNewReport(Reporte reporte, ReporteGestionDTO reporteGestionDTO) {
        String recipientUsername = reporte.getUsuario().getUsername();
        String message = "Tu reporte ha sido creado exitosamente y está pendiente de aprobación.";
        
        // Create a ReporteDTO with the provided ReporteGestionDTO for WebSocket notification
        ReporteDTO reporteDtoForNotification = mapReporteToDto(reporte);
        if (reporteDtoForNotification != null) {
            reporteDtoForNotification.setReporteGestion(reporteGestionDTO);
        }

        messagingTemplate.convertAndSendToUser(
            recipientUsername,
            "/queue/notifications",
            new NotificationMessage(message, reporteDtoForNotification)
        );
        // Fallback broadcast por username si la sesión STOMP no tiene Principal asociado
        // Permite que el frontend se suscriba a /topic/report-status.{username}
        messagingTemplate.convertAndSend(
            "/topic/report-status." + recipientUsername,
            new NotificationMessage(message, reporteDtoForNotification)
        );

        // Send email to the user who created the report
        String userEmail = reporte.getUsuario().getCorreo();
        String subject = "Nuevo Reporte Creado - " + reporte.getTipoIncidente().getNombre() + " en " + reporte.getZona().getNombre();
        Map<String, Object> templateVariables = new java.util.HashMap<>();
        templateVariables.put("reporteId", reporte.getId());
        templateVariables.put("reporteDescripcion", reporte.getDescripcion());
        templateVariables.put("reporteTipoIncidente", reporte.getTipoIncidente().getNombre());
        templateVariables.put("reporteZona", reporte.getZona().getNombre());
        templateVariables.put("message", message); // Use the same message for email
        templateVariables.put("reporteEstado", reporteGestionDTO.getEstado()); // Use estado from DTO

        try {
            if (userEmail == null || userEmail.trim().isEmpty()) {
                System.err.println("Error: No se puede enviar correo. El correo del usuario para el nuevo reporte ID " + reporte.getId() + " es nulo o vacío.");
            } else {
                emailService.sendHtmlEmail(userEmail, subject, "new_report_created.html", templateVariables);
            }
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo de nuevo reporte a " + userEmail + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado al procesar el envío de correo para el nuevo reporte ID " + reporte.getId() + " a " + userEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyReportStatusChange(Long reporteId, String message) {
        reporteRepository.findById(reporteId).ifPresent(reporte -> {
            String recipientUsername = reporte.getUsuario().getUsername();
            messagingTemplate.convertAndSendToUser(
                recipientUsername,
                "/queue/notifications",
                new NotificationMessage(message, mapReporteToDto(reporte))
            );
            // Fallback broadcast por username si la sesión STOMP no tiene Principal asociado
            // Permite que el frontend se suscriba a /topic/report-status.{username}
            messagingTemplate.convertAndSend(
                "/topic/report-status." + recipientUsername,
                new NotificationMessage(message, mapReporteToDto(reporte))
            );

            // Send email to the user who sent the report
            String userEmail = reporte.getUsuario().getCorreo();
            String subject = "Actualización de Reporte - " + reporte.getTipoIncidente().getNombre() + " en " + reporte.getZona().getNombre();
            Map<String, Object> templateVariables = new java.util.HashMap<>();
            templateVariables.put("reporteId", reporte.getId());
            templateVariables.put("reporteDescripcion", reporte.getDescripcion());
            templateVariables.put("reporteTipoIncidente", reporte.getTipoIncidente().getNombre()); // Add incident type
            templateVariables.put("reporteZona", reporte.getZona().getNombre()); // Add zone name
            templateVariables.put("message", message);
            templateVariables.put("reporteEstado", reporte.getReporteGestion().getEstado());

            try {
                if (userEmail == null || userEmail.trim().isEmpty()) {
                    System.err.println("Error: No se puede enviar correo. El correo del usuario para el reporte ID " + reporteId + " es nulo o vacío.");
                } else {
                    emailService.sendHtmlEmail(userEmail, subject, "report_status_change.html", templateVariables);
                }
            } catch (MessagingException e) {
                System.err.println("Error al enviar correo de cambio de estado de reporte a " + userEmail + ": " + e.getMessage());
                e.printStackTrace(); // Print full stack trace for more details
            } catch (Exception e) { // Catch any other unexpected exceptions
                System.err.println("Error inesperado al procesar el envío de correo para el reporte ID " + reporteId + " a " + userEmail + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}


