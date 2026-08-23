package com.utp_reporta_backend.modules.zona.service;

import com.utp_reporta_backend.modules.zona.model.enums.EstadoZona;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;
import com.utp_reporta_backend.modules.time.service.TimeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ZonaUpdateService {
    private final ZonaRepository zonaRepository;
    private final TimeService timeService;

    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
    public void resetZoneStatus() {
        List<Zona> zonas = zonaRepository.findAll();
        for (Zona zona : zonas) {
            if (zona.getFirstReportDate() != null && timeService.getCurrentLocalDateTimePeru().isAfter(zona.getFirstReportDate().plusWeeks(1))) {
                zona.setReportCount(0);
                zona.setFirstReportDate(null);
                zona.setEstado(EstadoZona.ZONA_SEGURA);
                zonaRepository.save(zona);
            }
        }
    }
}


