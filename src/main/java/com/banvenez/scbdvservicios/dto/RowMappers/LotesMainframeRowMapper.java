package com.banvenez.scbdvservicios.dto.RowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.banvenez.scbdvservicios.dto.ParametrosDTO;
import com.banvenez.scbdvservicios.dto.cantLoteDTO;

public class LotesMainframeRowMapper implements RowMapper<cantLoteDTO>{

	@Override
	public cantLoteDTO mapRow(ResultSet rs, int arg1) throws SQLException {
		cantLoteDTO data = new cantLoteDTO(); 
		
		data.setDATA(rs.getInt("DATA"));
		
		return data;
	}

}
