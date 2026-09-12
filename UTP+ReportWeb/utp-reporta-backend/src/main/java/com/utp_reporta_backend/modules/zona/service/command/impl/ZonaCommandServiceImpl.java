package com.utp_reporta_backend.modules.zona.service.command.impl;
import com.utp_reporta_backend.common.exception.ResourceNotFoundException;

import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.zona.dto.request.ZonaRequest;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.mapper.ZonaMapper;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;
import com.utp_reporta_backend.modules.zona.service.command.ZonaCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class ZonaCommandServiceImpl implements ZonaCommandService {
    private final ZonaRepository zonaRepository;
    private final SedeRepository sedeRepository;
    private final ZonaMapper mapper;
    public ZonaResponse crearZona(ZonaRequest request) {
        Zona zona = new Zona();
        zona.setNombre(request.getNombre()); zona.setDescripcion(request.getDescripcion()); zona.setSede(getSede(request.getSedeId()));
        setFoto(zona, request, false); return mapper.toResponse(zonaRepository.save(zona));
    }
    public ZonaResponse updateZona(Long id, ZonaRequest request) {
        Zona zona = getZona(id); zona.setNombre(request.getNombre()); zona.setDescripcion(request.getDescripcion()); zona.setSede(getSede(request.getSedeId()));
        setFoto(zona, request, true); return mapper.toResponse(zonaRepository.save(zona));
    }
    public void deleteZona(Long id) { getZona(id); zonaRepository.deleteById(id); }
    public ZonaResponse setActivoZona(Long id, boolean activo) { Zona zona=getZona(id); zona.setActivo(activo); return mapper.toResponse(zonaRepository.save(zona)); }
    private Zona getZona(Long id) { return zonaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + id)); }
    private Sede getSede(Long id) { return sedeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada con ID: " + id)); }
    private void setFoto(Zona zona, ZonaRequest request, boolean clearWhenNull) {
        if (request.getFoto()!=null && !request.getFoto().isEmpty()) { try { zona.setFoto(request.getFoto().getBytes()); } catch(IOException ex) { throw new RuntimeException("Error al procesar la foto", ex); } }
        else if (clearWhenNull && request.getFoto()==null) { zona.setFoto(null); }
    }
}
