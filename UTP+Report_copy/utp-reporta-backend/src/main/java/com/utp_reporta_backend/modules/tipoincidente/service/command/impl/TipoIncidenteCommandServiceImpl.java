package com.utp_reporta_backend.modules.tipoincidente.service.command.impl;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import com.utp_reporta_backend.modules.tipoincidente.mapper.TipoIncidenteMapper;
import com.utp_reporta_backend.modules.tipoincidente.repository.TipoIncidenteRepository;
import com.utp_reporta_backend.modules.tipoincidente.service.command.TipoIncidenteCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoIncidenteCommandServiceImpl implements TipoIncidenteCommandService {
    private final TipoIncidenteRepository repository;
    private final TipoIncidenteMapper mapper;
    public TipoIncidenteResponse createTipoIncidente(TipoIncidenteRequest request) { return mapper.toResponse(repository.save(mapper.toEntity(request))); }
    public TipoIncidenteResponse updateTipoIncidente(Long id, TipoIncidenteRequest request) {
        return repository.findById(id).map(entity -> { mapper.updateEntity(request, entity); return mapper.toResponse(repository.save(entity)); }).orElse(null);
    }
    public void deleteTipoIncidente(Long id) { repository.deleteById(id); }
}
