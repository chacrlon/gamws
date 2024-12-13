package com.banvenez.scbdvservicios.dto;
import lombok.Data;

@Data
public class ConsultarRangotransaccionesDTO {

    private String fechai;
    private String fechaf;
    private String cedula;
    private String monto;
    private String numerocuenta;
    private String numerolote;
    private String estadolote;
    private String movimiento;
}
