package com.utp_reporta_backend.modules.reporte.service.validation;

import com.utp_reporta_backend.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ReporteFileValidator {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    public void validate(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            return;
        }
        String contentType = foto.getContentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType) && !"image/png".equalsIgnoreCase(contentType)) {
            throw new BadRequestException("La foto debe ser JPG o PNG");
        }
        if (foto.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("La foto no puede superar los 10 MB");
        }
    }
}
