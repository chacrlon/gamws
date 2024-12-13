package com.banvenez.scbdvservicios.dto.RowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.banvenez.scbdvservicios.dto.CantidadTransaccionMainframeDTO;

public class TransaccionesMainframeRowMapper implements RowMapper<CantidadTransaccionMainframeDTO>{

	@Override
	public CantidadTransaccionMainframeDTO mapRow(ResultSet rs, int arg1) throws SQLException {
		CantidadTransaccionMainframeDTO data = new CantidadTransaccionMainframeDTO(); 
		
		data.setData(rs.getInt("DATA"));
		
		return data;
	}

}
