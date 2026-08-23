package com.utp_reporta_backend.modules.zona.service.command;
import com.utp_reporta_backend.modules.zona.dto.request.ZonaRequest;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
public interface ZonaCommandService {
    ZonaResponse crearZona(ZonaRequest request);
    ZonaResponse updateZona(Long id, ZonaRequest request);
    void deleteZona(Long id);
    ZonaResponse setActivoZona(Long id, boolean activo);
}
