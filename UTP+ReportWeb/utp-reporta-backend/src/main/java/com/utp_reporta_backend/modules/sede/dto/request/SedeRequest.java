package com.utp_reporta_backend.modules.sede.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SedeRequest {
    @NotBlank(message = "El nombre de la sede es obligatorio")
    private String nombre;
}
