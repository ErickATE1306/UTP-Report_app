package com.utp_reporta_backend.modules.zona.service.impl;
import com.utp_reporta_backend.modules.zona.dto.request.ZonaRequest;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.service.ZonaService;
import com.utp_reporta_backend.modules.zona.service.command.ZonaCommandService;
import com.utp_reporta_backend.modules.zona.service.query.ZonaQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class ZonaServiceImpl implements ZonaService {
    private final ZonaQueryService queryService; private final ZonaCommandService commandService;
    public List<ZonaResponse> obtenerTodasLasZonas(){return queryService.obtenerTodasLasZonas(false);}
    public List<ZonaResponse> obtenerTodasLasZonas(boolean value){return queryService.obtenerTodasLasZonas(value);}
    public List<ZonaResponse> obtenerZonasPorSedeId(Long id){return queryService.obtenerZonasPorSedeId(id,false);}
    public List<ZonaResponse> obtenerZonasPorSedeId(Long id,boolean value){return queryService.obtenerZonasPorSedeId(id,value);}
    public ZonaResponse crearZona(ZonaRequest request){return commandService.crearZona(request);}
    public ZonaResponse updateZona(Long id,ZonaRequest request){return commandService.updateZona(id,request);}
    public void deleteZona(Long id){commandService.deleteZona(id);}
    public ZonaResponse setActivoZona(Long id,boolean activo){return commandService.setActivoZona(id,activo);}
}
