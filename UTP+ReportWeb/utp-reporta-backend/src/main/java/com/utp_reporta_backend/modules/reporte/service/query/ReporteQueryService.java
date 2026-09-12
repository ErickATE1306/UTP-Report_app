package com.utp_reporta_backend.modules.reporte.service.query;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface ReporteQueryService {
    List<ReporteDTO> getAllReportes();
    ReporteDTO getReporteById(Long id);
    List<ReporteDTO> getFilteredReportes(Long zonaId, Long sedeId);
    List<ReporteDTO> getFilteredReports(PrioridadReporte prioridad, EstadoReporte estado, Boolean isAnonimo);
    List<ReporteDTO> getReportsByUsername(String username);
    void exportToExcel(HttpServletResponse response) throws IOException;
}
