package com.utp_reporta_backend.modules.reporte.controller;

import com.utp_reporta_backend.modules.reporte.dto.request.ReporteGestionRequest;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import com.utp_reporta_backend.modules.reporte.service.ReporteGestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/gestion")
@RequiredArgsConstructor
public class ReporteGestionController {
    private final ReporteGestionService reporteGestionService;

    @PostMapping("/{reporteId}")
    public ResponseEntity<ReporteGestionDTO> createReporteGestion(@PathVariable Long reporteId, @RequestParam EstadoReporte estado,
            @RequestParam(required = false) PrioridadReporte prioridad, @RequestParam(required = false) Long seguridadId) {
        return ResponseEntity.ok(reporteGestionService.updateReporteGestion(reporteId, new ReporteGestionRequest(estado, prioridad, seguridadId)));
    }

    @PutMapping("/{reporteId}/ir-a-zona") @PreAuthorize("hasRole('SEGURIDAD')")
    public ResponseEntity<ReporteGestionDTO> irAZona(@PathVariable Long reporteId) { return ResponseEntity.ok(reporteGestionService.irAZona(reporteId)); }

    @PutMapping("/{reporteId}/zona-ubicada") @PreAuthorize("hasRole('SEGURIDAD')")
    public ResponseEntity<ReporteGestionDTO> zonaUbicada(@PathVariable Long reporteId) { return ResponseEntity.ok(reporteGestionService.zonaUbicada(reporteId)); }

    @PutMapping("/{reporteId}/completar") @PreAuthorize("hasRole('SEGURIDAD')")
    public ResponseEntity<ReporteGestionDTO> completarReporte(@PathVariable Long reporteId, @RequestParam String mensajeSeguridad) { return ResponseEntity.ok(reporteGestionService.completarReporte(reporteId, mensajeSeguridad)); }

    @PutMapping("/{reporteId}/marcar-resuelto-admin") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReporteGestionDTO> marcarComoResueltoPorAdmin(@PathVariable Long reporteId, @RequestParam(required = false) String mensajeAdmin) { return ResponseEntity.ok(reporteGestionService.marcarComoResueltoPorAdmin(reporteId, mensajeAdmin)); }

    @PutMapping("/{reporteId}/rechazar-admin") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReporteGestionDTO> rechazarPorAdmin(@PathVariable Long reporteId, @RequestParam(required = false) String mensajeAdmin) { return ResponseEntity.ok(reporteGestionService.rechazarPorAdmin(reporteId, mensajeAdmin)); }
}
