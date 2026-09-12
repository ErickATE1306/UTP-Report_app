package com.utp_reporta_backend.modules.reporte.mapper;

import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteGestionDTO;
import com.utp_reporta_backend.modules.reporte.model.Reporte;
import com.utp_reporta_backend.modules.reporte.model.ReporteGestion;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.zona.model.Zona;
import java.util.Arrays;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-23T14:34:48-0500",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ReporteMapperImpl implements ReporteMapper {

    @Override
    public ReporteDTO toResponse(Reporte reporte) {
        if ( reporte == null ) {
            return null;
        }

        ReporteDTO reporteDTO = new ReporteDTO();

        reporteDTO.setTipoIncidenteId( reporteTipoIncidenteId( reporte ) );
        reporteDTO.setZonaId( reporteZonaId( reporte ) );
        reporteDTO.setUsuarioId( reporteUsuarioId( reporte ) );
        reporteDTO.setSeguridadAsignadoId( reporteSeguridadAsignadoId( reporte ) );
        reporteDTO.setContacto( reporte.getContacto() );
        reporteDTO.setDescripcion( reporte.getDescripcion() );
        reporteDTO.setFechaCreacion( reporte.getFechaCreacion() );
        byte[] foto = reporte.getFoto();
        if ( foto != null ) {
            reporteDTO.setFoto( Arrays.copyOf( foto, foto.length ) );
        }
        reporteDTO.setId( reporte.getId() );
        reporteDTO.setIsAnonimo( reporte.getIsAnonimo() );
        reporteDTO.setMensajeAdmin( reporte.getMensajeAdmin() );
        reporteDTO.setMensajeSeguridad( reporte.getMensajeSeguridad() );
        reporteDTO.setReporteGestion( reporteGestionToReporteGestionDTO( reporte.getReporteGestion() ) );

        return reporteDTO;
    }

    private Long reporteTipoIncidenteId(Reporte reporte) {
        TipoIncidente tipoIncidente = reporte.getTipoIncidente();
        if ( tipoIncidente == null ) {
            return null;
        }
        return tipoIncidente.getId();
    }

    private Long reporteZonaId(Reporte reporte) {
        Zona zona = reporte.getZona();
        if ( zona == null ) {
            return null;
        }
        return zona.getId();
    }

    private Long reporteUsuarioId(Reporte reporte) {
        Usuario usuario = reporte.getUsuario();
        if ( usuario == null ) {
            return null;
        }
        return usuario.getId();
    }

    private Long reporteSeguridadAsignadoId(Reporte reporte) {
        Usuario seguridadAsignado = reporte.getSeguridadAsignado();
        if ( seguridadAsignado == null ) {
            return null;
        }
        return seguridadAsignado.getId();
    }

    protected ReporteGestionDTO reporteGestionToReporteGestionDTO(ReporteGestion reporteGestion) {
        if ( reporteGestion == null ) {
            return null;
        }

        ReporteGestionDTO reporteGestionDTO = new ReporteGestionDTO();

        reporteGestionDTO.setEstado( reporteGestion.getEstado() );
        reporteGestionDTO.setFechaActualizacion( reporteGestion.getFechaActualizacion() );
        reporteGestionDTO.setId( reporteGestion.getId() );
        reporteGestionDTO.setPrioridad( reporteGestion.getPrioridad() );

        return reporteGestionDTO;
    }
}
