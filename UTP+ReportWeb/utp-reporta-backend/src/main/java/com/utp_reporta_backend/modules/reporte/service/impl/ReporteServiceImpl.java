package com.utp_reporta_backend.modules.reporte.service.impl;

import com.utp_reporta_backend.modules.reporte.dto.request.ReporteRequest;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import com.utp_reporta_backend.modules.reporte.service.ReporteService;
import com.utp_reporta_backend.modules.reporte.service.command.ReporteCommandService;
import com.utp_reporta_backend.modules.reporte.service.query.ReporteQueryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import com.utp_reporta_backend.common.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {
    private final ReporteQueryService queryService;
    private final ReporteCommandService commandService;
    public List<ReporteDTO> getAllReportes(){return queryService.getAllReportes();}
    public ReporteDTO getReporteById(Long id){
        ReporteDTO reporte=queryService.getReporteById(id);
        if(reporte==null) throw new ResourceNotFoundException("Reporte no encontrado con ID: "+id);
        return reporte;
    }
    public List<ReporteDTO> getFilteredReportes(Long zonaId,Long sedeId){return queryService.getFilteredReportes(zonaId,sedeId);}
    public List<ReporteDTO> getFilteredReports(PrioridadReporte prioridad,EstadoReporte estado,Boolean anonimo){return queryService.getFilteredReports(prioridad,estado,anonimo);}
    public ReporteDTO createReporte(ReporteRequest r){return commandService.createReporte(r.getTipoIncidenteId(),r.getZonaId(),r.getDescripcion(),r.getFoto(),r.getIsAnonimo(),r.getContacto(),r.getUsuarioId());}
    public ReporteDTO updateReporte(Long id,ReporteRequest r){
        ReporteDTO reporte=commandService.updateReporte(id,r.getTipoIncidenteId(),r.getZonaId(),r.getDescripcion(),r.getFoto(),r.getIsAnonimo(),r.getContacto(),r.getUsuarioId());
        if(reporte==null) throw new ResourceNotFoundException("Reporte no encontrado con ID: "+id);
        return reporte;
    }
    public void deleteReporte(Long id){commandService.deleteReporte(id);}
    public List<ReporteDTO> getReportsByUsername(String username){
        List<ReporteDTO> reportes=queryService.getReportsByUsername(username);
        if(reportes.isEmpty()) throw new ResourceNotFoundException("No se encontraron reportes para el usuario: "+username);
        return reportes;
    }
    public void exportToExcel(HttpServletResponse response)throws IOException{queryService.exportToExcel(response);}
}
