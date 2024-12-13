package com.banvenez.scbdvservicios.dto;

import lombok.Data;

@Data
public class ParametrosDTO {
	
	
	private Integer idGiomConfiguracion;
	private String tipoValor;
	private String descripcionValor; 
	private String valorConfigurado;
	private Integer oculto; 
	private Integer Estado; 
	private String estadoSistema;
	private Integer reprocesar;
}
