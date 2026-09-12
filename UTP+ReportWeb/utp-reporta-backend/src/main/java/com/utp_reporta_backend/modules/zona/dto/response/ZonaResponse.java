package com.utp_reporta_backend.modules.zona.dto.response;

import com.utp_reporta_backend.modules.zona.model.enums.EstadoZona;
import lombok.Data;

@Data
public class ZonaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private byte[] foto;
    private Long sedeId;
    private Boolean activo;
    private EstadoZona estado;
    private Integer reportCount;
}
