package com.banvenez.scbdvservicios.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultarLogsRequestDTO {
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private String nivel;
    private Long memoriaMinima; // Opcional: para filtrar por uso de memoria
}