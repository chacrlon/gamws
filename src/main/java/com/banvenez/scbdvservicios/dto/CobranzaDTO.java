package com.banvenez.scbdvservicios.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class CobranzaDTO {
    private Long idCobranza;
    private Long idLoteGiom;
    private Timestamp fechaHoraCobranza;
    private BigDecimal montoTotalRecuperado;
    private String estadoCobranza;
    private String nombreArchivo; // Para la consulta con JOIN
    private Timestamp fechaCreacionLote;
    private String unidad;
}