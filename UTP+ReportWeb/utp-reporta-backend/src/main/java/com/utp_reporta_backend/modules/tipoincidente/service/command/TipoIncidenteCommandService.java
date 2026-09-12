package com.utp_reporta_backend.modules.tipoincidente.service.command;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;

public interface TipoIncidenteCommandService {
    TipoIncidenteResponse createTipoIncidente(TipoIncidenteRequest request);
    TipoIncidenteResponse updateTipoIncidente(Long id, TipoIncidenteRequest request);
    void deleteTipoIncidente(Long id);
}
