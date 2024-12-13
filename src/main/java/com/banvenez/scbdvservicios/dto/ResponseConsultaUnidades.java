package com.banvenez.scbdvservicios.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseConsultaUnidades {
    private String estatus;
    private String mensaje;
    private List<Unidad> data;
}