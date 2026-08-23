package com.utp_reporta_backend.modules.tipoincidente.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TipoIncidenteRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String descripcion;
}
