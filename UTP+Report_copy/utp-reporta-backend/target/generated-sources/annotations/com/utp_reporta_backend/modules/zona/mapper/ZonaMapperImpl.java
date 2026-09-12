package com.utp_reporta_backend.modules.zona.mapper;

import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.model.Zona;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-23T14:34:48-0500",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ZonaMapperImpl implements ZonaMapper {

    @Override
    public ZonaResponse toResponse(Zona zona) {
        if ( zona == null ) {
            return null;
        }

        ZonaResponse zonaResponse = new ZonaResponse();

        zonaResponse.setSedeId( zonaSedeId( zona ) );
        zonaResponse.setActivo( zona.isActivo() );
        zonaResponse.setDescripcion( zona.getDescripcion() );
        zonaResponse.setEstado( zona.getEstado() );
        byte[] foto = zona.getFoto();
        if ( foto != null ) {
            zonaResponse.setFoto( Arrays.copyOf( foto, foto.length ) );
        }
        zonaResponse.setId( zona.getId() );
        zonaResponse.setNombre( zona.getNombre() );
        zonaResponse.setReportCount( zona.getReportCount() );

        return zonaResponse;
    }

    @Override
    public List<ZonaResponse> toResponseList(List<Zona> zonas) {
        if ( zonas == null ) {
            return null;
        }

        List<ZonaResponse> list = new ArrayList<ZonaResponse>( zonas.size() );
        for ( Zona zona : zonas ) {
            list.add( toResponse( zona ) );
        }

        return list;
    }

    private Long zonaSedeId(Zona zona) {
        Sede sede = zona.getSede();
        if ( sede == null ) {
            return null;
        }
        return sede.getId();
    }
}
