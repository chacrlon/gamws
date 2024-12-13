package com.banvenez.scbdvservicios.dto.RowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.banvenez.scbdvservicios.dto.ParametrosDTO;

public class ParametrosRowMapper implements RowMapper<ParametrosDTO>{

	@Override
	public ParametrosDTO mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		ParametrosDTO out= new ParametrosDTO(); 
		
		
		out.setIdGiomConfiguracion(arg0.getInt("ID_GIOM_CONFIGURACION_PK"));
		out.setTipoValor(arg0.getString("TIPO_VALOR_CONFIGURACION"));
		out.setDescripcionValor(arg0.getString("DESC_CAMPO_CONFIGURACION"));
		out.setValorConfigurado(arg0.getString("VALOR_CONFIGURADO"));
		out.setOculto(arg0.getInt("OCULTO"));
		out.setEstado(arg0.getInt("ESTADO_CONFIGURACION"));
		out.setEstadoSistema(arg0.getString("ESTADO_SISTEMA")!=null? arg0.getString("ESTADO_SISTEMA"):""); 

		return out;
	}

}
