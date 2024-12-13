package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.GuardarLoteDTO;
import com.banvenez.scbdvservicios.dto.RangofechasDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultardetallesLoteRowMapper implements RowMapper<GuardarLoteDTO> {

    @Override
    public GuardarLoteDTO mapRow(ResultSet rs, int i) throws SQLException {
        GuardarLoteDTO act = new GuardarLoteDTO();
        act.setFechaInicio(rs.getString("FECHA_INICIO"));
        act.setFechaFin(rs.getString("FECHA_FIN"));
        act.setUnidad(rs.getString("UNIDAD"));
        act.setNombrearchivo(rs.getString("NOMBRE_ARCHIVO"));
        act.setEstadolote(rs.getString("ESTADO_LOTE"));

        return act;
    }
}
