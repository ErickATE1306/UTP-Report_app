package com.utp_reporta_backend.modules.usuario.mapper;

import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-23T14:34:47-0500",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public UsuarioDTO toResponse(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setSedeNombre( usuarioSedeNombre( usuario ) );
        usuarioDTO.setIntentos( usuario.getIntentosReporte() );
        usuarioDTO.setCorreo( usuario.getCorreo() );
        usuarioDTO.setEnabled( usuario.isEnabled() );
        usuarioDTO.setFechaUltimoReporte( usuario.getFechaUltimoReporte() );
        usuarioDTO.setId( usuario.getId() );
        usuarioDTO.setNombreCompleto( usuario.getNombreCompleto() );
        usuarioDTO.setTelefono( usuario.getTelefono() );
        usuarioDTO.setTipoUsuario( usuario.getTipoUsuario() );
        usuarioDTO.setUsername( usuario.getUsername() );

        usuarioDTO.setZonasNombres( usuario.getZonas().stream().map(zona -> zona.getNombre()).toList() );
        usuarioDTO.setRoles( usuario.getRoles().stream().map(rol -> rol.getNombre().name()).toList() );

        return usuarioDTO;
    }

    private String usuarioSedeNombre(Usuario usuario) {
        Sede sede = usuario.getSede();
        if ( sede == null ) {
            return null;
        }
        return sede.getNombre();
    }
}
