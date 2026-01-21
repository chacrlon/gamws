package com.banvenez.scbdvservicios.dto.RowMappers;

import org.springframework.jdbc.core.RowMapper;
import com.banvenez.scbdvservicios.dto.CobranzaDTO;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CobranzaRowMapper implements RowMapper<CobranzaDTO> {
    @Override
    public CobranzaDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        CobranzaDTO cobranza = new CobranzaDTO();
        cobranza.setIdCobranza(rs.getLong("ID_COBRANZA_PK"));
        cobranza.setIdLoteGiom(rs.getLong("ID_LOTE_GIOM_FK"));
        cobranza.setFechaHoraCobranza(rs.getTimestamp("FECHA_HORA_COBRANZA"));
        cobranza.setMontoTotalRecuperado(rs.getBigDecimal("MONTO_TOTAL_RECUPERADO"));
        cobranza.setEstadoCobranza(rs.getString("ESTADO_COBRANZA"));
        cobranza.setNombreArchivo(rs.getString("NOMBRE_ARCHIVO"));
        cobranza.setFechaCreacionLote(rs.getTimestamp("FECHA_CREACION_LOTE"));
        cobranza.setUnidad(rs.getString("UNIDAD"));
        return cobranza;
    }
}