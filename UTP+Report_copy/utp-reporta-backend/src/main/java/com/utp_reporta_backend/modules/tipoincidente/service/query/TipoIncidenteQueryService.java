package com.utp_reporta_backend.modules.tipoincidente.service.query;

import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import java.util.List;

public interface TipoIncidenteQueryService {
    List<TipoIncidenteResponse> getAllTipoIncidentes();
    TipoIncidenteResponse getTipoIncidenteById(Long id);
}
