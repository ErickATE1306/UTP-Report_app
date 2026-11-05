package com.utp_reporta_backend.modules.reporte.service.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.utp_reporta_backend.common.exception.BadRequestException;

class ReporteFileValidatorTest {

    private static final long DIEZ_MB = 10L * 1024 * 1024;

    private final ReporteFileValidator validator = new ReporteFileValidator();

    private MultipartFile foto(String tipo) {
        return new MockMultipartFile("foto", "f", tipo, new byte[] {1});
    }

    private MultipartFile fotoDeTamano(long bytes) {
        MultipartFile foto = mock(MultipartFile.class);
        when(foto.isEmpty()).thenReturn(false);
        when(foto.getContentType()).thenReturn("image/png");
        when(foto.getSize()).thenReturn(bytes);
        return foto;
    }

    @Test
    @DisplayName("acepta un reporte sin foto")
    void sinFoto() {
        assertDoesNotThrow(() -> validator.validate(null));
    }

    @Test
    @DisplayName("ignora un archivo vacío")
    void archivoVacio() {
        assertDoesNotThrow(() -> validator.validate(new MockMultipartFile("foto", new byte[0])));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png", "IMAGE/PNG"})
    @DisplayName("acepta JPG y PNG")
    void tiposPermitidos(String tipo) {
        assertDoesNotThrow(() -> validator.validate(foto(tipo)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/gif", "application/pdf", "text/plain"})
    @DisplayName("rechaza otros tipos de archivo")
    void tiposRechazados(String tipo) {
        BadRequestException error = assertThrows(BadRequestException.class, () -> validator.validate(foto(tipo)));
        assertEquals("La foto debe ser JPG o PNG", error.getMessage());
    }

    @Test
    @DisplayName("rechaza un archivo sin tipo")
    void sinTipo() {
        assertThrows(BadRequestException.class, () -> validator.validate(foto(null)));
    }

    @Test
    @DisplayName("acepta exactamente 10 MB")
    void limiteExacto() {
        assertDoesNotThrow(() -> validator.validate(fotoDeTamano(DIEZ_MB)));
    }

    @Test
    @DisplayName("rechaza más de 10 MB")
    void superaLimite() {
        BadRequestException error = assertThrows(BadRequestException.class,
                () -> validator.validate(fotoDeTamano(DIEZ_MB + 1)));
        assertEquals("La foto no puede superar los 10 MB", error.getMessage());
    }
}
