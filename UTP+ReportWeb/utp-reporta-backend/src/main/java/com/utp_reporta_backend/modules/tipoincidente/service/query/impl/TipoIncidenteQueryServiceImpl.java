package com.utp_reporta_backend.modules.tipoincidente.service.query.impl;

import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import com.utp_reporta_backend.modules.tipoincidente.mapper.TipoIncidenteMapper;
import com.utp_reporta_backend.modules.tipoincidente.repository.TipoIncidenteRepository;
import com.utp_reporta_backend.modules.tipoincidente.service.query.TipoIncidenteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoIncidenteQueryServiceImpl implements TipoIncidenteQueryService {
    private final TipoIncidenteRepository repository;
    private final TipoIncidenteMapper mapper;
    public List<TipoIncidenteResponse> getAllTipoIncidentes() { return mapper.toResponseList(repository.findAll()); }
    public TipoIncidenteResponse getTipoIncidenteById(Long id) { return repository.findById(id).map(mapper::toResponse).orElse(null); }
}
