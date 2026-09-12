package com.utp_reporta_backend.modules.reporte.controller;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.dto.request.ReporteRequest;
import com.utp_reporta_backend.modules.reporte.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService reporteService;

    @GetMapping
    public ResponseEntity<List<ReporteDTO>> getAllReportes() {
        List<ReporteDTO> reportes = reporteService.getAllReportes();
        return new ResponseEntity<>(reportes, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReporteDTO>> getFilteredReportes(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long sedeId) {
        List<ReporteDTO> reportes = reporteService.getFilteredReportes(zonaId, sedeId);
        return new ResponseEntity<>(reportes, HttpStatus.OK);
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<ReporteDTO>> getFilteredReports(
            @RequestParam(required = false) PrioridadReporte prioridad,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) Boolean isAnonimo) {
        List<ReporteDTO> reportes = reporteService.getFilteredReports(prioridad, estado, isAnonimo);
        return new ResponseEntity<>(reportes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteDTO> getReporteById(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.getReporteById(id));
    }

   @PostMapping
    public ResponseEntity<ReporteDTO> createReporte(
            @RequestParam Long tipoIncidenteId,
            @RequestParam Long zonaId,
            @RequestParam String descripcion,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestParam Boolean isAnonimo,
            @RequestParam(value = "contacto", required = false) String contacto,
            @RequestParam("usuarioId") Long usuarioId) {
        ReporteDTO createdReporte = reporteService.createReporte(
                new ReporteRequest(tipoIncidenteId, zonaId, descripcion, foto, isAnonimo, contacto, usuarioId));
        return new ResponseEntity<>(createdReporte, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReporteDTO> updateReporte(
            @PathVariable Long id,
            @RequestParam("tipoIncidenteId") Long tipoIncidenteId,
            @RequestParam("zonaId") Long zonaId,
            @RequestParam("descripcion") String descripcion,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestParam("isAnonimo") Boolean isAnonimo,
            @RequestParam(value = "contacto", required = false) String contacto,
            @RequestParam("usuarioId") Long usuarioId) {
        ReporteDTO updatedReporte = reporteService.updateReporte(
                id, new ReporteRequest(tipoIncidenteId, zonaId, descripcion, foto, isAnonimo, contacto, usuarioId));
        return ResponseEntity.ok(updatedReporte);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReporte(@PathVariable Long id) {
        reporteService.deleteReporte(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<List<ReporteDTO>> getReportsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(reporteService.getReportsByUsername(username));
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reportes.xlsx";
        response.setHeader(headerKey, headerValue);
        reporteService.exportToExcel(response);
    }
}


