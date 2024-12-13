package com.banvenez.scbdvservicios.dto.RowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.banvenez.scbdvservicios.dto.EnglobadorDTO;

public class EnglobadorRowMapper implements RowMapper<EnglobadorDTO>{

	@Override
	public EnglobadorDTO mapRow(ResultSet rs, int arg1) throws SQLException {
		EnglobadorDTO resp = new EnglobadorDTO(); 
		resp.setIdEnglobadorPk(rs.getInt("ID_GIOM_ENGLOBADOR_PK"));
		resp.setDescripcionProceso(rs.getString("DESCRIPCION_PROCESO"));
		resp.setFechaCreacion(rs.getString("FECHA_CREACION"));
		resp.setEstado(rs.getInt("ESTADO"));
		
		return resp;
	}

}
