package com.utp_reporta_backend.modules.reporte.dto.response;

import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import com.utp_reporta_backend.modules.reporte.model.enums.PrioridadReporte;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGestionDTO {
    private Long id;
    private EstadoReporte estado;
    private PrioridadReporte prioridad;
    private LocalDateTime fechaActualizacion;
}


