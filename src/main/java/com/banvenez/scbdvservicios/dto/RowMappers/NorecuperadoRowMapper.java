package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.NorecuperadoDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NorecuperadoRowMapper implements RowMapper<NorecuperadoDTO> {

    @Override
    public NorecuperadoDTO mapRow(ResultSet rs, int i) throws SQLException {
        NorecuperadoDTO act = new NorecuperadoDTO();
        act.setMonto2(rs.getString("TOTAL_REGISTROS2"));



        return act;
    }
}
