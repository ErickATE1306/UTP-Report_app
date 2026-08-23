package com.utp_reporta_backend.modules.zona.service.query;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import java.util.List;
public interface ZonaQueryService {
    List<ZonaResponse> obtenerTodasLasZonas(boolean includeInactive);
    List<ZonaResponse> obtenerZonasPorSedeId(Long sedeId, boolean includeInactive);
}
