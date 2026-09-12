package com.utp_reporta_backend.modules.tipoincidente.mapper;

import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-23T14:34:48-0500",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TipoIncidenteMapperImpl implements TipoIncidenteMapper {

    @Override
    public TipoIncidenteResponse toResponse(TipoIncidente entity) {
        if ( entity == null ) {
            return null;
        }

        TipoIncidenteResponse tipoIncidenteResponse = new TipoIncidenteResponse();

        tipoIncidenteResponse.setDescripcion( entity.getDescripcion() );
        tipoIncidenteResponse.setId( entity.getId() );
        tipoIncidenteResponse.setNombre( entity.getNombre() );

        return tipoIncidenteResponse;
    }

    @Override
    public List<TipoIncidenteResponse> toResponseList(List<TipoIncidente> entities) {
        if ( entities == null ) {
            return null;
        }

        List<TipoIncidenteResponse> list = new ArrayList<TipoIncidenteResponse>( entities.size() );
        for ( TipoIncidente tipoIncidente : entities ) {
            list.add( toResponse( tipoIncidente ) );
        }

        return list;
    }

    @Override
    public TipoIncidente toEntity(TipoIncidenteRequest request) {
        if ( request == null ) {
            return null;
        }

        TipoIncidente tipoIncidente = new TipoIncidente();

        tipoIncidente.setDescripcion( request.getDescripcion() );
        tipoIncidente.setNombre( request.getNombre() );

        return tipoIncidente;
    }

    @Override
    public void updateEntity(TipoIncidenteRequest request, TipoIncidente entity) {
        if ( request == null ) {
            return;
        }

        entity.setDescripcion( request.getDescripcion() );
        entity.setNombre( request.getNombre() );
    }
}
