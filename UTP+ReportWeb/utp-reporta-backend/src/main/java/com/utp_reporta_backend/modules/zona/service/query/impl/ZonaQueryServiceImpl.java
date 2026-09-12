package com.utp_reporta_backend.modules.zona.service.query.impl;

import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.mapper.ZonaMapper;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;
import com.utp_reporta_backend.modules.zona.service.query.ZonaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZonaQueryServiceImpl implements ZonaQueryService {
    private final ZonaRepository repository;
    private final ZonaMapper mapper;
    public List<ZonaResponse> obtenerTodasLasZonas(boolean includeInactive) {
        List<Zona> zonas = includeInactive ? repository.findAll() : repository.findAllByActivoTrue();
        return mapper.toResponseList(zonas);
    }
    public List<ZonaResponse> obtenerZonasPorSedeId(Long sedeId, boolean includeInactive) {
        List<Zona> zonas = includeInactive ? repository.findBySedeId(sedeId) : repository.findBySedeIdAndActivoTrue(sedeId);
        return mapper.toResponseList(zonas);
    }
}
