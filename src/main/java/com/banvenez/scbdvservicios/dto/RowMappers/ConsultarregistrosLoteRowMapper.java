package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.CargaGiomDTO;
import com.banvenez.scbdvservicios.dto.ConsultarListahoraDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultarregistrosLoteRowMapper implements RowMapper<CargaGiomDTO>  {

    @Override
    public CargaGiomDTO mapRow(ResultSet rs, int i) throws SQLException {
        CargaGiomDTO act = new CargaGiomDTO();
        act.setId_lote(rs.getString("ID_REGISTRO_GIOM_PK"));
        act.setId_lotefk(rs.getString("ID_LOTE_GIOM_FK"));
        act.setNumeroCuenta(rs.getString("NUMERO_DE_CUENTA"));
        act.setMontoTransaccion(rs.getString("MONTO_TRANSACCION"));
        act.setTipoMovimiento(rs.getString("TIPO_MOVIMIENTO"));
        act.setSerialOperacion(rs.getString("SERIAL_OPERACION"));
        act.setReferencia(rs.getString("REFERENCIA"));
        act.setCodigoOperacion(rs.getString("CODIGO_OPERACION"));
        act.setReferencia2(rs.getString("REFERENCIA2"));
        act.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
        act.setEstado(rs.getString("ESTADO_REGISTRO"));
        act.setMontorecuperado(rs.getString("MONTO_RECUPERADO"));
        act.setVef(rs.getString("VEF"));
        act.setNumeroCedula(rs.getString("CEDULA"));
        
        act.setFechacarga(rs.getString("FECHA_CARGA_FORMATEADA")); // Usa el nuevo alias
        act.setEstado(rs.getString("ESTADO_REGISTRO")); // Ahora existe en el resultset
        act.setEstadoNombre(rs.getString("ESTADO_NOMBRE")); 
        return act;
    }
}
