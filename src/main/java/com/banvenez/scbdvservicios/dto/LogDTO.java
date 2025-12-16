package com.banvenez.scbdvservicios.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogDTO {
    private Long idLog;
    private LocalDateTime fecha;
    private String nivel; // INFO, ERROR, WARN, DEBUG
    private String mensaje;
    private String metodo;
    private Long memoriaUsada; // MB
    private Long memoriaMaxima; // MB
}