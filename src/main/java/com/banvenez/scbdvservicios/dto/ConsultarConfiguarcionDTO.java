package com.banvenez.scbdvservicios.dto;

import lombok.Data;

@Data
public class ConsultarConfiguarcionDTO {
	private String descriptor; 
	private Integer idConfiguracion; 
	private Integer estado; 
	private String usuario; 
	
}
