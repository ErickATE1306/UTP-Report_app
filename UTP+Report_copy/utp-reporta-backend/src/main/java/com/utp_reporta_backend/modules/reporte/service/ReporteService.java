package com.utp_reporta_backend.modules.reporte.service;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.dto.request.ReporteRequest;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

public interface ReporteService {
    void exportToExcel(HttpServletResponse response) throws IOException;
    List<ReporteDTO> getAllReportes();
    ReporteDTO getReporteById(Long id);
    List<ReporteDTO> getFilteredReportes(Long zonaId, Long sedeId);
    List<ReporteDTO> getFilteredReports(PrioridadReporte prioridad, EstadoReporte estado, Boolean isAnonimo);
    ReporteDTO createReporte(ReporteRequest request);
    ReporteDTO updateReporte(Long id, ReporteRequest request);
    void deleteReporte(Long id);
    List<ReporteDTO> getReportsByUsername(String username);
}


