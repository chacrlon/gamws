package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.ConsultarListahoraDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultarlistahoraRowMapper implements RowMapper<ConsultarListahoraDTO> {

    @Override
    public ConsultarListahoraDTO mapRow(ResultSet rs, int i) throws SQLException {
        ConsultarListahoraDTO act = new ConsultarListahoraDTO();
        act.setHorainicio(rs.getString("HORA_INICIO"));
        act.setIdhora(rs.getString("ID_PK"));
        act.setEstado(rs.getString("ESTADO_HORA"));
//        act.setHorafin(rs.getString("HORA_FIN"));


        return act;
    }
}
