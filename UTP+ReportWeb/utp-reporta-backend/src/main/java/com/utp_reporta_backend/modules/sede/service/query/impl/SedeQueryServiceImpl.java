package com.utp_reporta_backend.modules.sede.service.query.impl;

import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.mapper.SedeMapper;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.sede.service.query.SedeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SedeQueryServiceImpl implements SedeQueryService {
    private final SedeRepository repository;
    private final SedeMapper mapper;
    public List<SedeResponse> obtenerTodasLasSedes() { return mapper.toResponseList(repository.findAll()); }
    public Optional<SedeResponse> obtenerSedePorId(Long id) { return repository.findById(id).map(mapper::toResponse); }
}
