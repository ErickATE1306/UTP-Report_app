package com.utp_reporta_backend.modules.zona.service;

import java.util.List;

import com.utp_reporta_backend.modules.zona.dto.request.ZonaRequest;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;


public interface ZonaService {
	List<ZonaResponse> obtenerTodasLasZonas();
	List<ZonaResponse> obtenerZonasPorSedeId(Long sedeId);
	/** Devuelve zonas por sede, opcionalmente incluyendo las inactivas */
	List<ZonaResponse> obtenerZonasPorSedeId(Long sedeId, boolean includeInactive);
	/** Devuelve todas las zonas, opcionalmente incluyendo inactivas */
	List<ZonaResponse> obtenerTodasLasZonas(boolean includeInactive);
	ZonaResponse crearZona(ZonaRequest request);
	ZonaResponse updateZona(Long id, ZonaRequest request);
	void deleteZona(Long id);
	/** Cambia el estado activo/desactivado de la zona */
	ZonaResponse setActivoZona(Long id, boolean activo);
}


