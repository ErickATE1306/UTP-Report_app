package com.utp_reporta_backend.modules.reporte.repository;

import com.utp_reporta_backend.modules.reporte.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;


import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    long countByZonaId(Long zonaId);
    List<Reporte> findByZonaId(Long zonaId);
    List<Reporte> findByZonaSedeId(Long sedeId);
    List<Reporte> findByZonaIdAndZonaSedeId(Long zonaId, Long sedeId);

    List<Reporte> findByReporteGestion_PrioridadAndReporteGestion_EstadoAndIsAnonimo(
            PrioridadReporte prioridad, EstadoReporte estado, Boolean isAnonimo);

    List<Reporte> findByReporteGestion_PrioridadAndReporteGestion_Estado(
            PrioridadReporte prioridad, EstadoReporte estado);

    List<Reporte> findByReporteGestion_PrioridadAndIsAnonimo(
            PrioridadReporte prioridad, Boolean isAnonimo);

    List<Reporte> findByReporteGestion_EstadoAndIsAnonimo(
            EstadoReporte estado, Boolean isAnonimo);

    List<Reporte> findByReporteGestion_Prioridad(PrioridadReporte prioridad);

    List<Reporte> findByReporteGestion_Estado(EstadoReporte estado);

    List<Reporte> findByIsAnonimo(Boolean isAnonimo);

    List<Reporte> findByUsuarioUsername(String username);
}


