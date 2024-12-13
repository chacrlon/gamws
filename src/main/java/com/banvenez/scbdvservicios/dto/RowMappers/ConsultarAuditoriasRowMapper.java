package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.AuditoriasDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultarAuditoriasRowMapper  implements RowMapper<AuditoriasDTO> {

    @Override
    public AuditoriasDTO mapRow(ResultSet rs, int i) throws SQLException {
        AuditoriasDTO act = new AuditoriasDTO();
        act.setIdregistroauditoria(rs.getInt("ID_DEL_REGISTRO"));
        act.setDescripcion(rs.getString("DESCRIPCION"));
        act.setId(rs.getString("ID_AUDITORIA"));
        act.setAccion(rs.getString("TIPO_ACCION"));
        act.setUsuario(rs.getString("USUARIO_ACCION"));
        act.setFecharegistro(rs.getString("FECHA_REGISTRO"));


        return act;
    }
}
