package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.SemaforoLoteDTO;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SemaforoLoteRowMapper implements RowMapper<SemaforoLoteDTO> {

    @Override
    public SemaforoLoteDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        SemaforoLoteDTO dto = new SemaforoLoteDTO();
        dto.setIdLote(rs.getLong("ID_LOTE_GIOM_PK"));
        dto.setSemaforoEstado(rs.getString("SEMAFORO_ESTADO"));
        dto.setFechaEnvio(rs.getTimestamp("FECHA_ENVIO"));
        dto.setFechaRecepcion(rs.getTimestamp("FECHA_RECEPCION"));
        return dto;
    }
}