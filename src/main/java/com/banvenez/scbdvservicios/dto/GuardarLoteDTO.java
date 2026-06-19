package com.banvenez.scbdvservicios.dto;
import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class GuardarLoteDTO {


	private Integer totalRegistros;
	private BigDecimal montoTotal;
    private  String idlote;
    private  String lote;
    private  String estadolote;
    private  String fechaInicio;
    private  String fechaFin;
    private  String unidad;
    private  String fechacreacion;
    private  String nombrearchivo;
    private String usuario;
    private Integer cedula;
    private Timestamp fechaEnvio;
    private Timestamp fechaRecepcion;
    private String semaforoEstado; // "V" o "R"

}
