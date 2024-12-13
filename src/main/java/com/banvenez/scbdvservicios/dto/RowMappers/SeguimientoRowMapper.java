package com.banvenez.scbdvservicios.dto.RowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.banvenez.scbdvservicios.dto.SeguimientoDTO;

public class SeguimientoRowMapper implements RowMapper<SeguimientoDTO> {

	@Override
	public SeguimientoDTO mapRow(ResultSet rs, int arg1) throws SQLException {
		SeguimientoDTO data = new SeguimientoDTO(); 
		data.setIdGiomSeguimiento(rs.getInt("ID_GIOM_SEGUIMIENTO_PK"));
		data.setIdLoteGiom(rs.getInt("ID_LOTE_GIOM_FK"));
		data.setDescripcion(rs.getString("DECRIPCION_SEGUIMIENTO"));
		data.setCodigoEmpleado(rs.getString("CODIGO_EMPLEADO"));
		data.setNombreEmpleado(rs.getString("NOMRE_EMPLEADO"));
		data.setApellidoEmpleado(rs.getString("APELLIDO_EMPLEADO"));
		data.setCedulaEmpleado(rs.getString("CEDULA_EMPLEADO"));
		data.setCodigoUnidadEmpleado(rs.getString("CODIGO_UNIDAD"));
		data.setDescripcionUnidadEmpleado(rs.getString("DESCRIPCION_UNIDAD"));
		data.setIpEmpleado(rs.getString("IP_EMPLEADO"));
		data.setFechaCreacion(rs.getString("FECHA_CREACION"));

		return data;
	}

}
