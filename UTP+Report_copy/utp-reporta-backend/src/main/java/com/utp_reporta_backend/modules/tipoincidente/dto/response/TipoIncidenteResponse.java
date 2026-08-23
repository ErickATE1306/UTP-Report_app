package com.utp_reporta_backend.modules.tipoincidente.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoIncidenteResponse {
    private Long id;
    private String nombre;
    private String descripcion;
}
