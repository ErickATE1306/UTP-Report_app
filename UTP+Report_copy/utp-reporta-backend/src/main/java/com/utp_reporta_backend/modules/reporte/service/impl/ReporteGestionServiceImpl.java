package com.utp_reporta_backend.modules.reporte.service.impl;

import com.utp_reporta_backend.modules.reporte.dto.request.ReporteGestionRequest;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.service.ReporteGestionService;
import com.utp_reporta_backend.modules.reporte.service.command.ReporteGestionCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporteGestionServiceImpl implements ReporteGestionService {
    private final ReporteGestionCommandService commandService;
    public ReporteGestionDTO updateReporteGestion(Long id, ReporteGestionRequest r){return commandService.updateReporteGestion(id,r.getEstado(),r.getPrioridad(),r.getSeguridadId());}
    public ReporteGestionDTO irAZona(Long id){return commandService.irAZona(id);}
    public ReporteGestionDTO zonaUbicada(Long id){return commandService.zonaUbicada(id);}
    public ReporteGestionDTO completarReporte(Long id,String mensaje){return commandService.completarReporte(id,mensaje);}
    public ReporteGestionDTO marcarComoResueltoPorAdmin(Long id,String mensaje){return commandService.marcarComoResueltoPorAdmin(id,mensaje);}
    public ReporteGestionDTO rechazarPorAdmin(Long id,String mensaje){return commandService.rechazarPorAdmin(id,mensaje);}
}
