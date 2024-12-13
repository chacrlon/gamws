package com.banvenez.scbdvservicios.dto;
import lombok.Data;

@Data
public class EstadosLoteDTO {

    //private String estado;
    private String numero;
    private  Number idlote;
    // esto es nuevo
    private String usuario;
    private Integer cedula; 
    private String ip;
}
