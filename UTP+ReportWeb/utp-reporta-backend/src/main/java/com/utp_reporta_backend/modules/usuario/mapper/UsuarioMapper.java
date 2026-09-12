package com.utp_reporta_backend.modules.usuario.mapper;

import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    @Mapping(target = "sedeNombre", source = "sede.nombre")
    @Mapping(target = "zonasNombres", expression = "java(usuario.getZonas().stream().map(zona -> zona.getNombre()).toList())")
    @Mapping(target = "intentos", source = "intentosReporte")
    @Mapping(target = "roles", expression = "java(usuario.getRoles().stream().map(rol -> rol.getNombre().name()).toList())")
    UsuarioDTO toResponse(Usuario usuario);
}
