package com.utp_reporta_backend.modules.tipoincidente.service.impl;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import com.utp_reporta_backend.modules.tipoincidente.service.TipoIncidenteService;
import com.utp_reporta_backend.modules.tipoincidente.service.command.TipoIncidenteCommandService;
import com.utp_reporta_backend.modules.tipoincidente.service.query.TipoIncidenteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoIncidenteServiceImpl implements TipoIncidenteService {
    private final TipoIncidenteQueryService queryService;
    private final TipoIncidenteCommandService commandService;
    public List<TipoIncidenteResponse> getAllTipoIncidentes() { return queryService.getAllTipoIncidentes(); }
    public TipoIncidenteResponse getTipoIncidenteById(Long id) { return queryService.getTipoIncidenteById(id); }
    public TipoIncidenteResponse createTipoIncidente(TipoIncidenteRequest request) { return commandService.createTipoIncidente(request); }
    public TipoIncidenteResponse updateTipoIncidente(Long id, TipoIncidenteRequest request) { return commandService.updateTipoIncidente(id, request); }
    public void deleteTipoIncidente(Long id) { commandService.deleteTipoIncidente(id); }
}
