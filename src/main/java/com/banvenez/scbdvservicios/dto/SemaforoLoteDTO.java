package com.banvenez.scbdvservicios.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class SemaforoLoteDTO {
    private Long idLote;
    private String semaforoEstado; // "V" o "R"
    private Timestamp fechaEnvio;
    private Timestamp fechaRecepcion;
}