package com.utp_reporta_backend.modules.time.service.impl;

import com.utp_reporta_backend.modules.time.service.TimeService;
import com.utp_reporta_backend.modules.time.service.query.TimeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TimeServiceImpl implements TimeService {
    private final TimeQueryService queryService;
    public LocalDateTime getCurrentLocalDateTimePeru(){return queryService.getCurrentLocalDateTimePeru();}
    public LocalDate getCurrentLocalDatePeru(){return queryService.getCurrentLocalDatePeru();}
}
