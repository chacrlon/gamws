package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.MontorecuperadoDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultamontoRecuperadoRoWMapper implements RowMapper<MontorecuperadoDTO> {

    @Override
    public MontorecuperadoDTO mapRow(ResultSet rs, int i) throws SQLException {
        MontorecuperadoDTO act = new MontorecuperadoDTO();
        act.setMontorecuperado(rs.getString("MONTO_RECUPERADO"));
        act.setTotalregistros(rs.getString("TOTAL_REGISTROS"));


        return act;
    }
}
