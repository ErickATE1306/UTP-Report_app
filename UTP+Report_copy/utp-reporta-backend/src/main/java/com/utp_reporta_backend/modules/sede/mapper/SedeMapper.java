package com.utp_reporta_backend.modules.sede.mapper;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.model.Sede;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SedeMapper {
    SedeResponse toResponse(Sede sede);
    List<SedeResponse> toResponseList(List<Sede> sedes);
    @Mapping(target = "id", ignore = true)
    Sede toEntity(SedeRequest request);
    @Mapping(target = "id", ignore = true)
    void updateEntity(SedeRequest request, @MappingTarget Sede sede);
}
