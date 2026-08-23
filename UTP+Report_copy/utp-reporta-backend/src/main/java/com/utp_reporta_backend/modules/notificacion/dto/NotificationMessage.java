package com.utp_reporta_backend.modules.notificacion.dto;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mensaje de notificación enviado por WebSocket/STOMP.
 * Envía una respuesta de zona o reporte en lugar de la entidad JPA para evitar problemas
 * de serialización y exponer solo los campos necesarios al frontend.
 */
@Data
@NoArgsConstructor
public class NotificationMessage {
    private String message;
    private ZonaResponse zona;
    private ReporteDTO reporte;

    // Constructor for zone notifications
    public NotificationMessage(String message, ZonaResponse zona) {
        this.message = message;
        this.zona = zona;
        this.reporte = null; // Ensure reporte is null for zone notifications
    }

    // Constructor for report notifications
    public NotificationMessage(String message, ReporteDTO reporte) {
        this.message = message;
        this.reporte = reporte;
        this.zona = null; // Ensure zona is null for report notifications
    }
}

