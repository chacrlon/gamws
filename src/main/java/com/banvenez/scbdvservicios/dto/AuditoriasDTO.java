package com.banvenez.scbdvservicios.dto;
import lombok.Data;

@Data
public class AuditoriasDTO {

    private String descripcion;
    private Integer idregistroauditoria;
    private String accion;
    private String usuario;
    private  String id;
    private  String fecharegistro;

}
