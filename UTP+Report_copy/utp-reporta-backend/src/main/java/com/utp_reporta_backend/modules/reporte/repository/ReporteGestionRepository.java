package com.utp_reporta_backend.modules.reporte.repository;

import com.utp_reporta_backend.modules.reporte.model.ReporteGestion;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReporteGestionRepository extends JpaRepository<ReporteGestion, Long> {
    Optional<ReporteGestion> findByReporte(Reporte reporte);
}


