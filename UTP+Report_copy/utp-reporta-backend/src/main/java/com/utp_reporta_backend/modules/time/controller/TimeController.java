package com.utp_reporta_backend.modules.time.controller;

import com.utp_reporta_backend.modules.time.service.TimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time")
@RequiredArgsConstructor
public class TimeController {
    private final TimeService timeService;

    @GetMapping("/peru-date")
    public ResponseEntity<LocalDate> getPeruDate() {
        return ResponseEntity.ok(timeService.getCurrentLocalDatePeru());
    }

    @GetMapping("/peru-datetime")
    public ResponseEntity<LocalDateTime> getPeruDateTime() {
        return ResponseEntity.ok(timeService.getCurrentLocalDateTimePeru());
    }
}


