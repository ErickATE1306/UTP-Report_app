package com.utp_reporta_backend.modules.time.service.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeQueryService {
    LocalDateTime getCurrentLocalDateTimePeru();
    LocalDate getCurrentLocalDatePeru();
}
