package com.utp_reporta_backend.modules.zona.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.utp_reporta_backend.common.exception.BadRequestException;
import com.utp_reporta_backend.common.exception.ResourceNotFoundException;
import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.time.service.TimeService;
import com.utp_reporta_backend.modules.zona.dto.request.ZonaRequest;
import com.utp_reporta_backend.modules.zona.dto.response.ZonaResponse;
import com.utp_reporta_backend.modules.zona.mapper.ZonaMapper;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.model.enums.EstadoZona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;
import com.utp_reporta_backend.modules.zona.service.command.impl.ZonaCommandServiceImpl;
import com.utp_reporta_backend.modules.zona.service.query.impl.ZonaQueryServiceImpl;

class ZonaServicesTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ZonaCommandServiceImpl")
    class Command {
        @Mock private ZonaRepository zonaRepository;
        @Mock private SedeRepository sedeRepository;
        @Mock private ZonaMapper mapper;
        @InjectMocks private ZonaCommandServiceImpl service;

        private final Sede sede = new Sede(1L, "Lima Centro");
        private final ZonaResponse respuesta = new ZonaResponse();
        private Zona zona;

        @BeforeEach
        void preparar() {
            zona = new Zona();
            zona.setId(5L);
            zona.setFoto(new byte[] {9});
            lenient().when(zonaRepository.findById(5L)).thenReturn(Optional.of(zona));
            lenient().when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
            lenient().when(zonaRepository.save(any(Zona.class))).thenAnswer(i -> i.getArgument(0));
            lenient().when(mapper.toResponse(any(Zona.class))).thenReturn(respuesta);
        }

        private ZonaRequest request(MultipartFile foto) {
            return new ZonaRequest("Patio", "Central", foto, 1L);
        }

        @Test
        @DisplayName("crearZona guarda los datos, la sede y la foto")
        void crea() {
            byte[] bytes = {1, 2};

            assertSame(respuesta, service.crearZona(request(new MockMultipartFile("foto", bytes))));

            org.mockito.ArgumentCaptor<Zona> captor = org.mockito.ArgumentCaptor.forClass(Zona.class);
            verify(zonaRepository).save(captor.capture());
            assertEquals("Patio", captor.getValue().getNombre());
            assertEquals("Central", captor.getValue().getDescripcion());
            assertEquals(sede, captor.getValue().getSede());
            assertArrayEquals(bytes, captor.getValue().getFoto());
        }

        @Test
        @DisplayName("una zona nueva inicia activa, segura y sin reportes")
        void valoresIniciales() {
            service.crearZona(request(null));

            org.mockito.ArgumentCaptor<Zona> captor = org.mockito.ArgumentCaptor.forClass(Zona.class);
            verify(zonaRepository).save(captor.capture());
            Zona creada = captor.getValue();
            assertEquals("Patio", creada.getNombre());
            assertEquals(sede, creada.getSede());
            assertTrue(creada.isActivo());
            assertEquals(EstadoZona.ZONA_SEGURA, creada.getEstado());
            assertEquals(0, creada.getReportCount());
        }

        @Test
        @DisplayName("crearZona acepta 255 caracteres y rechaza 256 (F09)")
        void creaConLimiteDeDescripcion() {
            service.crearZona(new ZonaRequest("Patio", "x".repeat(255), null, 1L));
            verify(zonaRepository).save(any(Zona.class));

            assertThrows(BadRequestException.class, () -> service.crearZona(new ZonaRequest("Patio", "x".repeat(256), null, 1L)));
            verify(zonaRepository).save(any(Zona.class));
        }

        @Test
        @DisplayName("updateZona rechaza una descripción de 256 caracteres (F09)")
        void actualizaConDescripcionLarga() {
            assertThrows(BadRequestException.class, () -> service.updateZona(1L, new ZonaRequest("Patio", "x".repeat(256), null, 1L)));
        }

        @Test
        @DisplayName("crearZona falla si la sede no existe")
        void creaSinSede() {
            ZonaRequest request = new ZonaRequest("Patio", null, null, 99L);
            when(sedeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.crearZona(request));
            verify(zonaRepository, never()).save(any());
        }

        @Test
        @DisplayName("falla si la foto no se puede leer")
        void fotoIlegible() throws IOException {
            MultipartFile foto = mock(MultipartFile.class);
            when(foto.isEmpty()).thenReturn(false);
            when(foto.getBytes()).thenThrow(new IOException("x"));

            RuntimeException error = assertThrows(RuntimeException.class, () -> service.crearZona(request(foto)));

            assertEquals("Error al procesar la foto", error.getMessage());
        }

        @Test
        @DisplayName("updateZona falla si la zona no existe")
        void actualizaInexistente() {
            when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.updateZona(99L, request(null)));
        }

        @Test
        @DisplayName("updateZona reemplaza la foto si llega una nueva")
        void reemplazaFoto() {
            service.updateZona(5L, request(new MockMultipartFile("foto", new byte[] {1})));

            assertArrayEquals(new byte[] {1}, zona.getFoto());
            assertEquals("Patio", zona.getNombre());
        }

        @Test
        @DisplayName("updateZona elimina la foto si llega nula")
        void eliminaFoto() {
            service.updateZona(5L, request(null));

            assertNull(zona.getFoto());
        }

        @Test
        @DisplayName("updateZona conserva la foto si llega vacía")
        void conservaFoto() {
            service.updateZona(5L, request(new MockMultipartFile("foto", new byte[0])));

            assertArrayEquals(new byte[] {9}, zona.getFoto());
        }

        @Test
        @DisplayName("deleteZona elimina una zona existente")
        void elimina() {
            service.deleteZona(5L);

            verify(zonaRepository).deleteById(5L);
        }

        @Test
        @DisplayName("deleteZona falla si la zona no existe")
        void eliminaInexistente() {
            when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.deleteZona(99L));
            verify(zonaRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("setActivoZona cambia el estado lógico")
        void cambiaActivo() {
            service.setActivoZona(5L, false);

            assertFalse(zona.isActivo());
            verify(zonaRepository).save(zona);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ZonaQueryServiceImpl")
    class Query {
        @Mock private ZonaRepository repository;
        @Mock private ZonaMapper mapper;
        @InjectMocks private ZonaQueryServiceImpl service;

        private final List<Zona> zonas = List.of(new Zona());
        private final List<ZonaResponse> respuesta = List.of(new ZonaResponse());

        @Test
        @DisplayName("por defecto lista solo las zonas activas")
        void soloActivas() {
            when(repository.findAllByActivoTrue()).thenReturn(zonas);
            when(mapper.toResponseList(zonas)).thenReturn(respuesta);

            assertSame(respuesta, service.obtenerTodasLasZonas(false));
            verify(repository, never()).findAll();
        }

        @Test
        @DisplayName("puede incluir las zonas inactivas")
        void incluyeInactivas() {
            when(repository.findAll()).thenReturn(zonas);
            when(mapper.toResponseList(zonas)).thenReturn(respuesta);

            assertSame(respuesta, service.obtenerTodasLasZonas(true));
            verify(repository, never()).findAllByActivoTrue();
        }

        @Test
        @DisplayName("filtra por sede según includeInactive")
        void porSede() {
            when(repository.findBySedeIdAndActivoTrue(1L)).thenReturn(zonas);
            when(repository.findBySedeId(1L)).thenReturn(List.of());
            when(mapper.toResponseList(zonas)).thenReturn(respuesta);
            when(mapper.toResponseList(List.of())).thenReturn(List.of());

            assertSame(respuesta, service.obtenerZonasPorSedeId(1L, false));
            assertThat(service.obtenerZonasPorSedeId(1L, true)).isEmpty();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ZonaUpdateService")
    class ReinicioSemanal {
        private static final LocalDateTime AHORA = LocalDateTime.of(2025, 10, 11, 0, 0);

        @Mock private ZonaRepository zonaRepository;
        @Mock private TimeService timeService;
        @InjectMocks private ZonaUpdateService service;

        private Zona zona(LocalDateTime primerReporte) {
            Zona zona = new Zona();
            zona.setFirstReportDate(primerReporte);
            zona.setReportCount(7);
            zona.setEstado(EstadoZona.ZONA_PRECAUCION);
            when(zonaRepository.findAll()).thenReturn(List.of(zona));
            return zona;
        }

        @Test
        @DisplayName("reinicia una zona con la ventana semanal vencida")
        void ventanaVencida() {
            Zona zona = zona(AHORA.minusWeeks(1).minusSeconds(1));
            when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);

            service.resetZoneStatus();

            assertEquals(0, zona.getReportCount());
            assertNull(zona.getFirstReportDate());
            assertEquals(EstadoZona.ZONA_SEGURA, zona.getEstado());
            verify(zonaRepository).save(zona);
        }

        @Test
        @DisplayName("no modifica una zona dentro de la ventana semanal")
        void ventanaVigente() {
            Zona zona = zona(AHORA.minusDays(3));
            when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);

            service.resetZoneStatus();

            assertEquals(7, zona.getReportCount());
            verify(zonaRepository, never()).save(any());
        }

        @Test
        @DisplayName("ignora las zonas sin reportes")
        void sinReportes() {
            zona(null);

            service.resetZoneStatus();

            verify(zonaRepository, never()).save(any());
        }
    }
}
