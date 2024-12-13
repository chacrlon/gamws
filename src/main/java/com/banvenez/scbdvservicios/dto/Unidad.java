package com.banvenez.scbdvservicios.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Unidad {
    private String condicion;
    private String prefijo;
    private String codUnidad;
    private String unidad;
    private String codUnidadOrg;
    private String unidadOrg;
    private String codUnidadJrq;
    private String unidadJrq;
}