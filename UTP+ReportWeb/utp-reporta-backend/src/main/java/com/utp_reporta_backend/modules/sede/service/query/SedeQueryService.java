package com.utp_reporta_backend.modules.sede.service.query;

import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import java.util.List;
import java.util.Optional;

public interface SedeQueryService {
    List<SedeResponse> obtenerTodasLasSedes();
    Optional<SedeResponse> obtenerSedePorId(Long id);
}
