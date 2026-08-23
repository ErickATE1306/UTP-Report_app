package com.utp_reporta_backend.modules.tipoincidente.service;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import java.util.List;

public interface TipoIncidenteService {
    List<TipoIncidenteResponse> getAllTipoIncidentes();
    TipoIncidenteResponse getTipoIncidenteById(Long id);
    TipoIncidenteResponse createTipoIncidente(TipoIncidenteRequest request);
    TipoIncidenteResponse updateTipoIncidente(Long id, TipoIncidenteRequest request);
    void deleteTipoIncidente(Long id);
}


