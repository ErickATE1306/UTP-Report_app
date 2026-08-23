package com.utp_reporta_backend.modules.tipoincidente.mapper;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoIncidenteMapper {
    TipoIncidenteResponse toResponse(TipoIncidente entity);
    List<TipoIncidenteResponse> toResponseList(List<TipoIncidente> entities);
    @Mapping(target = "id", ignore = true)
    TipoIncidente toEntity(TipoIncidenteRequest request);
    @Mapping(target = "id", ignore = true)
    void updateEntity(TipoIncidenteRequest request, @MappingTarget TipoIncidente entity);
}
