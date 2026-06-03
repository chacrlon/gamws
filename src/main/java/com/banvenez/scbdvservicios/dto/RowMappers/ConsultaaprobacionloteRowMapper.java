/*package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.GuardarLoteDTO;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultaaprobacionloteRowMapper implements RowMapper<GuardarLoteDTO> {

    @Override
    public GuardarLoteDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        GuardarLoteDTO act = new GuardarLoteDTO();
        act.setFechacreacion(rs.getString("FECHA_CREACION"));
        act.setFechaInicio(rs.getString("FECHA_INICIO"));
        act.setFechaFin(rs.getString("FECHA_FIN"));
        act.setIdlote(rs.getString("ID_LOTE_GIOM_PK"));
        act.setUnidad(rs.getString("UNIDAD"));
        act.setEstadolote(rs.getString("ESTADO_LOTE"));

        act.setNombrearchivo(rs.getString("NOMBRE_ARCHIVO"));
        act.setTotalRegistros(rs.getInt("TOTAL_REGISTROS"));
        act.setMontoTotal(rs.getBigDecimal("MONTO_TOTAL"));

        return act;
    }
}*/

package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.GuardarLoteDTO;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultaaprobacionloteRowMapper implements RowMapper<GuardarLoteDTO> {

    @Override
    public GuardarLoteDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        GuardarLoteDTO act = new GuardarLoteDTO();
        act.setIdlote(rs.getString("ID_LOTE_GIOM_PK"));
        act.setFechacreacion(rs.getString("FECHA_CREACION"));
        act.setFechaInicio(rs.getString("FECHA_INICIO"));
        act.setFechaFin(rs.getString("FECHA_FIN"));
        act.setUnidad(rs.getString("UNIDAD"));
        act.setEstadolote(rs.getString("ESTADO_LOTE"));
        act.setNombrearchivo(rs.getString("NOMBRE_ARCHIVO"));

        // Manejo seguro de posibles valores nulos
        try {
            act.setTotalRegistros(rs.getInt("TOTAL_REGISTROS"));
        } catch (SQLException e) {
            act.setTotalRegistros(0);
        }
        try {
            java.math.BigDecimal monto = rs.getBigDecimal("MONTO_TOTAL");
            act.setMontoTotal(monto != null ? monto : java.math.BigDecimal.ZERO);
        } catch (SQLException e) {
            act.setMontoTotal(java.math.BigDecimal.ZERO);
        }

        return act;
    }
}