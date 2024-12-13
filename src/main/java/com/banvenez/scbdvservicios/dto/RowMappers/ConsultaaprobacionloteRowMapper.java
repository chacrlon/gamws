package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.GuardarLoteDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultaaprobacionloteRowMapper implements RowMapper<GuardarLoteDTO> {


    @Override
    public GuardarLoteDTO mapRow(ResultSet rs, int i) throws SQLException {
        GuardarLoteDTO act = new GuardarLoteDTO();
        act.setFechacreacion(rs.getString("FECHA_CREACION"));
        act.setFechaInicio(rs.getString("FECHA_INICIO"));
        act.setFechaFin(rs.getString("FECHA_FIN"));
        act.setIdlote(rs.getString("ID_LOTE_GIOM_PK"));
        act.setUnidad(rs.getString("UNIDAD"));
        act.setEstadolote(rs.getString("ESTADO_LOTE"));

        return act;
    }


}
