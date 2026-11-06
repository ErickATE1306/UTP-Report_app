package com.utp_reporta_backend.modules.catalogos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utp_reporta_backend.modules.sede.dto.request.SedeRequest;
import com.utp_reporta_backend.modules.sede.dto.response.SedeResponse;
import com.utp_reporta_backend.modules.sede.mapper.SedeMapper;
import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.sede.service.command.impl.SedeCommandServiceImpl;
import com.utp_reporta_backend.modules.sede.service.query.impl.SedeQueryServiceImpl;
import com.utp_reporta_backend.modules.tipoincidente.dto.request.TipoIncidenteRequest;
import com.utp_reporta_backend.modules.tipoincidente.dto.response.TipoIncidenteResponse;
import com.utp_reporta_backend.modules.tipoincidente.mapper.TipoIncidenteMapper;
import com.utp_reporta_backend.modules.tipoincidente.model.TipoIncidente;
import com.utp_reporta_backend.modules.tipoincidente.repository.TipoIncidenteRepository;
import com.utp_reporta_backend.modules.tipoincidente.service.command.impl.TipoIncidenteCommandServiceImpl;
import com.utp_reporta_backend.modules.tipoincidente.service.query.impl.TipoIncidenteQueryServiceImpl;

class SedeYTipoIncidenteServicesTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("SedeCommandServiceImpl")
    class SedeCommand {
        @Mock private SedeRepository repository;
        @Mock private SedeMapper mapper;
        @InjectMocks private SedeCommandServiceImpl service;

        private final SedeRequest request = new SedeRequest();
        private final Sede sede = new Sede(1L, "Lima");
        private final SedeResponse respuesta = new SedeResponse(1L, "Lima");

        @Test
        @DisplayName("crearSede guarda la entidad y devuelve la respuesta")
        void crea() {
            when(mapper.toEntity(request)).thenReturn(sede);
            when(repository.save(sede)).thenReturn(sede);
            when(mapper.toResponse(sede)).thenReturn(respuesta);

            assertSame(respuesta, service.crearSede(request));
        }

        @Test
        @DisplayName("actualizarSede modifica una sede existente")
        void actualiza() {
            when(repository.findById(1L)).thenReturn(Optional.of(sede));
            when(repository.save(sede)).thenReturn(sede);
            when(mapper.toResponse(sede)).thenReturn(respuesta);

            assertEquals(Optional.of(respuesta), service.actualizarSede(1L, request));
            verify(mapper).updateEntity(request, sede);
        }

        @Test
        @DisplayName("actualizarSede devuelve vacío si la sede no existe")
        void actualizaInexistente() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertTrue(service.actualizarSede(99L, request).isEmpty());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("eliminarSede elimina por id")
        void elimina() {
            service.eliminarSede(1L);

            verify(repository).deleteById(1L);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("SedeQueryServiceImpl")
    class SedeQuery {
        @Mock private SedeRepository repository;
        @Mock private SedeMapper mapper;
        @InjectMocks private SedeQueryServiceImpl service;

        @Test
        @DisplayName("lista todas las sedes")
        void lista() {
            List<Sede> sedes = List.of(new Sede(1L, "Lima"));
            List<SedeResponse> respuesta = List.of(new SedeResponse(1L, "Lima"));
            when(repository.findAll()).thenReturn(sedes);
            when(mapper.toResponseList(sedes)).thenReturn(respuesta);

            assertSame(respuesta, service.obtenerTodasLasSedes());
        }

        @Test
        @DisplayName("busca una sede por id")
        void porId() {
            Sede sede = new Sede(1L, "Lima");
            SedeResponse respuesta = new SedeResponse(1L, "Lima");
            when(repository.findById(1L)).thenReturn(Optional.of(sede));
            when(mapper.toResponse(sede)).thenReturn(respuesta);

            assertEquals(Optional.of(respuesta), service.obtenerSedePorId(1L));
        }

        @Test
        @DisplayName("devuelve vacío si la sede no existe")
        void porIdInexistente() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertTrue(service.obtenerSedePorId(99L).isEmpty());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("TipoIncidenteCommandServiceImpl")
    class TipoCommand {
        @Mock private TipoIncidenteRepository repository;
        @Mock private TipoIncidenteMapper mapper;
        @InjectMocks private TipoIncidenteCommandServiceImpl service;

        private final TipoIncidenteRequest request = new TipoIncidenteRequest();
        private final TipoIncidente tipo = new TipoIncidente(1L, "Robo", null);
        private final TipoIncidenteResponse respuesta = new TipoIncidenteResponse(1L, "Robo", null);

        @Test
        @DisplayName("createTipoIncidente guarda la entidad y devuelve la respuesta")
        void crea() {
            when(mapper.toEntity(request)).thenReturn(tipo);
            when(repository.save(tipo)).thenReturn(tipo);
            when(mapper.toResponse(tipo)).thenReturn(respuesta);

            assertSame(respuesta, service.createTipoIncidente(request));
        }

        @Test
        @DisplayName("updateTipoIncidente modifica un tipo existente")
        void actualiza() {
            when(repository.findById(1L)).thenReturn(Optional.of(tipo));
            when(repository.save(tipo)).thenReturn(tipo);
            when(mapper.toResponse(tipo)).thenReturn(respuesta);

            assertSame(respuesta, service.updateTipoIncidente(1L, request));
            verify(mapper).updateEntity(request, tipo);
        }

        @Test
        @DisplayName("updateTipoIncidente devuelve null si el tipo no existe")
        void actualizaInexistente() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertNull(service.updateTipoIncidente(99L, request));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("deleteTipoIncidente elimina por id")
        void elimina() {
            service.deleteTipoIncidente(1L);

            verify(repository).deleteById(1L);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("TipoIncidenteQueryServiceImpl")
    class TipoQuery {
        @Mock private TipoIncidenteRepository repository;
        @Mock private TipoIncidenteMapper mapper;
        @InjectMocks private TipoIncidenteQueryServiceImpl service;

        @Test
        @DisplayName("lista todos los tipos")
        void lista() {
            List<TipoIncidente> tipos = List.of(new TipoIncidente(1L, "Robo", null));
            List<TipoIncidenteResponse> respuesta = List.of(new TipoIncidenteResponse(1L, "Robo", null));
            when(repository.findAll()).thenReturn(tipos);
            when(mapper.toResponseList(tipos)).thenReturn(respuesta);

            assertSame(respuesta, service.getAllTipoIncidentes());
        }

        @Test
        @DisplayName("devuelve null si el tipo no existe")
        void porIdInexistente() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertNull(service.getTipoIncidenteById(99L));
        }
    }
}
