package com.utp_reporta_backend.modules.reporte.service.command.impl;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.reporte.repository.ReporteRepository;
import com.utp_reporta_backend.modules.tipoincidente.repository.TipoIncidenteRepository;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.dto.request.ReporteGestionRequest;
import com.utp_reporta_backend.modules.time.service.TimeService; // Import TimeService
import com.utp_reporta_backend.modules.reporte.service.ReporteGestionService;
import com.utp_reporta_backend.modules.notificacion.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import com.utp_reporta_backend.modules.reporte.model.enums.EstadoReporte;
import java.util.Optional;

import com.utp_reporta_backend.modules.usuario.model.enums.ERol;

import com.utp_reporta_backend.modules.reporte.service.command.ReporteCommandService;
import org.springframework.transaction.annotation.Transactional;
import com.utp_reporta_backend.modules.reporte.service.validation.ReporteFileValidator;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReporteCommandServiceImpl implements ReporteCommandService {
    private final ReporteRepository reporteRepository;
    private final TipoIncidenteRepository tipoIncidenteRepository;
    private final ZonaRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TimeService timeService;
    private final ReporteGestionService reporteGestionService;
    private final NotificationService notificationService;
    private final ReporteFileValidator fileValidator;

    @Override
    public ReporteDTO createReporte(Long tipoIncidenteId, Long zonaId, String descripcion, MultipartFile foto,
            Boolean isAnonimo, String contacto, Long usuarioId) {
        fileValidator.validate(foto);
        Reporte reporte = new Reporte();

        TipoIncidente tipoIncidente = tipoIncidenteRepository.findById(tipoIncidenteId)
                .orElseThrow(() -> new RuntimeException("TipoIncidente not found"));
        reporte.setTipoIncidente(tipoIncidente);

        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona not found"));
        reporte.setZona(zona);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario not found"));
        reporte.setUsuario(usuario);

        // Lógica para limitar reportes diarios para usuarios con rol "USUARIO"
        boolean isUserRole = usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombre().equals(ERol.ROLE_USUARIO));

        if (isUserRole) {
            LocalDate today = timeService.getCurrentLocalDatePeru(); // Use TimeService
            if (usuario.getFechaUltimoReporte() == null || !usuario.getFechaUltimoReporte().equals(today)) {
                usuario.setFechaUltimoReporte(today);
                usuario.setIntentosReporte(1);
            } else {
                if (usuario.getIntentosReporte() >= 3) {
                    throw new RuntimeException("Ha alcanzado el límite de 3 reportes por día.");
                }
                usuario.setIntentosReporte(usuario.getIntentosReporte() + 1);
            }
            usuarioRepository.save(usuario); // Guardar los cambios en el usuario
        }

        // fin de la lógica
        reporte.setDescripcion(descripcion);
        if (foto != null && !foto.isEmpty()) {
            try {
                reporte.setFoto(foto.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar la foto", e);
            }
        }
        reporte.setIsAnonimo(isAnonimo);
        reporte.setContacto(contacto);
        reporte.setFechaCreacion(timeService.getCurrentLocalDateTimePeru()); // Use TimeService

        Reporte savedReporte = reporteRepository.save(reporte);

        // Crear la gestión inicial en estado PENDIENTE para que el reporte tenga
        // reporteGestion desde su creación
        ReporteGestionDTO createdGestionDto = null;
        try {
            createdGestionDto = reporteGestionService.updateReporteGestion(savedReporte.getId(),
                    new ReporteGestionRequest(EstadoReporte.PENDIENTE, null, null));
            // After creating the ReporteGestion, we need to refresh the savedReporte object
            // to ensure it has the associated ReporteGestion before sending notifications.
            savedReporte = reporteRepository.findById(savedReporte.getId())
                                .orElseThrow(() -> new RuntimeException("Reporte not found after creating gestion"));

            // Notificar la creación del reporte solo si la gestión inicial fue exitosa
            notificationService.notifyNewReport(savedReporte, createdGestionDto);
        } catch (Exception e) {
            // No detener la creación del reporte por un fallo al crear la gestión; pero
            // loguear en futura iteración.
        }

        ReporteDTO savedDto = new ReporteDTO();
        savedDto.setId(savedReporte.getId());
        savedDto.setTipoIncidenteId(savedReporte.getTipoIncidente().getId());
        savedDto.setZonaId(savedReporte.getZona().getId());
        savedDto.setDescripcion(savedReporte.getDescripcion());
        savedDto.setFoto(savedReporte.getFoto());
        savedDto.setFechaCreacion(savedReporte.getFechaCreacion());
        savedDto.setIsAnonimo(savedReporte.getIsAnonimo());
        savedDto.setContacto(savedReporte.getContacto());
        savedDto.setUsuarioId(savedReporte.getUsuario().getId());
        // Si el servicio de gestión devolvió DTO, usarlo; si no, intentar reconsultar
        // la entidad para anexar la gestión
        if (createdGestionDto != null) {
            savedDto.setReporteGestion(createdGestionDto);
        } else {
            Optional<Reporte> maybe = reporteRepository.findById(savedReporte.getId());
            if (maybe.isPresent() && maybe.get().getReporteGestion() != null) {
                ReporteGestionDTO gestionDTO = new ReporteGestionDTO();
                gestionDTO.setId(maybe.get().getReporteGestion().getId());
                gestionDTO.setEstado(maybe.get().getReporteGestion().getEstado());
                gestionDTO.setPrioridad(maybe.get().getReporteGestion().getPrioridad());
                gestionDTO.setFechaActualizacion(maybe.get().getReporteGestion().getFechaActualizacion());
                savedDto.setReporteGestion(gestionDTO);
            }
        }
        return savedDto;
    }

    @Override
    public ReporteDTO updateReporte(Long id, Long tipoIncidenteId, Long zonaId, String descripcion, MultipartFile foto,
            Boolean isAnonimo, String contacto, Long usuarioId) {
        fileValidator.validate(foto);
        Optional<Reporte> reporte = reporteRepository.findById(id);
        if (reporte.isPresent()) {
            Reporte existingReporte = reporte.get();

            tipoIncidenteRepository.findById(tipoIncidenteId)
                    .ifPresent(existingReporte::setTipoIncidente);
            zonaRepository.findById(zonaId).ifPresent(existingReporte::setZona);
            usuarioRepository.findById(usuarioId).ifPresent(existingReporte::setUsuario);

            existingReporte.setDescripcion(descripcion);
            if (foto != null && !foto.isEmpty()) {
                try {
                    existingReporte.setFoto(foto.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("Error al procesar la foto", e);
                }
            }
            existingReporte.setIsAnonimo(isAnonimo);
            existingReporte.setContacto(contacto);

            Reporte updatedReporte = reporteRepository.save(existingReporte);

            ReporteDTO updatedDto = new ReporteDTO();
            updatedDto.setId(updatedReporte.getId());
            updatedDto.setTipoIncidenteId(updatedReporte.getTipoIncidente().getId());
            updatedDto.setZonaId(updatedReporte.getZona().getId());
            updatedDto.setDescripcion(updatedReporte.getDescripcion());
            updatedDto.setFoto(updatedReporte.getFoto());
            updatedDto.setFechaCreacion(updatedReporte.getFechaCreacion());
            updatedDto.setIsAnonimo(updatedReporte.getIsAnonimo());
            updatedDto.setContacto(updatedReporte.getContacto());
            updatedDto.setUsuarioId(updatedReporte.getUsuario().getId());
            return updatedDto;
        }
        return null;
    }

    @Override
    public void deleteReporte(Long id) {
        reporteRepository.deleteById(id);
    }

}
