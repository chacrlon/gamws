package com.banvenez.scbdvservicios.dto.RowMappers;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ResumenCobranzaRowMapper implements RowMapper<Map<String, Object>> {
    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> resumen = new HashMap<>();

        // Formatear la fecha correctamente
        java.sql.Date fechaSql = rs.getDate("FECHA_COBRANZA");
        if (fechaSql != null) {
            // Convertir a string en formato dd/MM/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            resumen.put("fechaCobranza", sdf.format(fechaSql));
        } else {
            resumen.put("fechaCobranza", null);
        }

        resumen.put("totalLotes", rs.getInt("TOTAL_LOTES"));

        // Manejar BigDecimal para el monto
        java.math.BigDecimal monto = rs.getBigDecimal("MONTO_TOTAL_RECUPERADO");
        if (monto != null) {
            resumen.put("montoTotalRecuperado", monto.doubleValue());
        } else {
            resumen.put("montoTotalRecuperado", 0.0);
        }

        return resumen;
    }
}