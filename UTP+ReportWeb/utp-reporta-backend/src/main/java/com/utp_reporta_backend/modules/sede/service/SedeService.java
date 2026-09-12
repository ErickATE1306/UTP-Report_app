package com.utp_reporta_backend.modules.sede.service;

import java.util.List;
import java.util.Optional;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;

public interface SedeService {
	List<SedeResponse> obtenerTodasLasSedes();
	Optional<SedeResponse> obtenerSedePorId(Long id);
	SedeResponse crearSede(SedeRequest request);
	Optional<SedeResponse> actualizarSede(Long id, SedeRequest request);
	void eliminarSede(Long id);
}


