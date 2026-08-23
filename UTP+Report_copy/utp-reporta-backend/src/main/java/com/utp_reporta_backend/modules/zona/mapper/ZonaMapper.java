package com.utp_reporta_backend.modules.zona.mapper;

import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.model.Zona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ZonaMapper {
    @Mapping(target = "sedeId", source = "sede.id")
    ZonaResponse toResponse(Zona zona);
    List<ZonaResponse> toResponseList(List<Zona> zonas);
}
