package com.banvenez.scbdvservicios.dto;
import lombok.Data;

@Data
public class GuardarLoteDTO {


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

}
