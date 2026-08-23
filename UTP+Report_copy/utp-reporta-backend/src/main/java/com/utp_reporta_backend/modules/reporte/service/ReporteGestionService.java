package com.utp_reporta_backend.modules.reporte.service;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.dto.request.ReporteGestionRequest;

public interface ReporteGestionService {
    ReporteGestionDTO updateReporteGestion(Long reporteId, ReporteGestionRequest request);
    ReporteGestionDTO irAZona(Long reporteId);
    ReporteGestionDTO zonaUbicada(Long reporteId);
    ReporteGestionDTO completarReporte(Long reporteId, String mensajeSeguridad);
    ReporteGestionDTO marcarComoResueltoPorAdmin(Long reporteId, String mensajeAdmin);
    ReporteGestionDTO rechazarPorAdmin(Long reporteId, String mensajeAdmin);
}


