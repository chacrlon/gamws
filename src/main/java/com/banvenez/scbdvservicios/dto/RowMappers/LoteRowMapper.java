package com.banvenez.scbdvservicios.dto.RowMappers;  

import com.banvenez.scbdvservicios.dto.LoteDTO;  
import org.springframework.jdbc.core.RowMapper;  
import java.sql.ResultSet;  
import java.sql.SQLException;  

public class LoteRowMapper implements RowMapper<LoteDTO> {  

    @Override  
    public LoteDTO mapRow(ResultSet rs, int rowNum) throws SQLException {  
        LoteDTO lote = new LoteDTO();  
        lote.setNumeroDeCuenta(rs.getString("NUMERO_DE_CUENTA")); // Para NUMERO_DE_CUENTA  
        lote.setVef(rs.getString("VEF")); // Para VEF  
        lote.setMontoTransaccion(rs.getDouble("MONTO_TRANSACCION")); // Para MONTO_TRANSACCION  
        lote.setTipoMovimiento(rs.getString("TIPO_MOVIMIENTO")); // Para TIPO_MOVIMIENTO  
        lote.setSerialOperacion(rs.getString("SERIAL_OPERACION")); // Para SERIAL_OPERACION  
        lote.setReferencia(rs.getString("REFERENCIA")); // Para REFERENCIA  
        lote.setCodigoOperacion(rs.getString("CODIGO_OPERACION")); // Para CODIGO_OPERACION  
        lote.setReferencia2(rs.getString("REFERENCIA2")); // Para REFERENCIA2  
        lote.setTipoDocumento(rs.getString("TIPO_DOCUMENTO")); // Para TIPO_DOCUMENTO  
        lote.setCedula(rs.getString("CEDULA")); // Para CEDULA  
        
     // Mapear los nuevos campos  
        lote.setIdLote(rs.getString("ID_LOTE_GIOM_FK")); // Para ID_LOTE_GIOM_FK  
        lote.setIdRegistro(rs.getString("ID_REGISTRO_GIOM_PK")); // Para ID_REGISTRO_GIOM_PK  
        lote.setFecha(rs.getString("FECHA_CARGA")); // Para FECHA_CARGA (solo la fecha)  
        
     // Mapear los nuevos campos  desc_respuesta_mainframe
        lote.setCod_err(rs.getString("COD_RESPUESTA_MAINFRAME"));
        lote.setTip_err(rs.getString("SERIAL_RESPUESTA_MAINFRAME"));
        lote.setDes_err(rs.getString("DESC_RESPUESTA_MAINFRAME"));
        return lote;  
    }  
}