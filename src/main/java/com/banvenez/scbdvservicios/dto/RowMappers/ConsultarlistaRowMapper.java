package com.banvenez.scbdvservicios.dto.RowMappers;


import com.banvenez.scbdvservicios.dto.GuardarLoteDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultarlistaRowMapper  implements RowMapper<GuardarLoteDTO> {

    @Override
    public GuardarLoteDTO mapRow(ResultSet rs, int i) throws SQLException {
        GuardarLoteDTO act = new GuardarLoteDTO();
        act.setEstadolote(rs.getString("ID_LOTE_GIOM_PK"));
        act.setIdlote(rs.getString("DESCRIPCION_LOTE"));


        return act;
    }
}
