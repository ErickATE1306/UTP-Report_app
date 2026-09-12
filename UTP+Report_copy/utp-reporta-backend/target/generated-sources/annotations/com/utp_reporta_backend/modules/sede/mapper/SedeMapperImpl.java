package com.utp_reporta_backend.modules.sede.mapper;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.model.Sede;
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
public class SedeMapperImpl implements SedeMapper {

    @Override
    public SedeResponse toResponse(Sede sede) {
        if ( sede == null ) {
            return null;
        }

        SedeResponse sedeResponse = new SedeResponse();

        sedeResponse.setId( sede.getId() );
        sedeResponse.setNombre( sede.getNombre() );

        return sedeResponse;
    }

    @Override
    public List<SedeResponse> toResponseList(List<Sede> sedes) {
        if ( sedes == null ) {
            return null;
        }

        List<SedeResponse> list = new ArrayList<SedeResponse>( sedes.size() );
        for ( Sede sede : sedes ) {
            list.add( toResponse( sede ) );
        }

        return list;
    }

    @Override
    public Sede toEntity(SedeRequest request) {
        if ( request == null ) {
            return null;
        }

        Sede sede = new Sede();

        sede.setNombre( request.getNombre() );

        return sede;
    }

    @Override
    public void updateEntity(SedeRequest request, Sede sede) {
        if ( request == null ) {
            return;
        }

        sede.setNombre( request.getNombre() );
    }
}
