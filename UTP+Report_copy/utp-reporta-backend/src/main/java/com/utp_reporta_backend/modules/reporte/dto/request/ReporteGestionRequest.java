package com.utp_reporta_backend.modules.reporte.dto.request;

import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteGestionRequest {
    private EstadoReporte estado;
    private PrioridadReporte prioridad;
    private Long seguridadId;
}
