package com.utp_reporta_backend.modules.reporte.service.command;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;

public interface ReporteGestionCommandService {
    ReporteGestionDTO updateReporteGestion(Long reporteId, EstadoReporte estado, PrioridadReporte prioridad, Long seguridadId);
    ReporteGestionDTO irAZona(Long reporteId);
    ReporteGestionDTO zonaUbicada(Long reporteId);
    ReporteGestionDTO completarReporte(Long reporteId, String mensajeSeguridad);
    ReporteGestionDTO marcarComoResueltoPorAdmin(Long reporteId, String mensajeAdmin);
    ReporteGestionDTO rechazarPorAdmin(Long reporteId, String mensajeAdmin);
}
