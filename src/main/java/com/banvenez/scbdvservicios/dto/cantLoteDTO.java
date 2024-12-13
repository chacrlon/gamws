package com.banvenez.scbdvservicios.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class cantLoteDTO {
	
	@JsonProperty(value="DATA")
	private Integer DATA;

}
