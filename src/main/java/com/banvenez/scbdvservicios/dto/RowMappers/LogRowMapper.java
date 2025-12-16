package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.LogDTO;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogRowMapper implements RowMapper<LogDTO> {

    @Override
    public LogDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        LogDTO log = new LogDTO();
        log.setIdLog(rs.getLong("ID_LOG"));

        java.sql.Timestamp timestamp = rs.getTimestamp("FECHA");
        if (timestamp != null) {
            log.setFecha(timestamp.toLocalDateTime());
        }

        log.setNivel(rs.getString("NIVEL"));
        log.setMensaje(rs.getString("MENSAJE"));
        log.setMetodo(rs.getString("METODO"));
        log.setMemoriaUsada(rs.getLong("MEMORIA_USADA"));
        log.setMemoriaMaxima(rs.getLong("MEMORIA_MAXIMA"));

        return log;
    }
}