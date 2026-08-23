package com.utp_reporta_backend.modules.sede.service.command.impl;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.mapper.SedeMapper;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.sede.service.command.SedeCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SedeCommandServiceImpl implements SedeCommandService {
    private final SedeRepository repository;
    private final SedeMapper mapper;
    public SedeResponse crearSede(SedeRequest request) { return mapper.toResponse(repository.save(mapper.toEntity(request))); }
    public Optional<SedeResponse> actualizarSede(Long id, SedeRequest request) {
        return repository.findById(id).map(sede -> { mapper.updateEntity(request, sede); return mapper.toResponse(repository.save(sede)); });
    }
    public void eliminarSede(Long id) { repository.deleteById(id); }
}
