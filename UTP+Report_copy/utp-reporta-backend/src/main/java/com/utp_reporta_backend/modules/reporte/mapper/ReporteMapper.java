package com.utp_reporta_backend.modules.reporte.mapper;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReporteMapper {
    @Mapping(target = "tipoIncidenteId", source = "tipoIncidente.id")
    @Mapping(target = "zonaId", source = "zona.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "seguridadAsignadoId", source = "seguridadAsignado.id")
    ReporteDTO toResponse(Reporte reporte);
}
