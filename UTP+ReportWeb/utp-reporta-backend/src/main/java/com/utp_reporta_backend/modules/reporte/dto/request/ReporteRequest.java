package com.utp_reporta_backend.modules.reporte.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ReporteRequest {
    private Long tipoIncidenteId;
    private Long zonaId;
    private String descripcion;
    private MultipartFile foto;
    private Boolean isAnonimo;
    private String contacto;
    private Long usuarioId;
}
