package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.GuardarLoteDTO;
import com.banvenez.scbdvservicios.dto.UnidadesDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UnidadesRowMapper implements RowMapper<UnidadesDTO> {

    @Override
    public UnidadesDTO mapRow(ResultSet rs, int i) throws SQLException {
        UnidadesDTO act = new UnidadesDTO();
        act.setCondicion(rs.getString("CONDICION"));
        act.setPrefijo(rs.getString("PREFIJO"));
        act.setUnidad(rs.getString("UNIDAD"));
        act.setCodigounidad(rs.getString("codigo_unidad"));
        act.setCodigopadre(rs.getString("codigo_padre"));
        act.setComentario(rs.getString("COMENTARIO"));
        act.setUsu(rs.getString("USU_APR"));
        act.setFechaapr(rs.getString("FECHA_APR"));
        act.setSesion(rs.getString("SESION"));
        act.setFechases(rs.getString("FECHA_SES"));
        act.setObservacion(rs.getString("OBSERVACION"));
        act.setMacroproceso(rs.getString("MACROPROCESO"));
        act.setCodigopresidencia(rs.getString("codigo_rpresidencia"));
        act.setNombrepadre(rs.getString("NOMBRE_PADRE"));
        act.setNombrerpres(rs.getString("NOMBRE_RPRES"));



        return act;
    }
}
