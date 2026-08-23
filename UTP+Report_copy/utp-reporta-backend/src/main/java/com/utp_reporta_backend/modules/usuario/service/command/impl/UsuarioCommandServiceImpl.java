package com.utp_reporta_backend.modules.usuario.service.command.impl;

import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.utp_reporta_backend.modules.usuario.service.command.UsuarioCommandService;
import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioCommandServiceImpl implements UsuarioCommandService {
    private final UsuarioRepository usuarioRepository;
    private final SedeRepository sedeRepository;
    private final ZonaRepository zonaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioDTO updateUsuarioEnabledStatus(Long id, boolean enabled) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setEnabled(enabled);
                    Usuario updatedUsuario = usuarioRepository.save(usuario);
                    UsuarioDTO usuarioDTO = new UsuarioDTO();
                    usuarioDTO.setId(updatedUsuario.getId());
                    usuarioDTO.setNombreCompleto(updatedUsuario.getNombreCompleto());
                    usuarioDTO.setUsername(updatedUsuario.getUsername());
                    usuarioDTO.setCorreo(updatedUsuario.getCorreo());
                    usuarioDTO.setTelefono(updatedUsuario.getTelefono());
                    usuarioDTO.setTipoUsuario(updatedUsuario.getTipoUsuario());
                    usuarioDTO.setSedeNombre(updatedUsuario.getSede() != null ? updatedUsuario.getSede().getNombre() : null);
                    usuarioDTO.setZonasNombres(updatedUsuario.getZonas().stream()
                            .map(zona -> zona.getNombre())
                            .collect(Collectors.toList()));
                    usuarioDTO.setIntentos(updatedUsuario.getIntentosReporte());
                    usuarioDTO.setFechaUltimoReporte(updatedUsuario.getFechaUltimoReporte());
                    usuarioDTO.setEnabled(updatedUsuario.isEnabled());
                    usuarioDTO.setRoles(updatedUsuario.getRoles().stream()
                            .map(rol -> rol.getNombre().name())
                            .collect(Collectors.toList()));
                    return usuarioDTO;
                })
                .orElse(null); // Return null if user not found
    }

    @Override
    public UsuarioDTO updateUsuario(Long id, UsuarioUpdateRequest request, String password) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isEmpty()) {
            return null; // Usuario no encontrado
        }

        Usuario usuario = usuarioOptional.get();

        // Actualizar campos básicos
        if (request.getNombreCompleto() != null) {
            usuario.setNombreCompleto(request.getNombreCompleto());
        }
        if (request.getCorreo() != null) {
            usuario.setCorreo(request.getCorreo());
        }
        if (request.getTelefono() != null && !request.getTelefono().equals(usuario.getTelefono())) {
            // Validate if the new phone number is unique for other users
            if (usuarioRepository.existsByTelefonoAndIdNot(request.getTelefono(), id)) {
                return null; // Indicate validation failure by returning null
            }
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getTipoUsuario() != null) {
            usuario.setTipoUsuario(request.getTipoUsuario());
        }
        // Actualizar contraseña si se proporciona y no está vacía
        if (password != null && !password.isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        // Actualizar sede
        if (request.getSedeNombre() != null) {
            Optional<Sede> sedeOptional = sedeRepository.findByNombre(request.getSedeNombre());
            sedeOptional.ifPresent(usuario::setSede);
        }

        // Actualizar zonas (solo si el usuario tiene rol de seguridad)
        if (usuario.getRoles().stream().anyMatch(rol -> rol.getNombre().equals(ERol.ROLE_SEGURIDAD))) {
            if (request.getZonasNombres() != null) {
                List<Zona> nuevasZonasList = request.getZonasNombres().stream()
                        .map(zonaNombre -> zonaRepository.findByNombre(zonaNombre))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                usuario.setZonas(new java.util.HashSet<>(nuevasZonasList)); // Convert List to Set
            }
        }
        
        // Guardar el usuario actualizado
        Usuario updatedUsuario = usuarioRepository.save(usuario);

        // Convertir a DTO y retornar
        UsuarioDTO resultDTO = new UsuarioDTO();
        resultDTO.setId(updatedUsuario.getId());
        resultDTO.setNombreCompleto(updatedUsuario.getNombreCompleto());
        resultDTO.setUsername(updatedUsuario.getUsername());
        resultDTO.setCorreo(updatedUsuario.getCorreo());
        resultDTO.setTelefono(updatedUsuario.getTelefono());
        resultDTO.setTipoUsuario(updatedUsuario.getTipoUsuario());
        resultDTO.setSedeNombre(updatedUsuario.getSede() != null ? updatedUsuario.getSede().getNombre() : null);
        resultDTO.setZonasNombres(updatedUsuario.getZonas().stream()
                .map(zona -> zona.getNombre())
                .collect(Collectors.toList()));
        resultDTO.setIntentos(updatedUsuario.getIntentosReporte());
        resultDTO.setFechaUltimoReporte(updatedUsuario.getFechaUltimoReporte());
        resultDTO.setEnabled(updatedUsuario.isEnabled());
        resultDTO.setRoles(updatedUsuario.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toList()));
        return resultDTO;
    }
}
