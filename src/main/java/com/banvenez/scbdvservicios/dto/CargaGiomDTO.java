package com.banvenez.scbdvservicios.dto;
import lombok.Data;

@Data
public class CargaGiomDTO {

    private  String numeroCuenta;
    private  String vef;
    private  String montoTransaccion;
    private  String tipoMovimiento;
    private  String serialOperacion;
    private  String referencia;
    private  String codigoOperacion;
    private  String referencia2;
    private  String tipoDocumento;
    private  String numeroCedula;
    private  String id_lotefk;
    private  String fechacarga;
    private  String montorecuperado;
    
    
    private String id_lote;
    private String codRespuestaMainframe; 
    private String descripcionRespuestaMainframe; 
    private String serialRespuestaMainframe; 
    private String estado;       // Este campo corresponde a ESTADO_REGISTRO (código)
    private String estadoNombre; // 👈 Nuevo campo para ESTADO_NOMBRE (texto)
    
}


