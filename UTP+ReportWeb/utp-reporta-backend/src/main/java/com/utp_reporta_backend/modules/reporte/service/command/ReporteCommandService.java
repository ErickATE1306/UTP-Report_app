package com.utp_reporta_backend.modules.reporte.service.command;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ReporteCommandService {
    ReporteDTO createReporte(Long tipoIncidenteId, Long zonaId, String descripcion, MultipartFile foto, Boolean isAnonimo, String contacto, Long usuarioId);
    ReporteDTO updateReporte(Long id, Long tipoIncidenteId, Long zonaId, String descripcion, MultipartFile foto, Boolean isAnonimo, String contacto, Long usuarioId);
    void deleteReporte(Long id);
}
