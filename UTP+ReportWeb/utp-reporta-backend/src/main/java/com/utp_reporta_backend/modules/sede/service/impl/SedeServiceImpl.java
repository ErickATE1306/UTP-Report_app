package com.utp_reporta_backend.modules.sede.service.impl;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.service.SedeService;
import com.utp_reporta_backend.modules.sede.service.command.SedeCommandService;
import com.utp_reporta_backend.modules.sede.service.query.SedeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements SedeService {
    private final SedeQueryService queryService;
    private final SedeCommandService commandService;
    public List<SedeResponse> obtenerTodasLasSedes() { return queryService.obtenerTodasLasSedes(); }
    public Optional<SedeResponse> obtenerSedePorId(Long id) { return queryService.obtenerSedePorId(id); }
    public SedeResponse crearSede(SedeRequest request) { return commandService.crearSede(request); }
    public Optional<SedeResponse> actualizarSede(Long id, SedeRequest request) { return commandService.actualizarSede(id, request); }
    public void eliminarSede(Long id) { commandService.eliminarSede(id); }
}
