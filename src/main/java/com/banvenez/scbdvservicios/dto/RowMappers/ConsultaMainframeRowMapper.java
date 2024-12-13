package com.banvenez.scbdvservicios.dto.RowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.banvenez.scbdvservicios.dto.LoteMainframe;

public class ConsultaMainframeRowMapper implements RowMapper<LoteMainframe> {


    @Override
    public LoteMainframe mapRow(ResultSet rs, int i) throws SQLException {
    	LoteMainframe act = new LoteMainframe();
        
        act.setIdlote(rs.getString("ID_LOTE_GIOM_PK"));
        act.setFechacreacion(rs.getString("FECHA_CREACION_LOTE"));
        act.setEstadolote(rs.getString("ESTADO_LOTE"));
        act.setUnidad(rs.getString("UNIDAD"));
        act.setFechaInicio(rs.getString("FECHA_INICIO"));
        act.setFechaFin(rs.getString("FECHA_FIN"));
        act.setNombrearchivo(rs.getString("NOMBRE_ARCHIVO"));
        
        

        return act;
    }

}
