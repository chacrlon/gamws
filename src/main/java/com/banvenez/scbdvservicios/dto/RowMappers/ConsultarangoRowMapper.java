package com.banvenez.scbdvservicios.dto.RowMappers;


import com.banvenez.scbdvservicios.dto.RangofechasDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultarangoRowMapper implements RowMapper<RangofechasDTO> {

    @Override
    public RangofechasDTO mapRow(ResultSet rs, int i) throws SQLException {
        RangofechasDTO act = new RangofechasDTO();
        act.setFechaInicio(rs.getString("FECHA_INICIO"));
        act.setFechaFin(rs.getString("FECHA_FIN"));


        return act;
    }
}
