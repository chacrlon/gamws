package com.banvenez.scbdvservicios.dto;

import lombok.Data;

@Data
public class SeguimientoDTO {
	
	private Integer idGiomSeguimiento;
	private Integer idLoteGiom; 
	private String descripcion; 
	private String codigoEmpleado;
	private String nombreEmpleado; 
	private String apellidoEmpleado; 
	private String cedulaEmpleado;
	private String codigoUnidadEmpleado; 
	private String descripcionEmpleado; 
	private String descripcionUnidadEmpleado; 
	private String ipEmpleado; 
	private String fechaCreacion; 

}
