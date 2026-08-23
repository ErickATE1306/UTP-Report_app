package com.utp_reporta_backend.modules.sede.service.command;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import java.util.Optional;

public interface SedeCommandService {
    SedeResponse crearSede(SedeRequest request);
    Optional<SedeResponse> actualizarSede(Long id, SedeRequest request);
    void eliminarSede(Long id);
}
