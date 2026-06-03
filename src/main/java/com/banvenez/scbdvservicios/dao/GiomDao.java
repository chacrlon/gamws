package com.banvenez.scbdvservicios.dao;

import com.banvenez.scbdvservicios.dto.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import com.banvenez.scbdvservicios.dto.RowMappers.*;
import com.banvenez.scbdvservicios.util.CifradoData;
import com.banvenez.scbdvservicios.util.FtpUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.internal.OracleTypes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.jdbc.support.oracle.SqlArrayValue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*;
import java.util.zip.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.apache.commons.net.ftp.FTPClient;
//Otras importaciones necesarias
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.commons.net.ftp.FTP;          // Añade esto
import org.apache.commons.net.ftp.FTPClient;    // Asegúrate de tener esto
import org.apache.commons.net.ftp.FTPFile;

@Slf4j
@Service
@Repository
@SuppressWarnings(value = { "unchecked", "rawtypes", "unused", "resource" })
public class GiomDao {
	@Value("${maxdata}")
	private  String SecretKeyData;
	private ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
	private JdbcTemplate jdbcTemplate;

	public GiomDao() {
		super();
		this.jdbcTemplate = (JdbcTemplate) context.getBean("jdbctemplateGiom");
	}
	private static ZipOutputStream zos;

	public ResponseModel validarNombreArchivo(String nombrearchivo) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withProcedureName("PRC_VALIDAR_NOMBRE_ARCHIVO")
					.declareParameters(
							new SqlParameter("P_NOMBRE_ARCHIVO", Types.VARCHAR),
							new SqlOutParameter("COD_RET", Types.VARCHAR),
							new SqlOutParameter("DE_CODRET", Types.VARCHAR)
					);

			MapSqlParameterSource params = new MapSqlParameterSource()
					.addValue("P_NOMBRE_ARCHIVO", nombrearchivo);

			Map<String, Object> result = jdbcCall.execute(params);

			response.setCode(Integer.parseInt((String) result.get("COD_RET")));
			response.setMessage((String) result.get("DE_CODRET"));

		} catch (Exception e) {
			response.setCode(9999);
			response.setMessage("Error en validación: " + e.getMessage());
		}
		return response;
	}
	
	public ResponseModel rechazarLote(String idLote, String usuario, String ip) {
	    ResponseModel response = new ResponseModel();
	    try {
	        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
	                .withProcedureName("PRC_RECHAZAR_LOTE")
	                .declareParameters(
	                    new SqlParameter("P_ID_LOTE", Types.NUMERIC),
	                    new SqlParameter("P_USUARIO", Types.VARCHAR),
	                    new SqlParameter("P_IP", Types.VARCHAR),
	                    new SqlOutParameter("COD_RET", Types.VARCHAR),
	                    new SqlOutParameter("DE_CODRET", Types.VARCHAR)
	                );
	        MapSqlParameterSource params = new MapSqlParameterSource()
	                .addValue("P_ID_LOTE", Integer.parseInt(idLote))
	                .addValue("P_USUARIO", usuario)
	                .addValue("P_IP", ip);
	        Map<String, Object> result = jdbcCall.execute(params);
	        response.setCode(Integer.parseInt((String) result.get("COD_RET")));
	        response.setMessage((String) result.get("DE_CODRET"));
	        response.setStatus(200);
	    } catch (Exception e) {
	        response.setCode(9999);
	        response.setMessage("Error al rechazar lote: " + e.getMessage());
	        response.setStatus(500);
	    }
	    return response;
	}

	public ResponseModel cargarArchivo(List<CargaGiomDTO> listaResultado) {
		log.info("BEGIN " + this.getClass().getSimpleName() + ".cargar registros del archivo({})",
				listaResultado.size());
		ResponseModel response = new ResponseModel();
		Object[] listar = new Object[listaResultado.size()];
		int arrayIndex2 = 0;
		for (CargaGiomDTO data : listaResultado) {
			Object[] datosr = new Object[11];
			datosr[0] = data.getId_lote();
			datosr[1] = data.getNumeroCuenta();
			datosr[2] = data.getVef();
			datosr[3] = data.getMontoTransaccion();
			datosr[4] = data.getTipoMovimiento();
			datosr[5] = data.getSerialOperacion();
			datosr[6] = data.getReferencia();
			datosr[7] = data.getCodigoOperacion();
			datosr[8] = data.getReferencia2();
			datosr[9] = data.getTipoDocumento();
			datosr[10] = data.getNumeroCedula();
			listar[arrayIndex2++] = datosr;
		}
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_GUARDAR_ARCHIVO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.TYPE_TBL_REGISTRO"),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(listar));
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			response.setCode(Integer.parseInt(codigo));
			response.setMessage(descripCodigo);
			response.setStatus(200);
			return response;
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setStatus(500);
			response.setMessage(e.getMessage());
			return response;
		}
	}

	public ResponseModel guardarnombreArchivo(String filename, String id_lote) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_NOMBRE_ARCHIVO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("NOMBRE_ARCHIVO", Types.VARCHAR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.INTEGER)
			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("NOMBRE_ARCHIVO", filename);
			inputMap.addValue("ID_LOTE_GIOM_PK", Integer.parseInt(id_lote));
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				return response;
			} else {
				response.setCode(1001);
				response.setMessage("Error al guardar nombre del archivo en la tabla GION_LOTE ");
				response.setStatus(204);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR insert PRC_INSERTAR_NOMBRE_ARCHIVO Exception");
			response.setStatus(500);
			return response;
		}
	}

	public ResponseModel guardarLote(GuardarLoteDTO datos) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_DATOS_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("DATO", OracleTypes.VARCHAR), new SqlParameter("UNIDAD", Types.VARCHAR),
					new SqlParameter("FECHA_INICIO", Types.VARCHAR), new SqlParameter("FECHA_FIN", Types.VARCHAR)

			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("UNIDAD", datos.getUnidad());
			inputMap.addValue("FECHA_INICIO", datos.getFechaInicio());
			inputMap.addValue("FECHA_FIN", datos.getFechaFin());

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			String dato = (String) resultMap.get("DATO");
			log.info("Resultado => {},{},{}", cod_retorno, desc_retorno, dato);
			if (cod_retorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				response.setId(Integer.parseInt(dato));

				AuditoriasDTO datosAuditoria = new AuditoriasDTO();
				datosAuditoria.setAccion("REGISTRO");
				datosAuditoria.setDescripcion("SE REGISTRA EL LOTE ID " + dato);
				datosAuditoria.setUsuario(datos.getUsuario());
				datosAuditoria.setIdregistroauditoria(Integer.parseInt(dato));
				boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
				return response;
			} else {
				response.setCode(1001);
				response.setMessage(
						"Error al guardar los datos en la tabla GION_LOTE => datos (" + datos.toString() + ")");
				response.setStatus(204);
				AuditoriasDTO datosAuditoria = new AuditoriasDTO();
				datosAuditoria.setAccion("REGISTRO");
				datosAuditoria.setDescripcion("NO FUE POSIBLE REGISTRAR EL LOTE ");
				datosAuditoria.setUsuario(datos.getUsuario());
				datosAuditoria.setIdregistroauditoria(Integer.parseInt(dato));
				boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR insert PRC_INSERTAR_DATOS_LOTE Exception");
			response.setStatus(500);
			AuditoriasDTO datosAuditoria = new AuditoriasDTO();
			datosAuditoria.setAccion("REGISTRO");
			datosAuditoria.setDescripcion("NO FUE POSIBLE REGISTRAR EL LOTE ");
			datosAuditoria.setUsuario(datos.getUsuario());
			boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
			return response;
		}

	}

	public ResponseModel consultar_aprobacion() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")                      // ← ESQUEMA AGREGADO
					.withProcedureName("PRC_CONSULTA_LISTA_APROBACION")
					.withoutProcedureColumnMetaDataAccess()
					.returningResultSet("P_Result", new ConsultaaprobacionloteRowMapper());

			jdbcCall.declareParameters(
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", OracleTypes.CURSOR)
			);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);

			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");

			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response);

			if (cod_retorno != null && cod_retorno.equals("1000")) {
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
			} else {
				response2.setStatus(500);
				response2.setCode(1001);
				response2.setMessage("Error en la consulta: " + (desc_retorno != null ? desc_retorno : "Código de retorno no reconocido"));
			}
		} catch (Exception e) {
			log.error("Error en consultar_aprobacion: ", e);
			response2.setStatus(500);
			response2.setCode(9999);
			// Mensaje de error más descriptivo (incluye la causa real)
			response2.setMessage("Error al consultar PRC_CONSULTA_LISTA_APROBACION: " + e.getMessage()
					+ (e.getCause() != null ? " - Causa: " + e.getCause().getMessage() : ""));
		}
		return response2;
	}

/*
	public ResponseModel consultar_aprobacion() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_APROBACION");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);

			jdbcCall.returningResultSet("P_Result", new ConsultaaprobacionloteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_APROBACION");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_APROBACION Exception");
			response2.setStatus(500);
			return response2;
		}
	}*/

	public ResponseModel consultartransacciones() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_TRANSACCIONES");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarregistrosLoteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_TRANSACCIONES");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_TRANSACCIONES Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel consultarrangoauditoria(FechasAuditoriasDTO datos) {
		ResponseModel response2 = new ResponseModel();
		ArrayList<AuditoriasDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_RANGO_AUDITORIA");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("fecha_i", Types.VARCHAR), new SqlParameter("fecha_f", Types.VARCHAR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarAuditoriasRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("fecha_i", datos.getFechai());
			inputMap.addValue("fecha_f", datos.getFechaf());
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<AuditoriasDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_RANGO_AUDITORIA");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_RANGO_AUDITORIA Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel consultarrangolote(RangoFechaDTO datos) {
		ResponseModel response2 = new ResponseModel();
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_RANGO_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("fecha_i", Types.VARCHAR), new SqlParameter("fecha_f", Types.VARCHAR),
					new SqlParameter("numerolote", Types.VARCHAR), new SqlParameter("estadolote", Types.VARCHAR),
					new SqlParameter("P_cod_unidad", Types.VARCHAR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarlistaloteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("fecha_i", datos.getFechai());
			inputMap.addValue("fecha_f", datos.getFechaf());
			inputMap.addValue("numerolote", datos.getNumerolote());
			inputMap.addValue("estadolote", datos.getEstadolote());
			inputMap.addValue("P_cod_unidad", datos.getCodigoUnidad());
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_RANGO_LOTE");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_RANGO_LOTE Exception");
			response2.setStatus(500);
			return response2;
		}
	}


	//METODO PARA FILTRAR CONSULTAS DE Búsqueda de Transacciones de Lotes
	public ResponseModel consultarrangotransacciones(ConsultarRangotransaccionesDTO datos) {
		ResponseModel response2 = new ResponseModel();
		ArrayList<CargaGiomDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_RANGO_LOTE_TRANSACCIONES");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("fecha_i", Types.VARCHAR), new SqlParameter("fecha_f", Types.VARCHAR),
					new SqlParameter("cedula", Types.VARCHAR), new SqlParameter("monto", Types.VARCHAR),
					new SqlParameter("numerocuenta", Types.VARCHAR), new SqlParameter("numerolote", Types.VARCHAR),
					new SqlParameter("estado", Types.VARCHAR), new SqlParameter("p_movimiento", Types.VARCHAR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarregistrosLoteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("fecha_i", datos.getFechai());
			inputMap.addValue("fecha_f", datos.getFechaf());
			inputMap.addValue("cedula", datos.getCedula());
			inputMap.addValue("monto", datos.getMonto());
			inputMap.addValue("numerocuenta", datos.getNumerocuenta());
			inputMap.addValue("numerolote", datos.getNumerolote());
			inputMap.addValue("estado", datos.getEstadolote());
			inputMap.addValue("p_movimiento", datos.getMovimiento());
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<CargaGiomDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				ConsultaRangoFechasDTO salida = new ConsultaRangoFechasDTO();
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				Double total = (double) 0;
				for (CargaGiomDTO cargaGiomDTO : response) {
					if (cargaGiomDTO.getEstado().equals("P")) {
						total = total + Double.parseDouble(cargaGiomDTO.getMontoTransaccion());
					}
				}
				salida.setData(response);
				salida.setMontoTotal(total);
				response2.setData(salida);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_RANGO_LOTE_TRANSACCIONES");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_RANGO_LOTE_TRANSACCIONES Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel consultarlistafiltrado() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_LOTE_FILTRADO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("fecha_i", Types.VARCHAR), new SqlParameter("fecha_f", Types.VARCHAR),
					new SqlParameter("cedula", Types.VARCHAR), new SqlParameter("descrDepto", Types.VARCHAR),
					new SqlParameter("descrCargo", Types.VARCHAR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarlistaloteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_LOTE_FILTRADO");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_LOTE_FILTRADO Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel consultarlista() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarlistaloteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_LOTE");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_LOTE Exception");
			response2.setStatus(500);
			return response2;
		}
	}
	// LOTES
	public ResponseModel detalleslote(GuardarLoteDTO datos) {
		ResponseModel response2 = new ResponseModel();
		GuardarLoteDTO response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_DETALLES_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.INTEGER)

			);
			jdbcCall.returningResultSet("P_Result", new ConsultardetallesLoteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_PK", Integer.parseInt(datos.getIdlote()));
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = ((ArrayList<GuardarLoteDTO>) resultMap.get("P_Result")).get(0);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_DETALLES_LOTE");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_DETALLES_LOTE Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel consulta_lote_archivo(String fecha) {
		ResponseModel response2 = new ResponseModel();
		CargaGiomDTO response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_ARCHIVO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("P_fecha", Types.VARCHAR)
			);
			jdbcCall.returningResultSet("P_Result", new CrearconsultaarchivoRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_fecha", fecha);
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (CargaGiomDTO) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_ARCHIVO");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_ARCHIVO Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel lote_monto(GuardarLoteDTO datos) {
		ResponseModel response2 = new ResponseModel();
		MontorecuperadoDTO response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_MONTO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.INTEGER)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultamontoRecuperadoRoWMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_PK", Integer.parseInt(datos.getIdlote()));
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = ((ArrayList<MontorecuperadoDTO>) resultMap.get("P_Result")).get(0);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_MONTO");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_MONTO Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel lote_monto2(GuardarLoteDTO datos) {
		ResponseModel response2 = new ResponseModel();
		NorecuperadoDTO response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_MONTO_NULL");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.INTEGER)
			);
			jdbcCall.returningResultSet("P_Result", new NorecuperadoRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_PK", Integer.parseInt(datos.getIdlote()));
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = ((ArrayList<NorecuperadoDTO>) resultMap.get("P_Result")).get(0);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_MONTO");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_MONTO Exception");
			response2.setStatus(500);
			return response2;
		}
	}


	public ResponseModel aprobacion(GuardarLoteDTO datos, String ip) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_UDATE_APROBACION");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("ID_LOTE_GIOM_FK", Types.NUMERIC));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_FK", Integer.parseInt(datos.getIdlote()));
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				AuditoriasDTO datosAuditoria = new AuditoriasDTO();
				datosAuditoria.setAccion("APROBACION");
				datosAuditoria.setDescripcion("SE APRUEBA EL LOTE " + datos.getIdlote());
				datosAuditoria.setUsuario(datos.getUsuario());
				datosAuditoria.setIdregistroauditoria(Integer.parseInt(datos.getIdlote()));
				boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
				PeapoleDataEntradaDTO data = new PeapoleDataEntradaDTO();
				data.setCedula(datos.getCedula());
				PeapoleDataDTO peapoleData = this.consultaPeapole(data);
				String decripcionSeguimiento = "Lote aprobado";
				String codigoEmpleado = "";
				String nomreEmpleado = peapoleData.getDatosTrabADE().getNombres();
				String apellidoEmpleado = peapoleData.getDatosTrabADE().getApellidos();
				String cedulaEmpleado = peapoleData.getDatosTrabADE().getCedula();
				String codigoUnidad = peapoleData.getDatosTrabADE().getCodDepto();
				String descripcionUnidad = peapoleData.getDatosTrabADE().getDescDepto();
				boolean seguimientoOk = this.guardarSeguimiento(decripcionSeguimiento, codigoEmpleado, nomreEmpleado,
						apellidoEmpleado, cedulaEmpleado, codigoUnidad, descripcionUnidad, ip, datos.getIdlote());
				return response;
			} else {
				AuditoriasDTO datosAuditoria = new AuditoriasDTO();
				datosAuditoria.setAccion("APROBACION");
				datosAuditoria.setDescripcion("NO FUE POSIBLE APROBAR EL LOTE " + datos.getIdlote());
				datosAuditoria.setUsuario(datos.getUsuario());
				datosAuditoria.setIdregistroauditoria(Integer.parseInt(datos.getIdlote()));
				boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
				response.setCode(1001);
				response.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				response.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR PRC_UDATE_APROBACION Exception");
			response.setStatus(500);
			AuditoriasDTO datosAuditoria = new AuditoriasDTO();
			datosAuditoria.setAccion("APROBACION");
			datosAuditoria.setDescripcion("NO FUE POSIBLE APROBAR EL LOTE " + datos.getIdlote());
			datosAuditoria.setUsuario(datos.getUsuario());
			datosAuditoria.setIdregistroauditoria(Integer.parseInt(datos.getIdlote()));
			boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
			return response;
		}
	}

	private boolean guardarSeguimiento(String decripcionSeguimiento, String codigoEmpleado, String nomreEmpleado,
									   String apellidoEmpleado, String cedulaEmpleado, String codigoUnidad, String descripcionUnidad,
									   String ipEmpleado, String idlote) {
		try {

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_SEGUIMIENTO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("p_id_lote_fk", OracleTypes.VARCHAR),
					new SqlParameter("p_decripcion_seguimiento", OracleTypes.VARCHAR),
					new SqlParameter("p_codigo_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_nomre_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_apellido_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_cedula_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_codigo_unidad", OracleTypes.VARCHAR),
					new SqlParameter("p_descripcion_unidad", OracleTypes.VARCHAR),
					new SqlParameter("p_ip_empleado", OracleTypes.VARCHAR),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("p_id_lote_fk", idlote);
			inputMap.addValue("p_decripcion_seguimiento", decripcionSeguimiento);
			inputMap.addValue("p_codigo_empleado", codigoEmpleado);
			inputMap.addValue("p_nomre_empleado", nomreEmpleado);
			inputMap.addValue("p_apellido_empleado", apellidoEmpleado);
			inputMap.addValue("p_cedula_empleado", cedulaEmpleado);
			inputMap.addValue("p_codigo_unidad", codigoUnidad);
			inputMap.addValue("p_descripcion_unidad", descripcionUnidad);
			inputMap.addValue("p_ip_empleado", ipEmpleado);
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	private PeapoleDataDTO consultaPeapole(PeapoleDataEntradaDTO data) {
		PeapoleDataDTO resp = new PeapoleDataDTO();
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("URLP");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		HttpPost post = new HttpPost(parametrosPivot.get(0).getValorConfigurado());
		try {
			StringEntity dataToSend = new StringEntity(new Gson().toJson(data));
			post.setHeader("Content-Type", "application/json");
			post.setEntity(dataToSend);
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			Gson gson = new Gson();
			log.info("Json de salida " + responseString);
			resp = gson.fromJson(responseString, PeapoleDataDTO.class);
		} catch (Exception e) {
			log.error("no fue posíble parsear el objeto de salida para la consulta al otro backend", e);
		}
		return resp;
	}

	public ResponseModel modificardatosestado(EstadosLoteDTO datos) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_UDATE_ESTADOS_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.NUMERIC), new SqlParameter("NUMERO", Types.VARCHAR)
			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_PK", datos.getIdlote());
			inputMap.addValue("NUMERO", datos.getNumero());
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				// registramos la auditoria
				try {
					AuditoriasDTO datosAuditoria = new AuditoriasDTO();
					datosAuditoria.setAccion("UPDATE DE LOTE");
					datosAuditoria.setDescripcion("SE MODIFICA EL ESTATUS DEL LOTE " + datos.getIdlote()
							+ " A EL ESTATUS " + datos.getNumero());
					datosAuditoria.setUsuario(datos.getUsuario());
					datosAuditoria.setIdregistroauditoria(datos.getIdlote().intValue());
					boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
					if (respuestaAuditoria) {
						log.info("fue actualizado correctamente la auditoria del lote");
					} else {
						log.error("no fue posible actualizar la auditoria del lote");
					}
					PeapoleDataEntradaDTO data = new PeapoleDataEntradaDTO();
					data.setCedula(datos.getCedula());
					PeapoleDataDTO peapoleData = this.consultaPeapole(data);
					String decripcionSeguimiento = "Modificacion de estado de lote, a estatus: " + datos.getNumero();
					String codigoEmpleado = "";
					String nomreEmpleado = peapoleData.getDatosTrabADE().getNombres();
					String apellidoEmpleado = peapoleData.getDatosTrabADE().getApellidos();
					String cedulaEmpleado = peapoleData.getDatosTrabADE().getCedula();
					String codigoUnidad = peapoleData.getDatosTrabADE().getCodDepto();
					String descripcionUnidad = peapoleData.getDatosTrabADE().getDescDepto();

					boolean seguimientoOk = this.guardarSeguimiento(decripcionSeguimiento, codigoEmpleado,
							nomreEmpleado, apellidoEmpleado, cedulaEmpleado, codigoUnidad, descripcionUnidad,
							datos.getIp(), datos.getIdlote().toString());

					if (seguimientoOk) {
						log.info("fue actualizado correctamente el seguimiento del lote");
					} else {
						log.error("no fue posible actualizar el seguimiento del lote");
					}
				} catch (Exception e) {
					log.error("no fue posible registrar la auditoria/seguimiento ", e);
				}
				return response;
			} else {
				response.setCode(1001);
				response.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				response.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR PRC_UDATE_ESTADOS_LOTE Exception");
			response.setStatus(500);
			return response;
		}
	}

	public ResponseModel modificardatoslote(GuardarLoteDTO datos) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_UDATE_DATOS_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.NUMERIC), new SqlParameter("FECHA_INICIO", Types.VARCHAR),
					new SqlParameter("FECHA_FIN", Types.VARCHAR), new SqlParameter("UNIDAD", Types.VARCHAR)
			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_PK", Integer.parseInt(datos.getIdlote()));
			inputMap.addValue("FECHA_INICIO", datos.getFechaInicio());
			inputMap.addValue("FECHA_FIN", datos.getFechaFin());
			inputMap.addValue("UNIDAD", datos.getUnidad());
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				return response;
			} else {
				response.setCode(1001);
				response.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				response.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR PRC_UDATE_DATOS_LOTE Exception");
			response.setStatus(500);
			return response;
		}
	}

	public ResponseModel consultarlistaHora() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<ConsultarListahoraDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_HORA");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarlistahoraRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<ConsultarListahoraDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_HORA");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_HORA Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel consultarlistaregistros() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<CargaGiomDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LISTA_REGISTROS");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarlistahoraRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<CargaGiomDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response2;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_REGISTROS");
				response2.setStatus(500);
				return response2;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_REGISTROS Exception");
			response2.setStatus(500);
			return response2;
		}
	}

	public ResponseModel eliminacion(GuardarLoteDTO datos) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_ELIMINAR_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR), new SqlParameter("P_id", Types.NUMERIC)
			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_id", Integer.parseInt(datos.getIdlote()));
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {

				PeapoleDataEntradaDTO data = new PeapoleDataEntradaDTO();
				data.setCedula(datos.getCedula());
				PeapoleDataDTO peapoleData = this.consultaPeapole(data);
				AuditoriasDTO registrador = new AuditoriasDTO();
				registrador.setAccion("SISTEMA");
				registrador.setDescripcion("SE ELIMINO UN LOTE");
				registrador.setUsuario(peapoleData.getDatosTrabADE().getNombres() + " "
						+ peapoleData.getDatosTrabADE().getApellidos());
				registrador.setIdregistroauditoria(Integer.parseInt(datos.getIdlote()));

				if (this.guardarauditoriaDinamico(registrador)) {
					log.info("Se escribio correctamente la auditoria para el lote " + datos.getIdlote());
				}
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				return response;
			} else {
				response.setCode(1001);
				response.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				response.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR PRC_UDATE_APROBACION Exception");
			response.setStatus(500);
			return response;
		}
	}

	private boolean guardarSeguimientoLista(String decripcionSeguimiento, String codigoEmpleado, String nomreEmpleado,
											String apellidoEmpleado,
											String cedulaEmpleado, String codigoUnidad, String descripcionUnidad, String ipEmpleado,
											List<String> idLotes) {
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_SEGUIMIENTO_LISTA_LOTE");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.ARRAY_VARCHAR_TYPE"),
					new SqlParameter("p_decripcion_seguimiento", OracleTypes.VARCHAR),
					new SqlParameter("p_codigo_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_nomre_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_apellido_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_cedula_empleado", OracleTypes.VARCHAR),
					new SqlParameter("p_codigo_unidad", OracleTypes.VARCHAR),
					new SqlParameter("p_descripcion_unidad", OracleTypes.VARCHAR),
					new SqlParameter("p_ip_empleado", OracleTypes.VARCHAR),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			String[] dataSend = new String[idLotes.size()];
			for (String data : idLotes) {
				dataSend[idLotes.indexOf(data)] = data;
			}
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(dataSend));
			inputMap.addValue("p_decripcion_seguimiento", decripcionSeguimiento);
			inputMap.addValue("p_codigo_empleado", codigoEmpleado);
			inputMap.addValue("p_nomre_empleado", nomreEmpleado);
			inputMap.addValue("p_apellido_empleado", apellidoEmpleado);
			inputMap.addValue("p_cedula_empleado", cedulaEmpleado);
			inputMap.addValue("p_codigo_unidad", codigoUnidad);
			inputMap.addValue("p_descripcion_unidad", descripcionUnidad);
			inputMap.addValue("p_ip_empleado", ipEmpleado);
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	public boolean guardarauditoriaDinamico(AuditoriasDTO datosAuditoria) {
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_AUDITORUA_DINAMICO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("P_id_registro", OracleTypes.VARCHAR),
					new SqlParameter("P_accion", OracleTypes.VARCHAR),
					new SqlParameter("P_usuario", OracleTypes.VARCHAR),
					new SqlParameter("p_descripcion", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_id_registro", datosAuditoria.getIdregistroauditoria());
			inputMap.addValue("P_accion", datosAuditoria.getAccion());
			inputMap.addValue("P_usuario", datosAuditoria.getUsuario());
			inputMap.addValue("p_descripcion", datosAuditoria.getDescripcion());
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	private boolean registrarEngloobador(List<String> idLotes) {
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_ENGLOBADOR");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("p_descripcion_englobador", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			String lotesEnviados = "los lotes enviados son: ";
			for (String string : idLotes) {
				lotesEnviados = lotesEnviados + " " + string;
			}
			inputMap.addValue("p_descripcion_englobador", lotesEnviados);
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	// MAINFRAME
	private boolean verificarDataMainframe() {
		log.info("inicio de ejecucion consulta de lotes existentes en mainframe");
		boolean resp = false;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_VERIFICAR_DATA_MAINFRAME");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("CANT_LOTE", OracleTypes.CURSOR),
					new SqlOutParameter("CANT_TRANSACCIONES", OracleTypes.CURSOR),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			jdbcCall.returningResultSet("CANT_LOTE", new LotesMainframeRowMapper());
			jdbcCall.returningResultSet("CANT_TRANSACCIONES", new TransaccionesMainframeRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				List<cantLoteDTO> lotes = (ArrayList<cantLoteDTO>) resultMap.get("CANT_LOTE");
				List<CantidadTransaccionMainframeDTO> transacciones = (ArrayList<CantidadTransaccionMainframeDTO>) resultMap
						.get("CANT_TRANSACCIONES");
				Integer transaccionesMainframe = 0;
				Integer lotesMainframe = 0;
				log.info("data consultada lotes: " + lotes + " transacciones: " + transacciones);
				for (cantLoteDTO cantLoteDTO : lotes) {
					lotesMainframe = cantLoteDTO.getDATA();
				}
				for (CantidadTransaccionMainframeDTO transaccionesDTO : transacciones) {
					transaccionesMainframe = transaccionesDTO.getData();
				}
				if (transaccionesMainframe > 0 || lotesMainframe > 0) {
					return false;
				}
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	private boolean cambiarEstatusTransaccionMasivo(String estatusTransaccion, List<String> idLotes) {
		log.info("inicio de ejecucion update masivo de lotes, data de entrada:  " + idLotes);
		boolean resp = false;

		try {

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_ACTUALIZAR_ESTADO_TRANSACCIONES");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.ARRAY_VARCHAR_TYPE"),
					new SqlParameter("p_estado_transaccion", OracleTypes.VARCHAR),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			String[] dataSend = new String[idLotes.size()];
			for (String data : idLotes) {
				dataSend[idLotes.indexOf(data)] = data;
			}
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(dataSend));
			inputMap.addValue("p_estado_transaccion", estatusTransaccion);
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los datos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	private boolean cambiarEstatusLoteMasivo(String estatusLote, List<String> idLotes) {
		log.info("inicio de ejecucion update masivo de lotes, DATA DE ENTRADA : " + idLotes);
		boolean resp = false;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_ACTUALIZAR_ESTADO_LOTES");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.ARRAY_VARCHAR_TYPE"),
					new SqlParameter("p_estado_lotes", OracleTypes.VARCHAR),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			String[] dataSend = new String[idLotes.size()];
			for (String data : idLotes) {
				dataSend[idLotes.indexOf(data)] = data;
			}
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(dataSend));
			inputMap.addValue("p_estado_lotes", estatusLote);
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	private ArrayList<CargaGiomDTO> dataMainframeTransacciones(List<String> idLotes) {
		ArrayList<CargaGiomDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_TRANSACCIONES_MAINFRAME_ARRAY");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.ARRAY_VARCHAR_TYPE"),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", OracleTypes.CURSOR));

			jdbcCall.returningResultSet("P_Result", new ConsultarregistrosLoteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			String[] dataSend = new String[idLotes.size()];
			for (String data : idLotes) {
				dataSend[idLotes.indexOf(data)] = data;
			}
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(dataSend));
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			response = (ArrayList<CargaGiomDTO>) resultMap.get("P_Result");
			return response;
		} catch (Exception e) {
			log.error("Error al guardar los datos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			return response;
		}
	}

	private ArrayList<CargaGiomDTO> consultaTransaccionesMainframe(String idlote) {
		ResponseModel response2 = new ResponseModel();
		ArrayList<CargaGiomDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_TRANSACCION_MAINFRAME");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR),
					new SqlParameter("p_id_lote", OracleTypes.INTEGER)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultarregistrosLoteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("p_id_lote", idlote);
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<CargaGiomDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_TRANSACCIONES");
				response2.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_TRANSACCIONES Exception");
			response2.setStatus(500);
			return response;
		}
	}

	private ArrayList<LoteMainframe> consultarListaLotesMainframe() {
		ResponseModel response2 = new ResponseModel();
		ArrayList<LoteMainframe> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTAR_LOTE_MAINFRAME");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new ConsultaMainframeRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<LoteMainframe>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				response2.setStatus(200);
				response2.setCode(Integer.parseInt(cod_retorno));
				response2.setMessage(desc_retorno);
				response2.setData(response);
				return response;
			} else {
				response2.setCode(1001);
				response2.setMessage("Error en la consulta PRC_CONSULTA_LISTA_APROBACION");
				response2.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response2.setCode(9999);
			response2.setMessage("ERROR al  consultar PRC_CONSULTA_LISTA_APROBACION Exception");
			response2.setStatus(500);
			return response;
		}
	}

	public ResponseModel consultarConfiguracion(ConsultarConfiguarcionDTO datos) {
		ResponseModel salida = new ResponseModel();
		List<ParametrosDTO> response = new ArrayList<ParametrosDTO>();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTAR_CONFIGURACION");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("p_parametro_consulta", Types.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new ParametrosRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("p_parametro_consulta", datos.getDescriptor());
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<ParametrosDTO>) resultMap.get("P_Result");
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				salida.setData(response);
				salida.setCode(1000);
				salida.setStatus(200);
				salida.setCode(Integer.parseInt(cod_retorno));
				salida.setMessage(desc_retorno);
				return salida;
			} else {
				salida.setCode(1001);
				salida.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				salida.setStatus(500);
				return salida;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			salida.setCode(9999);
			salida.setMessage("ERROR PRC_CONSULTAR_CONFIGURACION Exception");
			salida.setStatus(500);
			return salida;
		}
	}

	public ResponseModel cambioEstadoConfiguracion(ConsultarConfiguarcionDTO datos) {
		ResponseModel salida = new ResponseModel();
		List<ParametrosDTO> response = new ArrayList<ParametrosDTO>();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CAMBIO_ESTADO_CONFIGURACION");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("p_estado_configuracion", Types.INTEGER), new SqlParameter("P_id", Types.INTEGER)
			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("p_estado_configuracion", datos.getEstado());
			inputMap.addValue("P_id", datos.getIdConfiguracion());
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				salida.setCode(1000);
				salida.setStatus(200);
				salida.setCode(Integer.parseInt(cod_retorno));
				salida.setMessage(desc_retorno);
				AuditoriasDTO datosAuditoria = new AuditoriasDTO();
				datosAuditoria.setAccion("MODIFICACION");
				datosAuditoria.setDescripcion("SE MODIFICO LA HORA DE EJECUCION PARA PROCESAR DATOS DE MAINFRAME ");
				datosAuditoria.setUsuario(datos.getUsuario());
				boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
				return salida;
			} else {
				AuditoriasDTO datosAuditoria = new AuditoriasDTO();
				datosAuditoria.setAccion("MODIFICACION");
				datosAuditoria.setDescripcion(
						"NO FUE POSIBLE MODIFICAR LA HORA DE EJECUCION PARA PROCESAR DATOS DE MAINFRAME ");
				datosAuditoria.setUsuario(datos.getUsuario());
				boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
				salida.setCode(1001);
				salida.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				salida.setStatus(500);
				return salida;
			}
		} catch (Exception e) {

			log.error(e.getMessage(), e);
			salida.setCode(9999);
			salida.setMessage("ERROR PRC_CONSULTAR_CONFIGURACION Exception");
			salida.setStatus(500);

			AuditoriasDTO datosAuditoria = new AuditoriasDTO();
			datosAuditoria.setAccion("MODIFICACION");
			datosAuditoria
					.setDescripcion("NO FUE POSIBLE MODIFICAR LA HORA DE EJECUCION PARA PROCESAR DATOS DE MAINFRAME ");
			datosAuditoria.setUsuario(datos.getUsuario());
			boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
			return salida;
		}
	}

	public ResponseModel consultarSeguimiento(EstadosLoteDTO datos) {
		ResponseModel salida = new ResponseModel();
		List<SeguimientoDTO> response = new ArrayList<SeguimientoDTO>();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_SEGUIMIENTO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", OracleTypes.CURSOR), new SqlParameter("p_id_lote", Types.INTEGER)
			);
			jdbcCall.returningResultSet("P_Result", new SeguimientoRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("p_id_lote", datos.getIdlote());
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				response = (ArrayList<SeguimientoDTO>) resultMap.get("P_Result");
				salida.setData(response);
				salida.setCode(1000);
				salida.setStatus(200);
				salida.setCode(Integer.parseInt(cod_retorno));
				salida.setMessage(desc_retorno);
				return salida;
			} else {
				salida.setCode(1001);
				salida.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				salida.setStatus(500);
				return salida;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			salida.setCode(9999);
			salida.setMessage("ERROR PRC_CONSULTAR_CONFIGURACION Exception");
			salida.setStatus(500);
			return salida;
		}
	}

	public ResponseModel ejecutarRecepcion() {
		log.info("iniciando el proceso de recuperacion de data de mainframe");
		boolean ejecutado = this.verificarDataMainframe();
		if (!ejecutado) {
			log.info(
					"Existe data en mainframe, consultando si el archivo de ejecucion de mainframe y la diferencia de tiempo entre ejecuciones. ");
			EnglobadorDTO dataEnglobador = this.consultarEnglobador().get(0); // siempre retornaremos un solo registro
			log.info("se consulto la data de: " + dataEnglobador.toString());
			log.info(
					"procedemos a consultar la diferencia de tiempo entre la ultima ejecucion de mainframe y el sistema. ");
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
			datos.setDescriptor("HM");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			Integer diferenciaHorariaMaxima = Integer.parseInt(parametrosPivot.get(0).getValorConfigurado());
			log.info("la ruta del archivo a leer es: " + diferenciaHorariaMaxima);
			DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			try {
				datos = new ConsultarConfiguarcionDTO();
				datos.setDescriptor("RUTF");
				parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
				String rutaServidor = parametrosPivot.get(0).getValorConfigurado();
				log.info("la ruta del archivo a leer es: " + rutaServidor);
				Path file = Paths.get(rutaServidor);
				BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
				log.info("la ultima fecha de modificacion es: " + attr.lastModifiedTime());
				LocalDateTime horaUltimaModificacionArchivo = LocalDateTime
						.parse(attr.lastModifiedTime().toString().substring(0, 19).replace("T", " "), df);
				log.info(" hora de archivo .flag del servidor " + horaUltimaModificacionArchivo);
				log.info(" hora de sistema operativo.  " + LocalDateTime.now());
				Duration duration = Duration.between(horaUltimaModificacionArchivo, LocalDateTime.now());
				log.info("Diferencia de tiempo entre procesos en horas: " + (duration.getSeconds() / 60) / 60);
				if (((duration.getSeconds() / 60) / 60) < diferenciaHorariaMaxima) {
					log.info("La diferencia entre horas es menor a la hora maxima tolerable, ejecutando proceso. ");
					datos = new ConsultarConfiguarcionDTO();
					datos.setDescriptor("RUT");
					parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
					rutaServidor = parametrosPivot.get(0).getValorConfigurado();
					log.info("la ruta del archivo a leer es: " + rutaServidor);
					File archivo = new File(rutaServidor);
					FileReader fr = new FileReader(archivo);
					BufferedReader br = new BufferedReader(fr);
					ConsultarConfiguarcionDTO HoraData = new ConsultarConfiguarcionDTO();
					HoraData.setDescriptor("ESP");
					ResponseModel dataHoraSalida = this.consultarConfiguracion(HoraData);
					HashMap<String, ParametrosDTO> dataEstadosChequear = this
							.transformarMap((ArrayList<ParametrosDTO>) dataHoraSalida.getData());
					HashMap<String, Integer> lotesEvaluados = new HashMap<String, Integer>();
					List<CargaGiomDTO> listaDataBd = new ArrayList<CargaGiomDTO>();
					Integer isContinuar = 0;
					br.lines().forEach(line -> {
						log.info("leyendo el registro de: " + line);
						if (!line.contains("ENDOFFILE")) {
							CargaGiomDTO dataPivot = new CargaGiomDTO();
							String idTransaccion = line.substring(10, 25);
							String idLite = line.substring(25, 40);
							String respuestaMainframe = line.substring(170, 172);
							String serial = line.substring(172, 179);
							String Descripcion = line.substring(179, line.length());
							Integer lotesinCeros = Integer.parseInt(idLite);
							if (lotesEvaluados.get(lotesinCeros.toString()) == null) {
								lotesEvaluados.put(lotesinCeros.toString(), Integer.parseInt(idLite));
							}
							if (dataEstadosChequear.get(respuestaMainframe) == null) {
								ParametrosDTO parametro = new ParametrosDTO();
								parametro.setTipoValor("ESP");
								parametro.setDescripcionValor("Estado respuesta Mainframe, estadoDesconocido");
								parametro.setValorConfigurado(respuestaMainframe);
								parametro.setOculto(0);
								parametro.setEstado(1);
								parametro.setEstadoSistema("Q");
								parametro.setReprocesar(1);
								this.guardarConfiguracion(parametro);
								dataEstadosChequear.put(respuestaMainframe, parametro);
							}
							Integer idTransaccionPivot = Integer.parseInt(idTransaccion);
							dataPivot.setId_lote(idTransaccionPivot.toString());
							dataPivot.setCodRespuestaMainframe(respuestaMainframe);
							dataPivot.setSerialRespuestaMainframe(serial);
							dataPivot.setDescripcionRespuestaMainframe(Descripcion);
							dataPivot.setEstado(dataEstadosChequear.get(respuestaMainframe).getEstadoSistema());
							listaDataBd.add(dataPivot);
						} else {
							log.info("se alcanzo el final del archivo. ");
						}
					});
					boolean guardar = this.actualizarEstadoMainframeRetorno(listaDataBd);
					log.info(
							"Se procede a procesar los lotes existentes en el archivo de configuracion vs lotes en estatus reprocesar y enviados");
					boolean guardarLotes = this.actualizarLotesMainframeRetorno(lotesEvaluados);
				} else {
					log.error(
							"La diferencia entre horas es mayor a la hora maxima tolerable, ejecutando proceso de notificacion de falla en mainframe. ");
				}
			} catch (Exception e) {
				log.error("no fue posible leer el archivo especificado", e);
			}
		} else {
			log.info("No existe data enviada por este ambiente a mainframe");
		}
		log.info("finalizando el proceso de recuperacion de data de mainframe");
		return null;
	}

	private boolean actualizarLotesMainframeRetorno(HashMap<String, Integer> lotesEvaluados) {
		log.info("Inicio del proceso de estudio para determinar que lotes se enviarán a mainframe, data de entrada: "
				+ lotesEvaluados);
		ArrayList<GuardarLoteDTO> lotesEnMainframe = this.consultarLotesMainframeRetorno();
		for (GuardarLoteDTO guardarLoteDTO : lotesEnMainframe) {
			if (lotesEvaluados.get(guardarLoteDTO.getIdlote()) == null) {
				log.info("agregando un lote resagado a la evaluacion de lotes, data a agregar: " + guardarLoteDTO);
				lotesEvaluados.put(guardarLoteDTO.getIdlote(), Integer.parseInt(guardarLoteDTO.getIdlote()));
			}
		}
		log.info(
				"Se enviaran al backend los lotes para determinar si suspenderlos o colocarlos en estatus de reprocesar");
		log.info("Lotes a enviar: " + lotesEvaluados);
		List<String> lotesArray = new ArrayList();
		for (String setKeys : lotesEvaluados.keySet()) {
			lotesArray.add(setKeys);
		}
		boolean dataTramitada = this.actualizarLotesMainframeRetorno(lotesArray);
		if (dataTramitada) {
			log.info("Fin del proceso de estudio para determinar que lotes se enviarán a mainframe");
			log.info("Proceso de actualizar lotes en mainframe exitoso");
			return true;
		} else {
			log.info("Fin del proceso de estudio para determinar que lotes se enviarán a mainframe");
			log.error("Proceso de actualizar lotes en mainframe fallido");
			return false;
		}
	}

	private boolean actualizarLotesMainframeRetorno(List<String> lotesArray) {
		log.info("inicio del proceso de ejecucion  del procedure: PRC_PROCESADO_LOTES_TRANSACCIONES_MAINFRAME_RETORNO");
		boolean response = false;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_PROCESADO_LOTES_TRANSACCIONES_MAINFRAME_RETORNO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.ARRAY_VARCHAR_TYPE"),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			String[] dataSend = new String[lotesArray.size()];
			for (String data : lotesArray) {
				dataSend[lotesArray.indexOf(data)] = data;
			}
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(dataSend));
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				log.info(
						"fin exitoso del proceso de ejecucion  del procedure: PRC_PROCESADO_LOTES_TRANSACCIONES_MAINFRAME_RETORNO");
				return true;
			}
			log.info(
					"fin fallido del proceso de ejecucion  del procedure: PRC_PROCESADO_LOTES_TRANSACCIONES_MAINFRAME_RETORNO");
			return false;
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.info(
					"fin exitoso del proceso de ejecucion  del procedure: PRC_PROCESADO_LOTES_TRANSACCIONES_MAINFRAME_RETORNO");
			return false;
		}
	}

	private ArrayList<GuardarLoteDTO> consultarLotesMainframeRetorno() {
		log.info("Iniciando consulta de lotes resagados");
		ArrayList<GuardarLoteDTO> response = null;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTA_LOTE_MAINFRAME_RETORNO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", oracle.jdbc.OracleTypes.CURSOR));
			jdbcCall.returningResultSet("P_Result", new ConsultarlistaloteRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			jdbcCall.compile();
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			response = (ArrayList<GuardarLoteDTO>) resultMap.get("P_Result");
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("La consulta => {},{}", cod_retorno, desc_retorno);
			log.info("RESPUESTA CONSULTA => P_Result = {}", response.toString());
			if (cod_retorno.equals("1000")) {
				log.info("La consulta tiene data => {},{}", cod_retorno, desc_retorno);
				log.info("Consulta exitosa de lotes resagados, finalizando consulta ");
				return response;
			} else {
				log.error("Se presentó un error al consultar los lotes resagados, finalizando consulta ");
				return response;
			}
		} catch (Exception e) {
			log.error("Se presentó un error al consultar los lotes resagados, finalizando consulta ");
			log.error(e.getMessage(), e);
			return response;
		}
	}

	private boolean actualizarEstadoMainframeRetorno(List<CargaGiomDTO> listaDataBd) {
		log.info("Inicio del proceso de actualizacion de registros retornados de mainframe data de entrada: "
				+ listaDataBd.toString());
		Object[] listar = new Object[listaDataBd.size()];
		int arrayIndex2 = 0;
		for (CargaGiomDTO data : listaDataBd) {
			Object[] datosr = new Object[5];
			datosr[0] = data.getEstado();
			datosr[1] = data.getSerialRespuestaMainframe();
			datosr[2] = data.getDescripcionRespuestaMainframe();
			datosr[3] = data.getCodRespuestaMainframe();
			datosr[4] = data.getId_lote();
			listar[arrayIndex2++] = datosr;
		}
		try {
			System.out.println("estamos enviando a la base de datos: " + listar);
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_RECEPCION_TRC");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlParameter("VAR_ARRAY", OracleTypes.ARRAY, "GIOM.TYPE_REG_TRC_ARRAY"),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("VAR_ARRAY", new SqlArrayValue(listar));
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				log.info("fin del proceso de actualizacion de registros retornados de mainframe ");
				return true;
			} else {
				log.info("fin del proceso de actualizacion de registros retornados de mainframe ");
				return false;
			}
		} catch (Exception e) {
			log.info("fin del proceso de actualizacion de registros retornados de mainframe ");
			log.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean guardarConfiguracion(ParametrosDTO parametro) {
		boolean resp = false;
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_INSERTAR_CONFIGURACION");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("p_tipo_valor_config", OracleTypes.VARCHAR),
					new SqlParameter("p_desc_valor_config", OracleTypes.VARCHAR),
					new SqlParameter("p_valor_config", OracleTypes.VARCHAR),
					new SqlParameter("p_oculto", OracleTypes.INTEGER),
					new SqlParameter("p_reprocesar", OracleTypes.INTEGER),
					new SqlParameter("p_estado_sistema", OracleTypes.VARCHAR));
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("p_tipo_valor_config", parametro.getTipoValor());
			inputMap.addValue("p_desc_valor_config", parametro.getDescripcionValor());
			inputMap.addValue("p_valor_config", parametro.getValorConfigurado());
			inputMap.addValue("p_oculto", parametro.getOculto());
			inputMap.addValue("p_reprocesar", parametro.getReprocesar());
			inputMap.addValue("p_estado_sistema", parametro.getEstadoSistema());
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				return true;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return false;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return false;
		}
	}

	private HashMap<String, ParametrosDTO> transformarMap(ArrayList<ParametrosDTO> data) {
		HashMap<String, ParametrosDTO> dataSalida = new HashMap<String, ParametrosDTO>();
		for (ParametrosDTO parametrosDTO : data) {
			dataSalida.put(parametrosDTO.getValorConfigurado(), parametrosDTO);
		}
		return dataSalida;
	}

	private List<EnglobadorDTO> consultarEnglobador() {
		List<EnglobadorDTO> resp = new ArrayList<EnglobadorDTO>();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_CONSULTAR_ENGLOBADOR");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlOutParameter("P_Result", OracleTypes.CURSOR)
			);
			jdbcCall.returningResultSet("P_Result", new EnglobadorRowMapper());
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			log.info("Antes de Call = ");
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codigo = (String) resultMap.get("COD_RET");
			String descripCodigo = (String) resultMap.get("DE_CODRET");
			log.info("Resultado =  ({} - {})", codigo, descripCodigo);
			if (codigo.equals("1000")) {
				resp = (ArrayList<EnglobadorDTO>) resultMap.get("P_Result");
				return resp;
			} else {
				log.error("no fue posible registrar la data en base de datos, chequear proceso");
				return resp;
			}
		} catch (Exception e) {
			log.error("Error al guardar los dotos del archivo PRC_GUARDAR_ARCHIVO");
			log.error(e.getMessage(), e);
			log.error("no fue posible registrar la data en base de datos, chequear proceso");
			return resp;
		}
	}


	public ResponseModel obtenerLotesActivos() {
		ResponseModel response = new ResponseModel();
		String codRetorno;
		String descRetorno;
		String resultado = ""; // Inicializar como cadena vacía

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			//HAY QUE ANALIZAR SI SE VA A ENVIAR EL ID DE CADA LOTE O EL ID DE CADA TRANSACCION, ACTUALMENTE ENVIA EL ID DE CADA LOTE
			jdbcCall.withProcedureName("PRC_OBTENER_LOTES_ACTIVOS");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(
					new SqlOutParameter("RESULTADO", OracleTypes.VARCHAR), // Cambiar a VARCHAR2
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR)
			);

			// Ejecutar el procedimiento sin parámetros de entrada
			Map<String, Object> resultMap = jdbcCall.execute();

			// Obtener el resultado como String
			resultado = (String) resultMap.get("RESULTADO");

			// Obtener los códigos de retorno
			codRetorno = (String) resultMap.get("COD_RET");
			descRetorno = (String) resultMap.get("DE_CODRET");

			log.info("Resultado del procedimiento: Código = {}, Descripción = {}, Resultado = {}", codRetorno, descRetorno, resultado);

			// Configurar la respuesta exitosa
			response.setCode(Integer.parseInt(codRetorno));
			response.setMessage(descRetorno);
			response.setStatus(200);
			response.setData(resultado); // Almacenar el resultado como String
		} catch (Exception e) {
			log.error("Error al ejecutar el procedimiento", e);
			response.setCode(9999);
			response.setMessage("Error al ejecutar el procedimiento: " + e.getMessage());
			response.setStatus(500);
		}

		return response;
	}




	public ResponseModel obtenerRespuestaDelProcedimiento(String idLotes) {
		ResponseModel response = new ResponseModel();

		try {
			// Crear una instancia de SimpleJdbcCall
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_OBTENER_DATA_MAINFRAME_ARRAY");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			// Declarar los parámetros de salida y entrada
			jdbcCall.declareParameters(
					new SqlOutParameter("RESULTADO", OracleTypes.CURSOR),
					new SqlParameter("P_ID_LOTE", OracleTypes.VARCHAR),
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR)
			);

			// Mapear el resultado del cursor a una lista de objetos
			jdbcCall.returningResultSet("RESULTADO", new LoteRowMapper());

			// Crear un mapa de parámetros de entrada
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_ID_LOTE", idLotes);

			log.info("Llamando al procedimiento con P_ID_LOTE: {}", idLotes);

			// Ejecutar el procedimiento pasando el mapa de parámetros
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			log.info("Resultado del procedimiento: {}", resultMap); // Log para depuración

			// Obtener la lista de resultados mapeados
			ArrayList<LoteDTO> resultados = (ArrayList<LoteDTO>) resultMap.get("RESULTADO");
			log.info("Resultado del procedimiento: {}", resultados.stream()
					.map(LoteDTO::toFormattedString) // Usamos el nuevo método
					.collect(Collectors.joining("\n"))); // Juntamos los resultados
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_CODRET");

			if (codRetorno.equals("1000")) {
				response.setStatus(200);
				response.setCode(Integer.parseInt(codRetorno));
				response.setMessage(descRetorno);
				response.setData(resultados);
			} else {
				response.setStatus(500);
				response.setCode(9999);
				response.setMessage("Error en la consulta");
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setStatus(500);
			response.setCode(9999);
			response.setMessage("Error al consultar");
		}

		return response;
	}


	public ResponseModel actualizarEstadoRegistro(String idLotes, String estadoRegistro) {
		ResponseModel response = new ResponseModel();

		try {
			// Crear una instancia de SimpleJdbcCall
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_ACTUALIZAR_ESTADO_REGISTRO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);

			// Declarar los parámetros de entrada y salida
			jdbcCall.declareParameters(
					new SqlParameter("P_ID_LOTE", OracleTypes.VARCHAR),
					new SqlParameter("P_ESTADO_REGISTRO", OracleTypes.VARCHAR),  // Nuevo parámetro
					new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR)
			);

			// Crear un mapa de parámetros de entrada
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_ID_LOTE", idLotes);
			inputMap.addValue("P_ESTADO_REGISTRO", estadoRegistro);  // Agregar el valor del estado

			log.info("Llamando al procedimiento con P_ID_LOTE: {} y P_ESTADO_REGISTRO: {}", idLotes, estadoRegistro);

			// Ejecutar el procedimiento pasando el mapa de parámetros
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			log.info("Resultado del procedimiento: {}", resultMap); // Log para depuración

			// Obtener los valores de los parámetros de salida
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_CODRET");

			// Configurar la respuesta
			if (codRetorno.equals("1000")) {
				response.setStatus(200);
				response.setCode(Integer.parseInt(codRetorno));
				response.setMessage(descRetorno);
			} else {
				response.setStatus(500);
				response.setCode(9999);
				response.setMessage(descRetorno); // Puedes modificar el mensaje si deseas
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setStatus(500);
			response.setCode(9999);
			response.setMessage("Error al actualizar el estado del registro");
		}

		return response;
	}



	public ResponseModel actualizarEstadoLoteFTP(String idLotes, String estadoRegistro) {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withProcedureName("PRC_ACTUALIZAR_ESTADO_LOTE_FTP")
					.declareParameters(
							new SqlParameter("P_ID_LOTE", OracleTypes.VARCHAR),
							new SqlParameter("P_ESTADO_REGISTRO", OracleTypes.VARCHAR),
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR)
					);

			MapSqlParameterSource params = new MapSqlParameterSource()
					.addValue("P_ID_LOTE", idLotes)
					.addValue("P_ESTADO_REGISTRO", estadoRegistro);

			Map<String, Object> result = jdbcCall.execute(params);

			String codRet = (String) result.get("COD_RET");
			String mensaje = (String) result.get("DE_CODRET");

			if ("1000".equals(codRet)) {
				response.setStatus(200);
				response.setMessage(mensaje);
			} else {
				response.setStatus(500);
				response.setMessage("Error: " + mensaje);
			}

		} catch (Exception e) {
			log.error("Error al actualizar estado vía FTP", e);
			response.setStatus(500);
			response.setMessage("Error interno: " + e.getMessage());
		}

		return response;
	}

	//ESTE ES PARA ACTUALIZAR DIARIAMENTE TODOS LOS LOTES EN ESTADO A Y W
	public ResponseModel actualizarEstadosPorFecha() {
		ResponseModel response = new ResponseModel();
		log.info("Hora actual del servidor: {}", LocalDateTime.now());
		log.info("Zona horaria del servidor: {}", ZoneId.systemDefault());
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withProcedureName("PRC_ACTUALIZAR_ESTADOS_POR_FECHA")
					.declareParameters(
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR)
					);

			// No hay parámetros de entrada, se pasa un Map vacío
			MapSqlParameterSource inputMap = new MapSqlParameterSource();

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);

			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_CODRET");

			log.info("Resultado => CodRet: {}, DescRet: {}", codRetorno, descRetorno);
			log.info("Resultado de la actualización diaria: {}", descRetorno);
			if ("1000".equals(codRetorno)) {
				response.setCode(Integer.parseInt(codRetorno));
				response.setStatus(200);
				response.setMessage(descRetorno);
			} else {
				response.setCode(1001); // Código genérico para errores de negocio
				response.setStatus(500);
				response.setMessage("Error al actualizar estados: " + descRetorno);
			}
			return response;

		} catch (Exception e) {
			log.error("Error en PRC_ACTUALIZAR_ESTADOS_POR_FECHA: ", e);
			response.setCode(9999);
			response.setMessage("Error interno al ejecutar el procedimiento: " + e.getMessage());
			response.setStatus(500);
			return response;
		}
	}

	public ResponseModel actualizarRespuestaMainframe2(Long idLoteGiomPk) {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withProcedureName("PRC_ACTUALIZAR_RESPUESTA_MAINFRAME2")
					.declareParameters(
							new SqlParameter("P_ID_LOTE_GIOM_PK", OracleTypes.NUMERIC),
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_ID_LOTE_GIOM_PK", idLoteGiomPk);

			log.info("Llamando al procedimiento con P_ID_LOTE_GIOM_PK: {}", idLoteGiomPk);

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			log.info("Resultado del procedimiento: {}", resultMap);

			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");

			if ("1000".equals(codRetorno)) {
				response.setStatus(200);
				response.setCode(Integer.parseInt(codRetorno));
				response.setMessage(descRetorno);
			} else {
				response.setStatus(500);
				response.setCode(9999);
				response.setMessage(descRetorno);
			}

		} catch (Exception e) {
			log.error("Error al actualizar respuesta mainframe: ", e);
			response.setStatus(500);
			response.setCode(9999);
			response.setMessage("Error al procesar lote en mainframe");
		}

		return response;
	}


	// En GiomDao.java - Agrega estos métodos:

	public ResponseModel listarArchivosFTP() {
		FTPClient ftpClient = null;
		ResponseModel response = new ResponseModel();

		try {
			// Tu configuración FTP existente
			String host = "180.183.171.164";
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

			datos.setDescriptor("FU");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			String path = "/home/ftpq0326/giom/recive/";

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				if (!ftpClient.changeWorkingDirectory(path)) {
					response.setStatus(500);
					response.setMessage("No se pudo cambiar al directorio: " + path);
					return response;
				}

				FTPFile[] files = ftpClient.listFiles();
				List<Map<String, Object>> archivosInfo = new ArrayList<>();

				addLog("=== LISTADO COMPLETO DE ARCHIVOS EN FTP ===");
				for (FTPFile file : files) {
					Map<String, Object> info = new HashMap<>();
					info.put("nombre", file.getName());
					info.put("tamaño", file.getSize());
					info.put("tipo", file.getType());
					info.put("timestamp", file.getTimestamp() != null ? file.getTimestamp().getTime() : null);

					archivosInfo.add(info);

					addLog("Archivo: {} | Tamaño: {} bytes | Tipo: {}",
							file.getName(), file.getSize(), file.getType());
				}

				response.setStatus(200);
				response.setMessage("Se encontraron " + files.length + " archivos");
				response.setData(archivosInfo);

			} else {
				response.setStatus(500);
				response.setMessage("No se pudo conectar al FTP");
			}

		} catch (Exception e) {
			log.error("Error listando archivos FTP: {}", e.getMessage(), e);
			response.setStatus(500);
			response.setMessage("Error listando archivos: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					addLog("Error desconectando FTP: {}", e.getMessage());
				}
			}
		}

		return response;
	}



	public ResponseModel examinarArchivoRespaldo(String nombreArchivo) {
		FTPClient ftpClient = null;
		ResponseModel response = new ResponseModel();

		try {
			addLog("=== EXAMINANDO ARCHIVO EN RESPALDO ===");
			addLog("Archivo: {}", nombreArchivo);

			// Configuración FTP
			String host = "180.183.171.164";
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

			datos.setDescriptor("FU");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			String path = "/home/ftpq0326/giom/recive/giomrespaldo/";

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				if (!ftpClient.changeWorkingDirectory(path)) {
					response.setStatus(500);
					response.setMessage("No se pudo cambiar al directorio de respaldo: " + path);
					return response;
				}

				// Verificar si el archivo existe
				FTPFile[] files = ftpClient.listFiles(nombreArchivo);
				if (files.length == 0) {
					response.setStatus(404);
					response.setMessage("El archivo " + nombreArchivo + " no existe en el directorio de respaldo");
					return response;
				}

				FTPFile fileInfo = files[0];
				addLog("INFORMACIÓN DEL ARCHIVO EN RESPALDO:");
				addLog(" - Nombre: {}", fileInfo.getName());
				addLog(" - Tamaño: {} bytes", fileInfo.getSize());

				if (fileInfo.getSize() == 0) {
					response.setStatus(400);
					response.setMessage("El archivo en respaldo está vacío (0 bytes)");
					return response;
				}

				// Descargar el contenido
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				boolean success = ftpClient.retrieveFile(nombreArchivo, outputStream);

				if (success) {
					String contenido = outputStream.toString("UTF-8");
					addLog("=== CONTENIDO DEL ARCHIVO DE RESPALDO ===");
					addLog(" - Tamaño: {} caracteres", contenido.length());

					// Analizar el contenido
					String[] lineas = contenido.split("\n");
					addLog(" - Total de líneas: {}", lineas.length);

					// Verificar si contiene ENDOFFILE
					boolean contieneEndOfFile = contenido.contains("ENDOFFILE");
					addLog(" - Contiene ENDOFFILE: {}", contieneEndOfFile);

					// Mostrar estructura del archivo
					if (lineas.length > 0) {
						addLog("--- ESTRUCTURA DEL ARCHIVO ---");
						addLog("PRIMERAS 3 LÍNEAS:");
						for (int i = 0; i < Math.min(3, lineas.length); i++) {
							addLog("Línea {} ({} chars): {}", i + 1, lineas[i].length(), lineas[i]);
						}

						addLog("ÚLTIMAS 3 LÍNEAS:");
						for (int i = Math.max(0, lineas.length - 3); i < lineas.length; i++) {
							addLog("Línea {} ({} chars): {}", i + 1, lineas[i].length(), lineas[i]);
						}

						// Analizar formato de las líneas
						if (lineas.length > 0) {
							addLog("--- ANÁLISIS DE FORMATO ---");
							String primeraLinea = lineas[0];
							addLog("Longitud primera línea: {} caracteres", primeraLinea.length());

							// Buscar campos específicos en la primera línea
							if (primeraLinea.length() > 50) {
								try {
									String incremental = primeraLinea.substring(0, 10);
									String idLote = primeraLinea.substring(10, 25);
									String idRegistro = primeraLinea.substring(25, 40);

									addLog("Campos detectados:");
									addLog(" - Incremental: '{}'", incremental);
									addLog(" - ID Lote: '{}'", idLote);
									addLog(" - ID Registro: '{}'", idRegistro);
								} catch (Exception e) {
									addLog("Error analizando formato: {}", e.getMessage());
								}
							}
						}
					}

					// Construir respuesta detallada
					Map<String, Object> resultado = new HashMap<>();
					resultado.put("nombreArchivo", nombreArchivo);
					resultado.put("tamañoBytes", fileInfo.getSize());
					resultado.put("tamañoCaracteres", contenido.length());
					resultado.put("numeroLineas", lineas.length);
					resultado.put("contieneEndOfFile", contieneEndOfFile);
					resultado.put("timestampArchivo", fileInfo.getTimestamp());
					resultado.put("contenido", contenido);

					response.setStatus(200);
					response.setMessage("Archivo de respaldo examinado exitosamente");
					response.setData(resultado);

				} else {
					response.setStatus(500);
					response.setMessage("Falló la descarga del archivo de respaldo. Código FTP: " + ftpClient.getReplyCode());
				}

			} else {
				response.setStatus(500);
				response.setMessage("No se pudo conectar al servidor FTP");
			}

		} catch (Exception e) {
			log.error("Error examinando archivo de respaldo: {}", e.getMessage(), e);
			response.setStatus(500);
			response.setMessage("Error examinando archivo de respaldo: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					addLog("Error desconectando FTP: {}", e.getMessage());
				}
			}
		}

		return response;
	}


	public ResponseModel descargarArchivoContenidoFTP(String nombreArchivo) {
		FTPClient ftpClient = null;
		ResponseModel response = new ResponseModel();

		try {
			addLog("=== DESCARGA DE CONTENIDO DIRECTO ===");
			addLog("Archivo: {}", nombreArchivo);

			// Configuración FTP (misma de siempre)
			String host = "180.183.171.164";
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

			datos.setDescriptor("FU");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			String path = "/home/ftpq0326/giom/recive/";

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				if (!ftpClient.changeWorkingDirectory(path)) {
					response.setStatus(500);
					response.setMessage("No se pudo cambiar al directorio: " + path);
					return response;
				}

				// Verificar información del archivo
				FTPFile[] files = ftpClient.listFiles(nombreArchivo);
				if (files.length == 0) {
					response.setStatus(404);
					response.setMessage("El archivo " + nombreArchivo + " no existe en el servidor FTP");
					return response;
				}

				FTPFile fileInfo = files[0];
				addLog("INFORMACIÓN DEL ARCHIVO:");
				addLog(" - Nombre: {}", fileInfo.getName());
				addLog(" - Tamaño: {} bytes", fileInfo.getSize());

				if (fileInfo.getSize() == 0) {
					response.setStatus(400);
					response.setMessage("El archivo está vacío (0 bytes) en el servidor FTP");
					return response;
				}

				// Descargar el contenido a memoria
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				boolean success = ftpClient.retrieveFile(nombreArchivo, outputStream);

				if (success) {
					// CORRECCIÓN: Convertir bytes a String correctamente
					String contenido = outputStream.toString("UTF-8");
					// O también puedes usar:
					// String contenido = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

					addLog("=== CONTENIDO DESCARGADO EXITOSAMENTE ===");
					addLog(" - Tamaño: {} caracteres", contenido.length());

					// Mostrar primeras líneas para diagnóstico
					String[] lineas = contenido.split("\n");
					addLog(" - Total de líneas: {}", lineas.length);

					if (lineas.length > 0) {
						addLog("--- PRIMERAS 5 LÍNEAS ---");
						for (int i = 0; i < Math.min(5, lineas.length); i++) {
							addLog("Línea {}: {}", i + 1, lineas[i]);
						}
						addLog("--- ÚLTIMAS 5 LÍNEAS ---");
						for (int i = Math.max(0, lineas.length - 5); i < lineas.length; i++) {
							addLog("Línea {}: {}", i + 1, lineas[i]);
						}
					}

					// Crear un objeto con la información y el contenido
					Map<String, Object> resultado = new HashMap<>();
					resultado.put("nombreArchivo", nombreArchivo);
					resultado.put("tamañoBytes", fileInfo.getSize());
					resultado.put("tamañoCaracteres", contenido.length());
					resultado.put("numeroLineas", lineas.length);
					resultado.put("contenido", contenido);
					resultado.put("timestamp", new Date());

					response.setStatus(200);
					response.setMessage("Archivo descargado exitosamente");
					response.setData(resultado);

				} else {
					response.setStatus(500);
					response.setMessage("Falló la descarga. Código FTP: " + ftpClient.getReplyCode());
					addLog("ERROR FTP: Código: {}, Mensaje: {}",
							ftpClient.getReplyCode(), ftpClient.getReplyString());
				}

			} else {
				response.setStatus(500);
				response.setMessage("No se pudo conectar al servidor FTP");
			}

		} catch (Exception e) {
			log.error("Error en descarga de contenido: {}", e.getMessage(), e);
			response.setStatus(500);
			response.setMessage("Error en descarga: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					addLog("Error desconectando FTP: {}", e.getMessage());
				}
			}
		}

		return response;
	}







	// === SISTEMA DE LOGS MEJORADO ===
	private static final int MAX_LOG_ENTRIES = 1000;
	private final List<String> logs = new CopyOnWriteArrayList<>();

	// Método auxiliar para formatear mensajes
	private String formatMessage(String format, Object... args) {
		if (format.contains("{}")) {
			return String.format(format.replace("{}", "%s"), args);
		} else {
			return String.format(format, args);
		}
	}

	// Método para identificar logs importantes
	// En GiomDao.java - método actualizado esLogImportante
	private boolean esLogImportante(String mensaje) {
		// Lista de patrones de logs CRÍTICOS que SÍ deben ir a BD
		List<String> patronesImportantes = Arrays.asList(
				"ERROR", "❌", "💥", "🚨", "🎉", "✅",
				"PROCESAMIENTO MASIVO COMPLETADO",
				"ARCHIVO", "registros parseados",
				"Lotes procesados",
				"Tiempo total:",
				"Velocidad:",
				"Iniciando proceso FTP",
				"Conexión establecida correctamente",
				"Proceso ejecutado correctamente",
				"Error al",
				"Excepción",
				"Falla al",
				"No fue posible",
				"CRÍTICO",
				"ADVERTENCIA",
				"ELIMINANDO",
				"PROCESANDO"
		);

		// Lista de patrones de logs RUIDOSOS que NO deben ir a BD (AGREGAMOS LOS NUEVOS)
		List<String> patronesRuidosos = Arrays.asList(
				"No se encontraron archivos que comiencen con GIOM_RSP101",
				"Uso de memoria actual:",  // Este ya estaba
				"Iniciando limpieza programada",
				"Después de limpiar caches",
				"No hay ejecución en este ciclo horario",
				"Resultado del procedimiento:",
				"Tamaño del archivo",
				"Procesando archivo:",
				"Bytes transferidos",
				"Código de respuesta FTP",
				"Mensaje de respuesta FTP",
				"Código de respuesta desconexión FTP",
				// LOS NUEVOS QUE QUIERES EXCLUIR:
				"Iniciando validación de horario",
				"Actualizando estados por fecha",
				"Resultado de validación de horas",
				"¡Ejecución permitida!",
				"Coincidencia con hora:",
				"Hora actual:",
				"Validando hora:"
		);

		// Si es ruidoso, NO es importante
		for (String patron : patronesRuidosos) {
			if (mensaje.contains(patron)) {
				addLogMemoria("[FILTRO] Excluyendo de BD: {}", mensaje.substring(0, Math.min(50, mensaje.length())));
				return false;
			}
		}

		// Si es importante, SÍ va a BD
		for (String patron : patronesImportantes) {
			if (mensaje.contains(patron)) {
				return true;
			}
		}

		// Por defecto, los logs no van a BD (solo memoria/consola)
		return false;
	}

	// === MÉTODOS DE LOG CON NIVELES DIFERENTES ===

	// Método addLog original (mantiene compatibilidad)
	public void addLog(String format, Object... args) {
		// Obtener métricas de memoria
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long usedMemoryMB = usedMemory / (1024 * 1024);
		long maxMemoryMB = maxMemory / (1024 * 1024);

		String memoryInfo = String.format(
				"[Memoria: %d MB usados / %d MB máximo] ",
				usedMemoryMB,
				maxMemoryMB
		);

		// Formatear mensaje
		String formattedMessage = formatMessage(format, args);
		String logEntry = memoryInfo + formattedMessage;

		// 1. Guardar en memoria
		logs.add(logEntry);
		if (logs.size() > MAX_LOG_ENTRIES) {
			logs.remove(0);
		}

		// 2. Determinar si es un log que DEBE ir a BD
		if (esLogImportante(formattedMessage)) {
			// Guardar en base de datos (solo logs importantes)
			guardarLogEnBD("INFO", logEntry, usedMemoryMB, maxMemoryMB);
		} else {
			// Solo log en consola para logs no importantes
			log.info(logEntry);
		}
	}

	// Log solo para memoria/consola (NO BD)
	public void addLogMemoria(String format, Object... args) {
		String formattedMessage = formatMessage(format, args);

		// Guardar en memoria
		logs.add(formattedMessage);
		if (logs.size() > MAX_LOG_ENTRIES) {
			logs.remove(0);
		}

		// Log en consola
		log.info(formattedMessage);
	}

	// Log importante (SÍ BD)
	public void addLogImportante(String format, Object... args) {
		String formattedMessage = formatMessage(format, args);

		// Obtener métricas de memoria
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long usedMemoryMB = usedMemory / (1024 * 1024);
		long maxMemoryMB = maxMemory / (1024 * 1024);

		String memoryInfo = String.format(
				"[Memoria: %d MB usados / %d MB máximo] ",
				usedMemoryMB,
				maxMemoryMB
		);

		String logEntry = memoryInfo + formattedMessage;

		// Guardar en memoria
		logs.add(logEntry);
		if (logs.size() > MAX_LOG_ENTRIES) {
			logs.remove(0);
		}

		// Guardar en BD
		guardarLogEnBD("INFO", logEntry, usedMemoryMB, maxMemoryMB);

		// Log en consola
		log.info(logEntry);
	}

	// Log de error (SÍ BD)
	public void addLogError(String format, Object... args) {
		String formattedMessage = formatMessage(format, args);

		// Obtener métricas de memoria
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long usedMemoryMB = usedMemory / (1024 * 1024);
		long maxMemoryMB = maxMemory / (1024 * 1024);

		String memoryInfo = String.format(
				"[Memoria: %d MB usados / %d MB máximo] ",
				usedMemoryMB,
				maxMemoryMB
		);

		String logEntry = memoryInfo + formattedMessage;

		// Guardar en memoria
		logs.add(logEntry);
		if (logs.size() > MAX_LOG_ENTRIES) {
			logs.remove(0);
		}

		// Guardar en BD como ERROR
		guardarLogEnBD("ERROR", logEntry, usedMemoryMB, maxMemoryMB);

		// Log en consola
		log.error(logEntry);
	}

	// === MÉTODOS PARA GUARDAR LOGS EN BD ===

	// Método principal para guardar log en BD (con 4 parámetros)
	private void guardarLogEnBD(String nivel, String mensaje, long memoriaUsada, long memoriaMaxima) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		String logEntrada = String.format("[%s] 🔧 PREPARANDO log para BD: %s", timestamp,
				mensaje.length() > 30 ? mensaje.substring(0, 30) + "..." : mensaje);
		logs.add(logEntrada);

		try {
			// Obtener información del método llamante
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			String metodo = "";

			for (int i = 4; i < stackTrace.length; i++) {
				String className = stackTrace[i].getClassName();
				if (!className.contains("GiomDao") && !className.contains("Log")) {
					metodo = stackTrace[i].getMethodName();
					break;
				}
			}

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_INSERTAR_LOG")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlParameter("P_NIVEL", OracleTypes.VARCHAR),
							new SqlParameter("P_MENSAJE", OracleTypes.VARCHAR),
							new SqlParameter("P_METODO", OracleTypes.VARCHAR),
							new SqlParameter("P_MEMORIA_USADA", OracleTypes.NUMERIC),
							new SqlParameter("P_MEMORIA_MAXIMA", OracleTypes.NUMERIC),
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_NIVEL", nivel);

			// Truncar mensaje a 4000 caracteres máximo
			String mensajeTruncado = mensaje.length() > 4000 ? mensaje.substring(0, 3999) : mensaje;
			inputMap.addValue("P_MENSAJE", mensajeTruncado);

			// Truncar método a 100 caracteres máximo
			String metodoTruncado = (metodo != null && metodo.length() > 100) ? metodo.substring(0, 99) : metodo;
			inputMap.addValue("P_METODO", metodoTruncado);

			inputMap.addValue("P_MEMORIA_USADA", memoriaUsada);
			inputMap.addValue("P_MEMORIA_MAXIMA", memoriaMaxima);

			// Ejecutar asíncronamente
			new Thread(() -> {
				try {
					Map<String, Object> resultMap = jdbcCall.execute(inputMap);
					String codRetorno = (String) resultMap.get("COD_RET");
					String descRetorno = (String) resultMap.get("DE_RET");

					if ("1000".equals(codRetorno)) {
						logs.add(String.format("[%s] ✅ Log insertado en BD correctamente", timestamp));
					} else {
						logs.add(String.format("[%s] ❌ Error BD: %s - %s", timestamp, codRetorno, descRetorno));
					}

					// Mantener límite de logs en memoria
					while (logs.size() > MAX_LOG_ENTRIES) {
						logs.remove(0);
					}
				} catch (Exception e) {
					logs.add(String.format("[%s] 💥 Error ejecutando insert BD: %s", timestamp, e.getMessage()));
				}
			}).start();

		} catch (Exception e) {
			logs.add(String.format("[%s] 💥 Error preparando insert BD: %s", timestamp, e.getMessage()));
		}

		// Mantener límite de logs en memoria
		while (logs.size() > MAX_LOG_ENTRIES) {
			logs.remove(0);
		}
	}

	// Método sobrecargado para cuando no se pasan las métricas de memoria
	private void guardarLogEnBD(String nivel, String mensaje) {
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		long usedMemoryMB = usedMemory / (1024 * 1024);
		long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);

		guardarLogEnBD(nivel, mensaje, usedMemoryMB, maxMemoryMB);
	}

	// === MÉTODOS DE CONSULTA DE LOGS ===

	public ResponseModel guardarLog(String nivel, String mensaje, String metodo, String clase, String usuario, String ip) {
		ResponseModel response = new ResponseModel();

		try {
			Runtime runtime = Runtime.getRuntime();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();
			long usedMemoryMB = usedMemory / (1024 * 1024);
			long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_INSERTAR_LOG")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR),
							new SqlParameter("P_NIVEL", OracleTypes.VARCHAR),
							new SqlParameter("P_MENSAJE", OracleTypes.VARCHAR),
							new SqlParameter("P_METODO", OracleTypes.VARCHAR),
							new SqlParameter("P_MEMORIA_USADA", OracleTypes.NUMBER),
							new SqlParameter("P_MEMORIA_MAXIMA", OracleTypes.NUMBER)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_NIVEL", nivel);
			inputMap.addValue("P_MENSAJE", mensaje.length() > 4000 ? mensaje.substring(0, 3999) : mensaje);
			inputMap.addValue("P_METODO", metodo != null && metodo.length() > 100 ? metodo.substring(0, 99) : metodo);
			inputMap.addValue("P_MEMORIA_USADA", usedMemoryMB);
			inputMap.addValue("P_MEMORIA_MAXIMA", maxMemoryMB);

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");

			if ("1000".equals(codRetorno)) {
				response.setStatus(200);
				response.setMessage(descRetorno);
			} else {
				response.setStatus(500);
				response.setMessage(descRetorno);
			}

		} catch (Exception e) {
			addLogError("Error al guardar log: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error al guardar log: " + e.getMessage());
		}

		return response;
	}

	public ResponseModel consultarLogs(ConsultarLogsDTO consultarLogsDTO) {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_CONSULTAR_LOGS")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlParameter("P_FECHA_DESDE", Types.TIMESTAMP),
							new SqlParameter("P_FECHA_HASTA", Types.TIMESTAMP),
							new SqlParameter("P_NIVEL", Types.VARCHAR),
							new SqlParameter("P_MEMORIA_MINIMA", Types.NUMERIC),
							new SqlOutParameter("P_CURSOR", OracleTypes.CURSOR, new LogRowMapper()),
							new SqlOutParameter("COD_RET", Types.VARCHAR),
							new SqlOutParameter("DE_RET", Types.VARCHAR)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_FECHA_DESDE", consultarLogsDTO.getFechaDesde() != null ?
					java.sql.Timestamp.valueOf(consultarLogsDTO.getFechaDesde()) : null);
			inputMap.addValue("P_FECHA_HASTA", consultarLogsDTO.getFechaHasta() != null ?
					java.sql.Timestamp.valueOf(consultarLogsDTO.getFechaHasta()) : null);
			inputMap.addValue("P_NIVEL", consultarLogsDTO.getNivel());
			inputMap.addValue("P_MEMORIA_MINIMA", consultarLogsDTO.getMemoriaMinima());

			Map<String, Object> result = jdbcCall.execute(inputMap);
			String codRetorno = (String) result.get("COD_RET");
			String descRetorno = (String) result.get("DE_RET");

			if ("1000".equals(codRetorno)) {
				response.setStatus(200);
				response.setData(result.get("P_CURSOR"));
				response.setMessage(descRetorno);
			} else {
				response.setStatus(500);
				response.setMessage(descRetorno);
			}

		} catch (Exception e) {
			addLogError("Error al consultar logs: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error al consultar logs: " + e.getMessage());
		}

		return response;
	}

	public ResponseModel obtenerEstadisticasMemoria() {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_OBTENER_ESTADISTICAS_MEMORIA")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlOutParameter("P_MEMORIA_PROMEDIO", Types.NUMERIC),
							new SqlOutParameter("P_MEMORIA_MAXIMA_REG", Types.NUMERIC),
							new SqlOutParameter("P_MEMORIA_MINIMA_REG", Types.NUMERIC),
							new SqlOutParameter("P_TOTAL_LOGS", Types.NUMERIC),
							new SqlOutParameter("COD_RET", Types.VARCHAR),
							new SqlOutParameter("DE_RET", Types.VARCHAR)
					);

			Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource());
			String codRetorno = (String) result.get("COD_RET");
			String descRetorno = (String) result.get("DE_RET");

			if ("1000".equals(codRetorno)) {
				Map<String, Object> stats = new HashMap<>();
				stats.put("memoria_promedio", result.get("P_MEMORIA_PROMEDIO"));
				stats.put("memoria_maxima", result.get("P_MEMORIA_MAXIMA_REG"));
				stats.put("memoria_minima", result.get("P_MEMORIA_MINIMA_REG"));
				stats.put("total_logs", result.get("P_TOTAL_LOGS"));

				response.setStatus(200);
				response.setData(stats);
				response.setMessage(descRetorno);
			} else {
				response.setStatus(500);
				response.setMessage(descRetorno);
			}

		} catch (Exception e) {
			addLogError("Error al obtener estadísticas: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error al obtener estadísticas: " + e.getMessage());
		}

		return response;
	}

	// Método para obtener logs en memoria
	public synchronized List<String> getLogs() {
		return new ArrayList<>(logs);
	}

	public ResponseModel probarConexionLogs() {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_INSERTAR_LOG")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR),
							new SqlParameter("P_NIVEL", OracleTypes.VARCHAR),
							new SqlParameter("P_MENSAJE", OracleTypes.VARCHAR),
							new SqlParameter("P_METODO", OracleTypes.VARCHAR),
							new SqlParameter("P_MEMORIA_USADA", OracleTypes.NUMBER),
							new SqlParameter("P_MEMORIA_MAXIMA", OracleTypes.NUMBER)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_NIVEL", "INFO");
			inputMap.addValue("P_MENSAJE", "Log de prueba - Conexión BD funcionando");
			inputMap.addValue("P_METODO", "probarConexionLogs");
			inputMap.addValue("P_MEMORIA_USADA", 100);
			inputMap.addValue("P_MEMORIA_MAXIMA", 1000);

			Map<String, Object> result = jdbcCall.execute(inputMap);
			String codRetorno = (String) result.get("COD_RET");
			String descRetorno = (String) result.get("DE_RET");

			if ("1000".equals(codRetorno)) {
				response.setStatus(200);
				response.setMessage("✅ Conexión a BD de logs funcionando correctamente");
				addLogImportante("Prueba de conexión a BD de logs: EXITOSA");
			} else {
				response.setStatus(500);
				response.setMessage("❌ Error en BD: " + descRetorno);
				addLogError("Prueba de conexión a BD de logs: FALLIDA - {}", descRetorno);
			}

		} catch (Exception e) {
			response.setStatus(500);
			response.setMessage("❌ Excepción al probar conexión: " + e.getMessage());
			addLogError("Prueba de conexión a BD de logs: EXCEPCIÓN - {}", e.getMessage());
		}

		return response;
	}

	// === MÉTODO PARA LISTAR ARCHIVOS EN RESPALDO ===

	public ResponseModel listarArchivosEnRespaldo() {
		FTPClient ftpClient = null;
		ResponseModel response = new ResponseModel();

		try {
			String host = "180.183.171.164";
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

			datos.setDescriptor("FU");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			String path = "/home/ftpq0326/giom/recive/giomrespaldo/";

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				if (!ftpClient.changeWorkingDirectory(path)) {
					addLogMemoria("No se pudo cambiar al directorio de respaldo: {}", path);
					response.setStatus(500);
					response.setMessage("No se pudo cambiar al directorio de respaldo: " + path);
					return response;
				}

				FTPFile[] files = ftpClient.listFiles();
				List<Map<String, Object>> archivosInfo = new ArrayList<>();

				addLogMemoria("=== ARCHIVOS EN DIRECTORIO DE RESPALDO ===");
				for (FTPFile file : files) {
					Map<String, Object> info = new HashMap<>();
					info.put("nombre", file.getName());
					info.put("tamaño", file.getSize());
					info.put("tipo", file.getType());
					info.put("timestamp", file.getTimestamp() != null ? file.getTimestamp().getTime() : null);

					archivosInfo.add(info);

					addLogMemoria("Archivo en respaldo: {} | Tamaño: {} bytes",
							file.getName(), file.getSize());
				}

				response.setStatus(200);
				response.setMessage("Se encontraron " + files.length + " archivos en respaldo");
				response.setData(archivosInfo);

			} else {
				addLogMemoria("No se pudo conectar al FTP");
				response.setStatus(500);
				response.setMessage("No se pudo conectar al FTP");
			}

		} catch (Exception e) {
			addLogError("Error listando archivos en respaldo: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error listando archivos en respaldo: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					addLogMemoria("Error desconectando FTP: {}", e.getMessage());
				}
			}
		}

		return response;
	}

	// === MÉTODO EJECUTAR FTP ===

	@Scheduled(cron = "0 */30 * * * *")
	public ResponseModel ejecutarFtp() {
		LoteDTO.resetContador();
		addLogMemoria("Iniciando el proceso asincrónico de mainframe automáticamente");

		// 0. Actualizar estados por fecha
		addLogMemoria("Actualizando estados por fecha...");
		ResponseModel estadoResponse = this.actualizarEstadosPorFecha();
		if (estadoResponse.getStatus() != 200) {
			addLogError("Error al actualizar estados por fecha: {}", estadoResponse.getMessage());
		} else {
			addLogMemoria("Actualización de estados por fecha completada: {}", estadoResponse.getMessage());
		}

		// 1. Verificar lotes activos
		boolean ejecutado = this.verificarDataMainframe();

		// 2. Validar horario configurado
		if (ejecutado) {
			addLogMemoria("Iniciando validación de horario...");
			ejecutado = false;

			ConsultarConfiguarcionDTO horaData = new ConsultarConfiguarcionDTO();
			horaData.setDescriptor("H");
			ResponseModel dataHoraSalida = this.consultarConfiguracion(horaData);

			if (dataHoraSalida.getStatus() == 200) {
				List<ParametrosDTO> horasData = (List<ParametrosDTO>) dataHoraSalida.getData();

				if (horasData != null && !horasData.isEmpty()) {
					LocalTime horaActual = LocalTime.now(ZoneId.of("America/Caracas"));
					addLogMemoria("Hora actual: {}", horaActual.format(DateTimeFormatter.ofPattern("HH:mm")));

					for (ParametrosDTO parametrosDTO : horasData) {
						if (parametrosDTO.getEstado() == 1) {
							LocalTime horaConfigurada = LocalTime.parse(parametrosDTO.getValorConfigurado());
							addLogMemoria("Validando hora: {}", horaConfigurada);

							if (horaConfigurada.getHour() == horaActual.getHour()) {
								addLogMemoria("¡Ejecución permitida! Coincidencia con hora: {}",
										horaConfigurada.format(DateTimeFormatter.ofPattern("HH:mm")));
								ejecutado = true;
								break;
							}
						}
					}
				}
			} else {
				addLogError("Error al consultar horas configuradas: {}", dataHoraSalida.getMessage());
			}
			addLogMemoria("Resultado de validación de horas: {}", ejecutado);
		}

		if (ejecutado) {
			ResponseModel lotesActivosResponse = obtenerLotesActivos();
			String valores = (String) lotesActivosResponse.getData();

			if (valores == null || valores.isEmpty()) {
				addLogMemoria("No se obtuvieron IDs de lotes activos.");
				return null;
			}

			String[] idLotesArray = valores.split(",");
			StringBuilder resultadoFinal = new StringBuilder();

			for (String idLote : idLotesArray) {
				ResponseModel respuestaProcedimiento = obtenerRespuestaDelProcedimiento(idLote.trim());

				if (respuestaProcedimiento.getStatus() == 200) {
					List<LoteDTO> resultados = (List<LoteDTO>) respuestaProcedimiento.getData();
					for (LoteDTO lote : resultados) {
						resultadoFinal.append(lote.toFormattedString()).append("\n");
					}
				} else {
					addLogError("Error al obtener respuesta para el lote: {}", idLote);
				}
			}

			String resultado = resultadoFinal.toString();

			if (resultado.isEmpty()) {
				addLogMemoria("No se obtuvieron datos del procedimiento, resultado es null o vacío.");
				return null;
			}

			// ⭐ CAMBIADO: Solo conteo va a BD, detalles van a memoria
			int numRegistros = resultado.split("\n").length;
			addLogImportante("📊 Resultado del procedimiento: {} registros listos para enviar", numRegistros);

			// Detalles solo en memoria
			if (numRegistros > 0) {
				String primeraLinea = resultado.split("\n")[0];
				String ultimaLinea = resultado.split("\n")[numRegistros - 1];
				addLogImportante("📋 Resumen: Primera línea: {}... | Última línea: {}...",
						primeraLinea.substring(0, Math.min(50, primeraLinea.length())),
						ultimaLinea.substring(0, Math.min(50, ultimaLinea.length())));
			}

			addLogMemoria("Se inicia el proceso de consulta de base de datos para determinar la data de envío");
			addLogMemoria("Iniciando proceso");
			FTPClient ftpClient = null;
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

			datos.setDescriptor("FH");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String host = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FU");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			addLogImportante("Iniciando consulta de documentos XML en el servidor FTP");
			addLogImportante("Host FTP: \r\n {} \r\n Contraseña de directorio remoto: {} \r\n Usuario: {}",
					host, password, usuario);
			addLogImportante("Iniciando conexión");

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient.isConnected()) {
				addLogImportante("Conexión establecida correctamente con el FTP");
				byte[] textoDecomposed = resultado.getBytes(StandardCharsets.UTF_8);

				int tamanioEntrada = textoDecomposed.length;
				addLogImportante("Tamaño archivo ENTRADA a enviar: {} bytes", tamanioEntrada);

				InputStream is = new ByteArrayInputStream(textoDecomposed);

				try {
					boolean success = ftpClient.storeFile("'VALP.GOFI.BAT1SBAS.GOJP0101.ENTRADA'", is);

					int bytesTransferidos = textoDecomposed.length - is.available();
					addLogImportante("Bytes transferidos para ENTRADA: {}/{}", bytesTransferidos, tamanioEntrada);

					addLogImportante("Código de respuesta FTP al tratar de registrar VALP.GOFI.BAT1SBAS.GOJP0101.ENTRADA {} El valor de ftpClient es : {}",
							ftpClient.getReplyCode(), ftpClient);
					addLogImportante("Mensaje de respuesta FTP al tratar de registrar VALP.GOFI.BAT1SBAS.GOJP0101.ENTRADA {}",
							ftpClient.getReplyString());

					if (success && ftpClient.getReplyCode() == 250) {
						addLogImportante("Escritura correcta en el mainframe");
						byte[] flagDecomposed = "PROCESO COMPLETADO".getBytes(StandardCharsets.UTF_8);

						int tamanioFlag = flagDecomposed.length;
						addLogImportante("Tamaño archivo FLAG a enviar: {} bytes", tamanioFlag);

						InputStream flagStream = new ByteArrayInputStream(flagDecomposed);
						success = ftpClient.storeFile("'VALP.GOFI.BAT1SBAS.GOJP0101.FLAG'", flagStream);

						bytesTransferidos = flagDecomposed.length - flagStream.available();
						addLogImportante("Bytes transferidos para FLAG: {}/{}", bytesTransferidos, tamanioFlag);

						addLogImportante("Código de respuesta FTP al tratar de registrar VALP.GOFI.BAT1SBAS.GOJP0101.FLAG {}",
								ftpClient.getReplyCode());
						addLogImportante("Mensaje de respuesta FTP al tratar de registrar VALP.GOFI.BAT1SBAS.GOJP0101.FLAG {}",
								ftpClient.getReplyString());

						if (success && ftpClient.getReplyCode() == 250) {
							addLogImportante("Proceso ejecutado correctamente, actualizando estados...");

							ArrayList<LoteMainframe> listaLotesEjecutar = this.consultarListaLotesMainframe();
							List<String> idLotes = listaLotesEjecutar.stream()
									.map(LoteMainframe::getIdlote)
									.collect(Collectors.toList());

							String idLotesStr = String.join(",", idLotes);
							ResponseModel response = this.actualizarEstadoLoteFTP(idLotesStr, "L");

							if (response.getStatus() == 200) {
								addLogImportante("Estados actualizados exitosamente");
							} else {
								addLogError("Error al actualizar estados: {}", response.getMessage());
							}
						} else {
							addLogError("Falla al escribir flag en el mainframe");
							addLogMemoria("Código de respuesta FTP {}", ftpClient.getReplyCode());
							addLogMemoria("Mensaje de respuesta FTP {}", ftpClient.getReplyString());
						}
					} else {
						addLogError("Falla al escribir data en el mainframe");
						addLogMemoria("Código de respuesta FTP {}", ftpClient.getReplyCode());
						addLogMemoria("Mensaje de respuesta FTP {}", ftpClient.getReplyString());
					}
				} catch (Exception e) {
					addLogError("No fue posible ejecutar el proceso asincrónico: {}", e.getMessage());
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						addLogMemoria("Error al cerrar el InputStream: {}", e.getMessage());
					}
				}
			} else {
				addLogError("Falló la conexión con el FTP");
			}

			try {
				ftpClient.disconnect();
				addLogMemoria("Código de respuesta desconexión FTP {}", ftpClient.getReplyCode());
				addLogMemoria("Mensaje de respuesta desconexión FTP {}", ftpClient.getReplyString());
			} catch (IOException ex) {
				addLogMemoria("No fue posible la desconexión con el servidor FTP: {}", ex.getMessage());
			}
		} else {
			addLogMemoria("No hay ejecución en este ciclo horario");
		}
		return null;
	}

	// === MÉTODO LEER ARCHIVO DESDE FTP ===

	@Scheduled(fixedDelay = 60000)
	public ResponseModel leerArchivoDesdeFTP() {
		FTPClient ftpClient = null;
		ResponseModel responseModel = new ResponseModel();

		try {
			String host = "180.183.171.164";
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

			datos.setDescriptor("FU");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			String path = "/home/ftpq0326/giom/recive/";
			String newDirectoryPath = "giomrespaldo/";

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				if (!ftpClient.changeWorkingDirectory(path)) {
					addLogMemoria("No se pudo cambiar al directorio: {}", path);
					responseModel.setStatus(500);
					responseModel.setMessage("No se pudo cambiar al directorio en el servidor FTP.");
					return responseModel;
				}

				FTPFile[] files = ftpClient.listFiles();
				List<String> archivosProceso = Arrays.stream(files)
						.filter(f -> f.getName().startsWith("GIOM_RSP101"))
						.map(FTPFile::getName)
						.collect(Collectors.toList());

				if (archivosProceso.isEmpty()) {
					addLogMemoria("No se encontraron archivos que comiencen con GIOM_RSP101.");
					responseModel.setStatus(404);
					responseModel.setMessage("No se encontraron archivos que procesar.");
					return responseModel;
				}

				addLogImportante("📁 Encontrados {} archivos para procesar en FTP", archivosProceso.size());

				for (String fileName : archivosProceso) {
					addLogImportante("Procesando archivo: {}", fileName);

					FTPFile fileInfo = ftpClient.listFiles(fileName)[0];
					long fileSize = fileInfo.getSize();
					addLogImportante("Tamaño del archivo {}: {} bytes", fileName, fileSize);

					if (fileSize == 0) {
						addLogImportante("Archivo {} está vacío, saltando...", fileName);
						continue;
					}

					InputStream inputStream = ftpClient.retrieveFileStream(fileName);

					if (inputStream != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						StringBuilder contenido = new StringBuilder();
						String line;
						List<String> lineasArchivo = new ArrayList<>();

						while ((line = reader.readLine()) != null) {
							contenido.append(line).append("\n");
							lineasArchivo.add(line);
						}
						reader.close();
						inputStream.close();

						boolean completed = ftpClient.completePendingCommand();
						if (!completed) {
							addLogError("No se completó la transferencia para: {}", fileName);
							continue;
						}

						if (!contenido.toString().contains("ENDOFFILE")) {
							addLogError("Archivo {} NO CONTIENE ENDOFFILE - Saltando", fileName);
							continue;
						}

						addLogImportante("Archivo {} contiene ENDOFFILE - Iniciando procesamiento MASIVO", fileName);

						ResponseModel procesoMasivo = procesarArchivoMasivamente(contenido.toString(), fileName, ftpClient, newDirectoryPath);

						if (procesoMasivo.getStatus() == 200) {
							addLogImportante("✅ PROCESAMIENTO MASIVO EXITOSO: {}", fileName);

							String newFilePath = newDirectoryPath + fileName;
							if (ftpClient.rename(fileName, newFilePath)) {
								addLogImportante("Archivo movido a respaldo: {}", newFilePath);
							} else {
								addLogMemoria("⚠️ No se pudo mover el archivo a respaldo");
							}
						} else {
							addLogError("❌ ERROR en procesamiento masivo: {}", procesoMasivo.getMessage());
						}
					}
				}
			}

		} catch (Exception e) {
			addLogError("Error al leer el archivo desde FTP: {}", e.getMessage());
			responseModel.setStatus(500);
			responseModel.setMessage("Error al leer el archivo desde FTP: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					addLogMemoria("Error al desconectar del servidor FTP: {}", e.getMessage());
				}
			}
		}

		return responseModel;
	}

	// === MÉTODO PROCESAR ARCHIVO MASIVAMENTE ===

	private ResponseModel procesarArchivoMasivamente(String contenido, String fileName, FTPClient ftpClient, String newDirectoryPath) {
		long inicioTotal = System.currentTimeMillis();
		ResponseModel responseModel = new ResponseModel();

		try {
			List<LoteDTO> todosLosRegistros = parsearTexto(contenido);
			addLogImportante("📊 ARCHIVO {}: {} registros parseados", fileName, todosLosRegistros.size());

			if (todosLosRegistros.isEmpty()) {
				responseModel.setStatus(400);
				responseModel.setMessage("No hay registros para procesar");
				return responseModel;
			}

			List<LoteDTO> registrosValidos = filtrarRegistrosValidos(todosLosRegistros);
			addLogImportante("✅ Registros válidos: {}/{}", registrosValidos.size(), todosLosRegistros.size());

			if (registrosValidos.isEmpty()) {
				responseModel.setStatus(400);
				responseModel.setMessage("No hay registros válidos para procesar");
				return responseModel;
			}

			Map<String, List<LoteDTO>> registrosPorLote = registrosValidos.stream()
					.collect(Collectors.groupingBy(LoteDTO::getIdLote));

			registrosPorLote.keySet().removeIf(key -> key == null || key.trim().isEmpty());
			addLogImportante("📦 LOTES VÁLIDOS IDENTIFICADOS: {}", registrosPorLote.keySet());

			int totalRegistrosProcesados = 0;
			int lotesProcesados = 0;

			for (Map.Entry<String, List<LoteDTO>> entry : registrosPorLote.entrySet()) {
				String idLote = entry.getKey();
				List<LoteDTO> registrosDelLote = entry.getValue();

				addLogMemoria("🔄 Procesando lote {} con {} registros", idLote, registrosDelLote.size());

				ResponseModel respuestaMasiva = actualizarRespuestasMainframeMasivo(registrosDelLote);

				if (respuestaMasiva.getStatus() == 200) {
					totalRegistrosProcesados += registrosDelLote.size();
					lotesProcesados++;
					addLogMemoria("✅ Lote {} procesado: {} registros", idLote, registrosDelLote.size());
				} else {
					addLogError("❌ Error en lote {}: {}", idLote, respuestaMasiva.getMessage());
				}

				actualizarEstadosLoteMasivo(idLote, registrosDelLote);
			}

			long tiempoTotal = System.currentTimeMillis() - inicioTotal;
			long segundos = tiempoTotal / 1000;
			double velocidad = segundos > 0 ? totalRegistrosProcesados / (double) segundos : totalRegistrosProcesados;

			addLogImportante("===========================================");
			addLogImportante("🎉 PROCESAMIENTO MASIVO COMPLETADO");
			addLogImportante("📁 Archivo: {}", fileName);
			addLogImportante("📊 Registros parseados: {}", todosLosRegistros.size());
			addLogImportante("✅ Registros válidos: {}", registrosValidos.size());
			addLogImportante("📈 Registros procesados: {}/{}", totalRegistrosProcesados, registrosValidos.size());
			addLogImportante("📦 Lotes procesados: {}", lotesProcesados);
			addLogImportante("⏱️ Tiempo total: {} segundos", segundos);
			addLogImportante("🚀 Velocidad: {}/segundo", String.format("%.2f", velocidad));
			addLogImportante("===========================================");

			responseModel.setStatus(200);
			responseModel.setMessage(String.format("Procesados %d de %d registros válidos en %d segundos",
					totalRegistrosProcesados, registrosValidos.size(), segundos));

		} catch (Exception e) {
			addLogError("❌ ERROR en procesamiento masivo: {}", e.getMessage());
			responseModel.setStatus(500);
			responseModel.setMessage("Error en procesamiento masivo: " + e.getMessage());
		}

		return responseModel;
	}

	// === MÉTODOS AUXILIARES PARA PROCESAMIENTO ===

	private List<LoteDTO> filtrarRegistrosValidos(List<LoteDTO> todosLosRegistros) {
		List<LoteDTO> validos = new ArrayList<>();
		List<LoteDTO> invalidos = new ArrayList<>();

		for (LoteDTO registro : todosLosRegistros) {
			if (esRegistroValido(registro)) {
				validos.add(registro);
			} else {
				invalidos.add(registro);
			}
		}

		if (!invalidos.isEmpty()) {
			addLogMemoria("⚠️ Se encontraron {} registros inválidos:", invalidos.size());
			for (LoteDTO invalido : invalidos) {
				addLogMemoria("   ❌ ID Registro: '{}', ID Lote: '{}'",
						invalido.getIdRegistro(), invalido.getIdLote());
			}
		}

		return validos;
	}

	private boolean esRegistroValido(LoteDTO registro) {
		if (registro.getIdRegistro() == null || registro.getIdRegistro().trim().isEmpty()) {
			return false;
		}
		if (registro.getIdLote() == null || registro.getIdLote().trim().isEmpty()) {
			return false;
		}
		return true;
	}

	private ResponseModel actualizarRespuestasMainframeMasivo(List<LoteDTO> registros) {
		ResponseModel response = new ResponseModel();

		if (registros == null || registros.isEmpty()) {
			response.setStatus(400);
			response.setMessage("Lista de registros vacía");
			return response;
		}

		try {
			List<LoteDTO> registrosValidos = registros.stream()
					.filter(r -> r.getIdRegistro() != null && !r.getIdRegistro().trim().isEmpty())
					.collect(Collectors.toList());

			if (registrosValidos.isEmpty()) {
				response.setStatus(400);
				response.setMessage("No hay registros válidos para actualizar");
				return response;
			}

			addLogMemoria("🔧 Registros válidos para actualización: {}/{}",
					registrosValidos.size(), registros.size());

			int totalProcessed = ejecutarBatchUpdateCompatible(registrosValidos);

			response.setStatus(200);
			response.setMessage("Actualización masiva exitosa: " + totalProcessed + " registros");
			addLogImportante("🎯 ACTUALIZACIÓN MASIVA COMPLETADA: {} registros", totalProcessed);

		} catch (Exception e) {
			addLogError("❌ Error en actualización masiva: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error en actualización masiva: " + e.getMessage());
		}

		return response;
	}

	private int ejecutarBatchUpdateCompatible(List<LoteDTO> registros) {
		int successfulUpdates = 0;

		try {
			int batchSize = 1000;

			for (int i = 0; i < registros.size(); i += batchSize) {
				int end = Math.min(i + batchSize, registros.size());
				List<LoteDTO> batch = registros.subList(i, end);

				int processedInBatch = procesarBatchIndividual(batch);
				successfulUpdates += processedInBatch;

				addLogMemoria("✅ Batch {}-{}: {} registros actualizados",
						i + 1, end, processedInBatch);
			}

		} catch (Exception e) {
			addLogError("❌ Error en batch update compatible: {}", e.getMessage());
		}

		return successfulUpdates;
	}

	private int procesarBatchIndividual(List<LoteDTO> batch) {
		int successfulUpdates = 0;

		try {
			for (LoteDTO registro : batch) {
				ResponseModel respuesta = actualizarRespuestaMainframeIndividual(registro);
				if (respuesta.getStatus() == 200) {
					successfulUpdates++;
				}
			}

		} catch (Exception e) {
			addLogError("❌ Error en procesamiento de batch individual: {}", e.getMessage());
		}

		return successfulUpdates;
	}

	public ResponseModel actualizarRespuestaMainframeIndividual(LoteDTO registro) {
		ResponseModel response = new ResponseModel();

		try {
			if (registro.getIdRegistro() == null || registro.getIdRegistro().trim().isEmpty()) {
				response.setStatus(400);
				response.setMessage("ID de registro vacío");
				return response;
			}

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_ACTUALIZAR_RESPUESTA_MAINFRAME")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR),
							new SqlParameter("P_ID_REGISTRO_GIOM_PK", OracleTypes.NUMBER),
							new SqlParameter("P_COD_RESPUESTA", OracleTypes.VARCHAR),
							new SqlParameter("P_DESC_RESPUESTA", OracleTypes.VARCHAR),
							new SqlParameter("P_SERIAL_RESPUESTA", OracleTypes.VARCHAR)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_ID_REGISTRO_GIOM_PK", Long.parseLong(registro.getIdRegistro()));
			inputMap.addValue("P_COD_RESPUESTA", registro.getCod_err() != null ? registro.getCod_err() : "");
			inputMap.addValue("P_DESC_RESPUESTA", registro.getDes_err() != null ? registro.getDes_err() : "");
			inputMap.addValue("P_SERIAL_RESPUESTA", registro.getTip_err() != null ? registro.getTip_err() : "");

			addLogMemoria("🔧 Actualizando registro individual: {}", registro.getIdRegistro());

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");

			if ("1000".equals(codRetorno)) {
				response.setStatus(200);
				response.setMessage(descRetorno);
				addLogMemoria("✅ Registro {} actualizado exitosamente", registro.getIdRegistro());
			} else {
				response.setStatus(500);
				response.setMessage(descRetorno);
				addLogError("❌ Error en registro {}: {}", registro.getIdRegistro(), descRetorno);
			}

		} catch (Exception e) {
			addLogError("❌ Excepción en registro {}: {}",
					registro.getIdRegistro(), e.getMessage());
			response.setStatus(500);
			response.setMessage("Error en actualización individual: " + e.getMessage());
		}

		return response;
	}

	private void actualizarEstadosLoteMasivo(String idLote, List<LoteDTO> registrosDelLote) {
		try {
			long countP = registrosDelLote.stream()
					.filter(r -> "00".equals(r.getCod_err()))
					.count();
			long countW = registrosDelLote.stream()
					.filter(r -> "50".equals(r.getCod_err()))
					.count();
			long countR = registrosDelLote.stream()
					.filter(r -> "99".equals(r.getCod_err()))
					.count();

			// Calcular monto total recuperado (basado en los DTOs)
			BigDecimal montoTotalRecuperado = registrosDelLote.stream()
					.filter(r -> "00".equals(r.getCod_err()))
					.map(r -> BigDecimal.valueOf(r.getMontoTransaccion()))
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			String estadoLote = (countW > 0) ? "W" : "P";

			addLogMemoria("📊 Lote {} - Distribución: P:{}, W:{}, R:{} -> Estado: {}",
					idLote, countP, countW, countR, estadoLote);

			// ====== PRIMERO: ACTUALIZAR ESTADOS DE TRANSACCIONES ======
			try {
				Long idLoteLong = Long.parseLong(idLote);
				ResponseModel updateResponse2 = actualizarRespuestaMainframe2(idLoteLong);
				if (updateResponse2.getStatus() != 200) {
					addLogMemoria("⚠️ Advertencia en actualización secundaria lote {}: {}",
							idLote, updateResponse2.getMessage());
				} else {
					addLogMemoria("✅ Estados de transacciones actualizados para lote {}", idLote);
				}
			} catch (NumberFormatException e) {
				addLogError("❌ Error parseando ID lote {}: {}", idLote, e.getMessage());
			}

			// ====== SEGUNDO: REGISTRAR COBRANZA ======
			// Solo si hay transacciones procesadas (P) con código 00
			if (countP > 0 && montoTotalRecuperado.compareTo(BigDecimal.ZERO) >= 0) {
				ResponseModel cobranzaResponse = registrarCobranzaLote(Long.parseLong(idLote));
				if (cobranzaResponse.getStatus() == 200) {
					addLogImportante("💰 Cobranza registrada para lote {}: monto total recuperado: {}",
							idLote, montoTotalRecuperado);
				} else {
					addLogError("❌ Error al registrar cobranza para lote {}: {}",
							idLote, cobranzaResponse.getMessage());
				}
			}

			// ====== TERCERO: ACTUALIZAR ESTADO DEL LOTE ======
			ResponseModel response = this.actualizarEstadoRegistro(idLote, estadoLote);
			if (response.getStatus() != 200) {
				addLogError("⚠️ Error al actualizar estado del lote {}: {}", idLote, response.getMessage());
			}

		} catch (Exception e) {
			addLogError("❌ Error en actualización de estados lote {}: {}", idLote, e.getMessage());
		}
	}

	// === REPORTES DEL LOTE EN CADA ENVIO DE COBRANZA ===

	public ResponseModel registrarCobranzaLote(Long idLote) {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_REGISTRAR_COBRANZA_LOTE")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlParameter("P_ID_LOTE", OracleTypes.NUMBER),
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_ID_LOTE", idLote);

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");

			response.setStatus("1000".equals(codRetorno) ? 200 : 500);
			response.setMessage(descRetorno);

		} catch (Exception e) {
			log.error("Error al registrar cobranza para lote {}: {}", idLote, e.getMessage());
			response.setStatus(500);
			response.setMessage("Error al registrar cobranza: " + e.getMessage());
		}

		return response;
	}

	public ResponseModel obtenerCobranzasPorRangoFecha(Date fechaInicio, Date fechaFin) {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_OBTENER_COBRANZAS_POR_FECHA")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlParameter("P_FECHA_INICIO", OracleTypes.TIMESTAMP),
							new SqlParameter("P_FECHA_FIN", OracleTypes.TIMESTAMP),
							new SqlOutParameter("P_OUT_DATA", OracleTypes.CURSOR),
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR)
					)
					.returningResultSet("P_OUT_DATA", new CobranzaRowMapper());

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_FECHA_INICIO", new java.sql.Timestamp(fechaInicio.getTime()));
			inputMap.addValue("P_FECHA_FIN", new java.sql.Timestamp(fechaFin.getTime()));

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");
			List<CobranzaDTO> cobranzas = (List<CobranzaDTO>) resultMap.get("P_OUT_DATA");

			response.setStatus("1000".equals(codRetorno) ? 200 : 500);
			response.setMessage(descRetorno);
			response.setData(cobranzas);

		} catch (Exception e) {
			log.error("Error al obtener cobranzas por rango de fecha: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error al obtener cobranzas: " + e.getMessage());
		}

		return response;
	}

	public ResponseModel obtenerResumenCobranzas(Date fechaInicio, Date fechaFin) {
		ResponseModel response = new ResponseModel();

		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withCatalogName("GIOM")
					.withProcedureName("PRC_OBTENER_RESUMEN_COBRANZAS")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlParameter("P_FECHA_INICIO", OracleTypes.TIMESTAMP),
							new SqlParameter("P_FECHA_FIN", OracleTypes.TIMESTAMP),
							new SqlOutParameter("P_OUT_DATA", OracleTypes.CURSOR),
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR)
					)
					.returningResultSet("P_OUT_DATA", new ResumenCobranzaRowMapper());

			MapSqlParameterSource inputMap = new MapSqlParameterSource();

			if (fechaInicio != null) {
				inputMap.addValue("P_FECHA_INICIO", new java.sql.Timestamp(fechaInicio.getTime()));
			} else {
				inputMap.addValue("P_FECHA_INICIO", null);
			}

			if (fechaFin != null) {
				inputMap.addValue("P_FECHA_FIN", new java.sql.Timestamp(fechaFin.getTime()));
			} else {
				inputMap.addValue("P_FECHA_FIN", null);
			}

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");
			List<Map<String, Object>> resumen = (List<Map<String, Object>>) resultMap.get("P_OUT_DATA");

			response.setStatus("1000".equals(codRetorno) ? 200 : 500);
			response.setMessage(descRetorno);
			response.setData(resumen);

		} catch (Exception e) {
			log.error("Error al obtener resumen de cobranzas: {}", e.getMessage());
			response.setStatus(500);
			response.setMessage("Error al obtener resumen: " + e.getMessage());
		}

		return response;
	}

	// === MONITOREO DE MEMORIA ===

	@Scheduled(fixedRate = 60000)
	public void logMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.totalMemory() - runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long usedMemoryMB = usedMemory / (1024 * 1024);
		long maxMemoryMB = maxMemory / (1024 * 1024);

		addLogMemoria("Uso de memoria actual: {} MB / {} MB", usedMemoryMB, maxMemoryMB);

		if (usedMemory > maxMemory * 0.8) {
			addLogImportante("🚨 ADVERTENCIA: Uso crítico de memoria: {} / {} MB", usedMemoryMB, maxMemoryMB);
			System.gc();
			addLogMemoria("🧹 Garbage Collection forzado debido a uso crítico de memoria");
		}
	}

	// === SISTEMA DE LOCK Y CACHE ===

	private final Map<String, Boolean> executionLocks = new ConcurrentHashMap<>();
	private final Map<String, Long> executionStartTimes = new ConcurrentHashMap<>();
	private static final long MAX_EXECUTION_TIME = 10 * 60 * 1000;

	public boolean acquireLock(String lockKey) {
		Long startTime = executionStartTimes.get(lockKey);
		if (startTime != null && (System.currentTimeMillis() - startTime) > MAX_EXECUTION_TIME) {
			addLogImportante("Liberando lock por timeout: {}", lockKey);
			releaseLock(lockKey);
		}
		return executionLocks.putIfAbsent(lockKey, true) == null;
	}

	public void releaseLock(String lockKey) {
		executionLocks.remove(lockKey);
		executionStartTimes.remove(lockKey);
	}

	public void setStartTime(String lockKey) {
		executionStartTimes.put(lockKey, System.currentTimeMillis());
	}

	@Autowired
	private Map<String, Object> cacheManager;

	public void clearCache() {
		cacheManager.clear();
	}

	@Scheduled(fixedRate = 300000)
	public void limpiarCachesProgramado() {
		addLogMemoria("Iniciando limpieza programada de cachés");
		clearCache();
		System.gc();
		logMemoryUsage();
	}

	// === MÉTODO ELIMINAR ARCHIVOS FTP ===

	public ResponseModel eliminarArchivosFTP() {
		FTPClient ftpClient = null;
		ResponseModel responseModel = new ResponseModel();

		String host = "180.183.171.164";
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

		try {
			datos.setDescriptor("FU");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String usuario = parametrosPivot.get(0).getValorConfigurado();

			datos.setDescriptor("FP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String password = parametrosPivot.get(0).getValorConfigurado();

			String path = "/home/ftpq0326/giom/recive/";

			addLogMemoria("Conectando al FTP - Host: {}, Usuario: {}", host, usuario);

			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				if (!ftpClient.changeWorkingDirectory(path)) {
					addLogMemoria("No se pudo cambiar al directorio: {}", path);
					responseModel.setStatus(500);
					responseModel.setMessage("No se pudo cambiar al directorio en el servidor FTP.");
					return responseModel;
				}

				addLogMemoria("Directorio actual cambiado a: {}", path);

				FTPFile[] files = ftpClient.listFiles();
				List<String> todosArchivos = Arrays.stream(files)
						.map(FTPFile::getName)
						.collect(Collectors.toList());

				addLogMemoria("Archivos encontrados en el directorio: {}", todosArchivos.size());
				addLogMemoria("Lista completa de archivos: {}", todosArchivos);

				List<String> archivosGIOM = Arrays.stream(files)
						.filter(f -> f.getName().startsWith("GIOM_RSP101"))
						.map(FTPFile::getName)
						.collect(Collectors.toList());

				if (archivosGIOM.isEmpty()) {
					addLogMemoria("No se encontraron archivos que comiencen con GIOM_RSP101");
					responseModel.setStatus(404);
					responseModel.setMessage("No se encontraron archivos GIOM_RSP101 para eliminar");
					return responseModel;
				}

				addLogImportante("Archivos GIOM_RSP101 encontrados: {}", archivosGIOM.size());
				addLogMemoria("Lista de archivos a eliminar: {}", archivosGIOM);

				int eliminadosExitosos = 0;
				for (String fileName : archivosGIOM) {
					boolean eliminado = ftpClient.deleteFile(fileName);
					if (eliminado) {
						eliminadosExitosos++;
						addLogImportante("Archivo {} eliminado exitosamente", fileName);
					} else {
						addLogError("Error al eliminar archivo {}. Código FTP: {}",
								fileName, ftpClient.getReplyCode());
					}
				}

				responseModel.setStatus(200);
				responseModel.setMessage(String.format(
						"Proceso completado. Eliminados %d de %d archivos GIOM_RSP101",
						eliminadosExitosos, archivosGIOM.size()
				));
				responseModel.setData(archivosGIOM);

				addLogImportante("Proceso de eliminación completado. Éxitos: {}, Fallos: {}",
						eliminadosExitosos, archivosGIOM.size() - eliminadosExitosos);

			} else {
				addLogError("No se pudo conectar al servidor FTP");
				responseModel.setStatus(500);
				responseModel.setMessage("No se pudo conectar al servidor FTP");
			}

		} catch (Exception e) {
			addLogError("Error crítico al eliminar archivos desde FTP: {}", e.getMessage());
			responseModel.setStatus(500);
			responseModel.setMessage("Error al eliminar archivos desde FTP: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					if (ftpClient.isConnected()) {
						ftpClient.logout();
						ftpClient.disconnect();
						addLogMemoria("Conexión FTP cerrada correctamente");
					}
				} catch (IOException e) {
					addLogMemoria("Error al desconectar del servidor FTP: {}", e.getMessage());
				}
			}
		}

		return responseModel;
	}

	// === MÉTODOS AUXILIARES QUE YA EXISTÍAN ===

	private void calcularYMostrarEstadisticas(int procesados, int total, long inicio, int faltantes) {
		try {
			int porcentaje = total > 0 ? (procesados * 100) / total : 0;
			long tiempo = System.currentTimeMillis() - inicio;
			long velocidad = tiempo > 0 ? (procesados * 1000) / tiempo : 0;
			long faltantesSegundos = (velocidad > 0) ? faltantes / velocidad : 0;

			addLogMemoria("ESTADÍSTICAS: {}% - {}s - {}/s - {}s faltantes",
					porcentaje, tiempo/1000, velocidad, faltantesSegundos);
		} catch (Exception e) {
			addLogMemoria("Error en estadísticas: {}", e.getMessage());
		}
	}

	private ResponseModel createSkippedResponse() {
		ResponseModel response = new ResponseModel();
		response.setStatus(200);
		response.setMessage("Ejecución omitida - Proceso anterior en curso");
		return response;
	}

	private String determinarEstadoRegistro(LoteDTO lote) {
		try {
			if (lote.getCod_err() != null && !lote.getCod_err().trim().isEmpty()) {
				String codigoError = lote.getCod_err().trim();
				switch (codigoError) {
					case "00":
						return "P";
					case "50":
						return "W";
					case "99":
						return "R";
					default:
						return "W";
				}
			} else {
				return "W";
			}
		} catch (Exception e) {
			addLogError("❌ Error determinando estado para registro {}: {}",
					lote.getIdRegistro(), e.getMessage());
			return "W";
		}
	}








	// Método auxiliar para extraer fecha y hora del nombre del archivo
	private LocalDateTime extraerFechaHora(String nombreArchivo) {
		// El formato esperado es: GIOM_RSP101.VE250620151530
		int index = nombreArchivo.indexOf(".VE");
		if (index == -1) {
			throw new IllegalArgumentException("Formato de archivo no válido: " + nombreArchivo);
		}
		String fechaHoraStr = nombreArchivo.substring(index + 3); // Obtiene "250620151530"
		if (fechaHoraStr.length() < 12) {
			throw new IllegalArgumentException("La cadena de fecha y hora es demasiado corta: " + fechaHoraStr);
		}
		int año = 2000 + Integer.parseInt(fechaHoraStr.substring(0, 2));
		int mes = Integer.parseInt(fechaHoraStr.substring(2, 4));
		int dia = Integer.parseInt(fechaHoraStr.substring(4, 6));
		int hora = Integer.parseInt(fechaHoraStr.substring(6, 8));
		int minuto = Integer.parseInt(fechaHoraStr.substring(8, 10));
		int segundo = Integer.parseInt(fechaHoraStr.substring(10, 12));

		return LocalDateTime.of(año, mes, dia, hora, minuto, segundo);
	}



	public ResponseModel listarArchivosEnDirectorio() {
		ResponseModel responseModel = new ResponseModel();
		respuestasFromDTO resp = new respuestasFromDTO();
		List<String> archivosLocal = new ArrayList<>(); // Almacenar nombres de archivos

		try (Stream<Path> walk = Files.walk(Paths.get("/home/oraclew/Oracle/Middleware/Oracle_Home/user_projects/domains/java_domain/servers/AdminServer/upload/"))) {
			// Filtrar todos los archivos en el directorio especificado
			archivosLocal = walk.filter(Files::isRegularFile)
					.map(x -> x.getFileName().toString())
					.collect(Collectors.toList());

			// Registrar la lista de archivos encontrados
			log.info("Archivos encontrados: {}", archivosLocal);

			resp.setEstatus("SUCCESS");
			resp.setMensaje("Consulta realizada exitosamente");
			resp.setData(archivosLocal); // Almacenar los nombres de todos los archivos encontrados
		} catch (IOException e) {
			resp.setEstatus("ERROR");
			resp.setMensaje("Error al buscar archivos: " + e.getMessage());
			log.error("Error al buscar archivos", e);
		}

		// Aquí puedes decidir cómo devolver la respuesta final
		responseModel.setStatus(200); // Código de éxito
		responseModel.setMessage(resp.getMensaje());
		responseModel.setData(archivosLocal); // Almacenar la lista de archivos encontrados

		return responseModel; // Devolver el ResponseModel
	}



	public ResponseModel modificardatosestadoReprocesado(EstadosLoteDTO datos) {
		ResponseModel response = new ResponseModel();
		try {
			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate);
			jdbcCall.withProcedureName("PRC_UDATE_ESTADOS_LOTE_REPROCESADO");
			jdbcCall.withoutProcedureColumnMetaDataAccess();
			jdbcCall.setFunction(false);
			jdbcCall.declareParameters(new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
					new SqlOutParameter("DE_CODRET", OracleTypes.VARCHAR),
					new SqlParameter("ID_LOTE_GIOM_PK", Types.NUMERIC), new SqlParameter("NUMERO", Types.VARCHAR)
			);
			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("ID_LOTE_GIOM_PK", datos.getIdlote());
			inputMap.addValue("NUMERO", datos.getNumero());
			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String cod_retorno = (String) resultMap.get("COD_RET");
			String desc_retorno = (String) resultMap.get("DE_CODRET");
			log.info("Resultado => {},{}", cod_retorno, desc_retorno);
			if (cod_retorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setCode(Integer.parseInt(cod_retorno));
				response.setMessage(desc_retorno);
				// registramos la auditoria
				try {
					AuditoriasDTO datosAuditoria = new AuditoriasDTO();
					datosAuditoria.setAccion("UPDATE DE LOTE");
					datosAuditoria.setDescripcion("SE MODIFICA EL ESTATUS DEL LOTE " + datos.getIdlote()
							+ " A EL ESTATUS " + datos.getNumero());
					datosAuditoria.setUsuario(datos.getUsuario());
					datosAuditoria.setIdregistroauditoria(datos.getIdlote().intValue());
					boolean respuestaAuditoria = this.guardarauditoriaDinamico(datosAuditoria);
					if (respuestaAuditoria) {
						log.info("fue actualizado correctamente la auditoria del lote");
					} else {
						log.error("no fue posible actualizar la auditoria del lote");
					}
					PeapoleDataEntradaDTO data = new PeapoleDataEntradaDTO();
					data.setCedula(datos.getCedula());
					PeapoleDataDTO peapoleData = this.consultaPeapole(data);
					String decripcionSeguimiento = "Modificacion de estado de lote, a estatus: " + datos.getNumero();
					String codigoEmpleado = "";
					String nomreEmpleado = peapoleData.getDatosTrabADE().getNombres();
					String apellidoEmpleado = peapoleData.getDatosTrabADE().getApellidos();
					String cedulaEmpleado = peapoleData.getDatosTrabADE().getCedula();
					String codigoUnidad = peapoleData.getDatosTrabADE().getCodDepto();
					String descripcionUnidad = peapoleData.getDatosTrabADE().getDescDepto();

					boolean seguimientoOk = this.guardarSeguimiento(decripcionSeguimiento, codigoEmpleado,
							nomreEmpleado, apellidoEmpleado, cedulaEmpleado, codigoUnidad, descripcionUnidad,
							datos.getIp(), datos.getIdlote().toString());

					if (seguimientoOk) {
						log.info("fue actualizado correctamente el seguimiento del lote");
					} else {
						log.error("no fue posible actualizar el seguimiento del lote");
					}
				} catch (Exception e) {
					log.error("no fue posible registrar la auditoria/seguimiento ", e);
				}
				return response;
			} else {
				response.setCode(1001);
				response.setMessage("Error al modificar los datos => datos (" + datos.toString() + ")");
				response.setStatus(500);
				return response;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			response.setCode(9999);
			response.setMessage("ERROR PRC_UDATE_ESTADOS_LOTE Exception");
			response.setStatus(500);
			return response;
		}
	}


	public List<LoteDTO> parsearTexto(String contenido) {
		List<LoteDTO> lotes = new ArrayList<>();

		// Dividir el contenido en líneas (si hay varias líneas)
		String[] lineas = contenido.split("\n");

		for (String linea : lineas) {
			if (linea.length() >= 220) {
				LoteDTO lote = new LoteDTO();

				try {
					// Asignar valores a las propiedades del DTO según las posiciones
					lote.setIncremental(linea.substring(0, 10).trim());
					lote.setIdLote(linea.substring(11, 25).trim());
					lote.setIdRegistro(linea.substring(26, 40).trim());
					lote.setReferencia(linea.substring(41, 48).trim());
					lote.setTipoMovimiento(linea.substring(49, 50).trim());
					lote.setFecha(linea.substring(50, 57).trim());
					lote.setCedula(linea.substring(58, 58).trim());
					lote.setNumeroOrdenante(linea.substring(59, 69).trim());
					lote.setDigitoOrdenante(linea.substring(70, 70).trim());
					lote.setNumeroDeCuenta(linea.substring(71, 90).trim());
					lote.setSerialOperacion(linea.substring(91, 95).trim());
					lote.setCodigoOperacion(linea.substring(96, 99).trim());

					// Validar y convertir montoTransaccion
					String montoStr = linea.substring(100, 116).trim();
					if (!montoStr.isEmpty()) {
						lote.setMontoTransaccion(Double.valueOf(montoStr));
					} else {
						lote.setMontoTransaccion(0.0);
					}

					// Asignaciones adicionales
					lote.setCampoLibre(linea.substring(116, 116).trim());
					lote.setValidaCedula(linea.substring(117, 118).trim());
					lote.setTipoLote(linea.substring(118, 120).trim());

					//Respuesta de Mainframe
					lote.setObservacion(linea.substring(121, 170).trim());
					lote.setCod_err(linea.substring(170, 172).trim());
					lote.setTip_err(linea.substring(172, 179).trim());
					lote.setDes_err(linea.substring(182, 219).trim());

					// Agregar el lote a la lista
					lotes.add(lote);
				} catch (NumberFormatException e) {
					log.error("Error al convertir montoTransaccion: {}", e.getMessage());
				} catch (Exception e) {
					log.error("Error al procesar la línea: {}. Error: {}", linea, e.getMessage());
				}
			} else {
				log.warn("La línea no tiene la longitud esperada: {}", linea);
			}
		}

		return lotes;
	}


	public ResponseModel actualizarRespuestaMainframe(LoteDTO datos) {
		ResponseModel response = new ResponseModel();
		try {
			// Validar que el ID de registro no sea nulo o vacío
			if (datos.getIdRegistro() == null || datos.getIdRegistro().isEmpty()) {
				response.setCode(9999);
				response.setMessage("El ID de registro es nulo o vacío.");
				response.setStatus(400); // Bad Request
				return response;
			}

			// Similar validación para otros campos necesarios si fuera necesario
			if (datos.getCod_err() == null || datos.getCod_err().isEmpty()) {
				response.setCode(9999);
				response.setMessage("El código de error es nulo o vacío.");
				response.setStatus(400); // Bad Request
				return response;
			}

			SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withProcedureName("GIOM.PRC_ACTUALIZAR_RESPUESTA_MAINFRAME")
					.withoutProcedureColumnMetaDataAccess()
					.declareParameters(
							new SqlOutParameter("COD_RET", OracleTypes.VARCHAR),
							new SqlOutParameter("DE_RET", OracleTypes.VARCHAR),
							new SqlParameter("P_ID_REGISTRO_GIOM_PK", OracleTypes.NUMBER),
							new SqlParameter("P_COD_RESPUESTA", OracleTypes.VARCHAR),
							new SqlParameter("P_DESC_RESPUESTA", OracleTypes.VARCHAR),
							new SqlParameter("P_SERIAL_RESPUESTA", OracleTypes.VARCHAR)
					);

			MapSqlParameterSource inputMap = new MapSqlParameterSource();
			inputMap.addValue("P_ID_REGISTRO_GIOM_PK", datos.getIdRegistro());
			inputMap.addValue("P_COD_RESPUESTA", datos.getCod_err());
			inputMap.addValue("P_DESC_RESPUESTA", datos.getDes_err());
			inputMap.addValue("P_SERIAL_RESPUESTA", datos.getTip_err());

			Map<String, Object> resultMap = jdbcCall.execute(inputMap);
			String codRetorno = (String) resultMap.get("COD_RET");
			String descRetorno = (String) resultMap.get("DE_RET");

			if (codRetorno.equals("1000")) {
				response.setCode(1000);
				response.setStatus(200);
				response.setMessage(descRetorno);
			} else {
				response.setCode(9999);
				response.setMessage(descRetorno);
				response.setStatus(204);
			}
		} catch (Exception e) {
			response.setCode(9999);
			response.setMessage("Error al llamar al procedimiento PRC_ACTUALIZAR_RESPUESTA_MAINFRAME: " + e.getMessage());
			response.setStatus(500);
			log.error(e.getMessage(), e);
		}
		return response;
	}



	public ResponseModel comprimirArchivoLocal() {
		FTPClient ftpClient = null;
		InputStream inputStream = null;
		ResponseModel responseModel = new ResponseModel();

		String host = "180.183.174.156";
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

		// Obtener configuración del usuario
		datos.setDescriptor("FU");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String usuario = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración de la contraseña
		datos.setDescriptor("FP");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String password = parametrosPivot.get(0).getValorConfigurado();

		String path = "/home/ftpd0326/giom/recive/";
		String fileName = "prueba.txt"; // Archivo específico a procesar
		String newDirectory = "archivo_zip"; // Nombre del nuevo directorio
		String zipFileName = fileName.replace(".txt", ".zip"); // Nombre del archivo ZIP

		// Declarar ByteArrayOutputStream fuera del bloque try-with-resources
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				// Intentar recuperar el archivo específico
				ftpClient.changeWorkingDirectory(path);
				inputStream = ftpClient.retrieveFileStream(fileName);

				if (inputStream != null) {
					// Crear el nuevo directorio en el servidor FTP si no existe
					if (!ftpClient.changeWorkingDirectory(newDirectory)) {
						ftpClient.makeDirectory(newDirectory);
						ftpClient.changeWorkingDirectory(newDirectory);
					}

					// Crear un InputStream para el ZIP que se mandará al FTP
					try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

						// Crear entrada para el archivo en el ZIP
						ZipEntry zipEntry = new ZipEntry(fileName);
						zipOutputStream.putNextEntry(zipEntry);

						// Leer el archivo original y escribir en el ZIP
						byte[] bytes = new byte[1024];
						int length;
						while ((length = inputStream.read(bytes)) >= 0) {
							zipOutputStream.write(bytes, 0, length);
						}
						zipOutputStream.closeEntry();
					}

					// Subir el archivo ZIP al servidor FTP
					ftpClient.changeWorkingDirectory(newDirectory); // Asegúrate de estar en el nuevo directorio
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
					boolean success = ftpClient.storeFile(zipFileName, byteArrayInputStream);

					// Cerrar streams
					byteArrayInputStream.close();

					if (success) {
						log.info("Archivo comprimido creado y subido exitosamente: {}", zipFileName);
						responseModel.setStatus(200);
						responseModel.setMessage("El archivo prueba.txt ha sido comprimido y subido correctamente.");
					} else {
						log.error("Error al subir el archivo ZIP al servidor FTP.");
						responseModel.setStatus(500);
						responseModel.setMessage("Error al subir el archivo ZIP al servidor FTP.");
					}

					ftpClient.completePendingCommand(); // Completar la transferencia
				} else {
					log.error("El archivo prueba.txt no fue encontrado en el servidor.");
					responseModel.setStatus(404);
					responseModel.setMessage("El archivo prueba.txt no fue encontrado en el servidor.");
				}
			} else {
				log.error("No se pudo conectar al servidor FTP.");
				responseModel.setStatus(500);
				responseModel.setMessage("No se pudo conectar al servidor FTP.");
			}
		} catch (Exception e) {
			log.error("Error al leer y comprimir archivos desde FTP: ", e);
			responseModel.setStatus(500);
			responseModel.setMessage("Error al leer y comprimir archivos desde FTP: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					log.error("Error al desconectar del servidor FTP", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close(); // Asegurarse de cerrar el InputStream
				} catch (IOException e) {
					log.error("Error al cerrar InputStream", e);
				}
			}
			try {
				byteArrayOutputStream.close(); // Cerrar ByteArrayOutputStream
			} catch (IOException e) {
				log.error("Error al cerrar ByteArrayOutputStream", e);
			}
		}

		return responseModel;
	}

	private void compressToZip(InputStream inputStream, String zipFilePath) {
		try (FileOutputStream fos = new FileOutputStream(zipFilePath);
			 ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			ZipEntry zipEntry = new ZipEntry("archivo.txt"); // Nombrar el archivo dentro del zip
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = inputStream.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}

			zipOut.closeEntry();
		} catch (IOException e) {
			log.error("Error al comprimir el archivo a .zip", e);
		}
	}


	public ResponseModel moverArchivoEnFTP() {
		FTPClient ftpClient = null;
		ResponseModel responseModel = new ResponseModel();

		String host = "180.183.174.156"; // Dirección del servidor FTP
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("FU");

		// Obtener configuración del usuario
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String usuario = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración de la contraseña
		datos.setDescriptor("FP");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String password = parametrosPivot.get(0).getValorConfigurado();

		String path = "/home/ftpd0326/giom/recive/";
		String fileName = "GIOM_RSP101.VE241127115229";
		String newFilePath = "giomrespaldo/" + fileName; // Solo dejar la ruta relativa para el nuevo archivo

		try {
			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

			if (ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode();

				// Cambiar al directorio de origen
				if (!ftpClient.changeWorkingDirectory(path)) {
					log.error("No se pudo cambiar al directorio: {}", path);
					responseModel.setStatus(500);
					responseModel.setMessage("No se pudo cambiar al directorio en el servidor FTP.");
					return responseModel;
				} else {
					log.info("Cambiado al directorio: {}", path);
				}

				// Verificar si el archivo existe
				FTPFile[] files = ftpClient.listFiles();
				boolean fileExists = Arrays.stream(files).anyMatch(f -> f.getName().equals(fileName));
				if (!fileExists) {
					responseModel.setStatus(500);
					responseModel.setMessage("El archivo no existe en el directorio de origen.");
					return responseModel;
				}

				// Intentar mover el archivo al directorio de respaldo
				if (ftpClient.rename(fileName, newFilePath)) {
					responseModel.setStatus(200);
					responseModel.setMessage("Archivo movido exitosamente a: " + newFilePath);
				} else {
					int replyCode = ftpClient.getReplyCode(); // Obtener el código de respuesta
					String replyString = ftpClient.getReplyString(); // Obtener mensaje de respuesta
					log.error("No se pudo mover el archivo. Código de respuesta: {}, Mensaje: {}", replyCode, replyString);
					responseModel.setStatus(500);
					responseModel.setMessage("Error al mover el archivo: " + replyString);
				}
			} else {
				log.error("No se pudo conectar al servidor FTP.");
				responseModel.setStatus(500);
				responseModel.setMessage("No se pudo conectar al servidor FTP.");
			}
		} catch (Exception e) {
			log.error("Error al mover el archivo en FTP", e);
			responseModel.setStatus(500);
			responseModel.setMessage("Error al mover el archivo en FTP: " + e.getMessage());
		} finally {
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					log.error("Error al desconectar del servidor FTP", e);
				}
			}
		}
		return responseModel;
	}



	public ResponseModel leerArchivoDesdeFTP2() {
		FTPClient ftpClient = null; // Inicializar el FTPClient
		InputStream inputStream = null;
		ResponseModel responseModel = new ResponseModel();

		// Establecer el host FTP directamente
		String host = "180.183.174.156"; // Dirección del servidor FTP
		// Crear un objeto para consultar la configuración
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

		// Obtener configuración del usuario
		datos.setDescriptor("FU");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String usuario = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración de la contraseña
		datos.setDescriptor("FP");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String password = parametrosPivot.get(0).getValorConfigurado();

		// Configurar el path y el nombre del archivo
		String path = "/home/ftpd0326/giom/recive/"; // Asegúrate de que el path sea correcto
		String fileName = "GIOM_RSP101.VE241219115605"; // Nombre del archivo a leer

		try {
			ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password); // Usar FtpUtil para obtener el cliente FTP

			if (ftpClient.isConnected()) {
				ftpClient.enterLocalPassiveMode(); // Cambia a modo pasivo

				// Cambiar al directorio donde se encuentra el archivo
				if (!ftpClient.changeWorkingDirectory(path)) {
					log.error("No se pudo cambiar al directorio: {}", path);
					responseModel.setStatus(500);
					responseModel.setMessage("No se pudo cambiar al directorio en el servidor FTP.");
					return responseModel;
				}

				inputStream = ftpClient.retrieveFileStream(fileName);

				if (inputStream != null) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					StringBuilder contenido = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						contenido.append(line).append("\n");
					}
					reader.close();

					// Llamar al método para parsear el contenido
					List<LoteDTO> lotes = parsearTexto(contenido.toString());

					responseModel.setData(lotes); // Establecer la lista de lotes en el ResponseModel
					// responseModel.setData(contenido.toString()); // Establecer el contenido en el ResponseModel
					responseModel.setStatus(200); // Código de éxito
				} else {
					log.error("El archivo no fue encontrado en el servidor.");
					responseModel.setStatus(404); // Archivo no encontrado
					responseModel.setMessage("El archivo no fue encontrado en el servidor.");
				}
			} else {
				log.error("No se pudo conectar al servidor FTP.");
				responseModel.setStatus(500); // Error de conexión
				responseModel.setMessage("No se pudo conectar al servidor FTP.");
			}
		} catch (Exception e) {
			log.error("Error al leer el archivo desde FTP", e);
			responseModel.setStatus(500); // Error interno del servidor
			responseModel.setMessage("Error al leer el archivo desde FTP: " + e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Error al cerrar el InputStream", e);
				}
			}
			if (ftpClient != null) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					log.error("Error al desconectar del servidor FTP", e);
				}
			}
		}
		return responseModel;
	}



	public ResponseModel ejecutarFtpAutomatico2() {
		log.info("Iniciando el proceso asincrónico de mainframe automáticamente");

		//********** LOGICA PARA OBTENER EL ID DE LOS LOTES, EXTRAER TODA LA DATA DE SUS ARCHIVOS.TXT Y ENVIARLA A TRAVES DE UN ARRAY **********


		// Llamar al método obtenerLotesActivos para obtener los valores
		ResponseModel lotesActivosResponse = obtenerLotesActivos();
		String valores = (String) lotesActivosResponse.getData(); // Obtener el resultado como String

		if (valores == null || valores.isEmpty()) {
			log.error("No se obtuvieron IDs de lotes activos.");
			return null;
		}

		String[] idLotesArray = valores.split(","); // Suponiendo que los IDs están separados por comas
		StringBuilder resultadoFinal = new StringBuilder();

		for (String idLote : idLotesArray) {
			ResponseModel respuestaProcedimiento = obtenerRespuestaDelProcedimiento(idLote.trim());

			if (respuestaProcedimiento.getStatus() == 200) {
				// Obtener la lista de LoteDTO
				List<LoteDTO> resultados = (List<LoteDTO>) respuestaProcedimiento.getData();
				// Convertir la lista de LoteDTO a un String
				for (LoteDTO lote : resultados) {
					// Aquí debes definir cómo quieres convertir cada LoteDTO a String
					resultadoFinal.append(lote.toFormattedString()).append("\n"); // Asegúrate de que LoteDTO tenga un método toString adecuado
				}
			} else {
				log.error("Error al obtener respuesta para el lote: {}", idLote);
			}
		}

		String resultado = resultadoFinal.toString(); // Convertir el StringBuilder a String

		if (resultado.isEmpty()) {
			log.error("No se obtuvieron datos del procedimiento, resultado es null o vacío.");
			return null;
		}

		log.info("Resultado del procedimiento: {}", resultado);



		//********************************************************************************************************************************************


		log.info("Se inicia el proceso de consulta de base de datos para determinar la data de envío");
		log.info("Iniciando proceso");
		FTPClient ftpClient = null;
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

		// Obtener configuración del host FTP
		datos.setDescriptor("FH");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String host = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración de la contraseña
		datos.setDescriptor("FP");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String password = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración del usuario
		datos.setDescriptor("FU");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String usuario = parametrosPivot.get(0).getValorConfigurado();

		log.info("Iniciando consulta de documentos XML en el servidor FTP");
		log.info("Host FTP: \r\n " + host + " \r\n Contraseña de directorio remoto: " + password + " \r\n Usuario: " + usuario);
		log.info("Iniciando conexión");

		ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

		if (ftpClient.isConnected()) {
			log.info("Conexión establecida correctamente con el FTP");
			byte[] textoDecomposed = resultado.getBytes(StandardCharsets.UTF_8);
			InputStream is = new ByteArrayInputStream(textoDecomposed);

			try {
				boolean success = ftpClient.storeFile("'VALT.GOFI.BAT1SBAS.GOJT0101.ENTRADA'", is);
				log.info("Código de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.ENTRADA "
						+ ftpClient.getReplyCode() + " El valor de ftpClient es : " + ftpClient);
				log.info("Mensaje de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.ENTRADA "
						+ ftpClient.getReplyString());

				if (success && ftpClient.getReplyCode() == 250) {
					log.info("Escritura correcta en el mainframe");
					byte[] flagDecomposed = "PROCESO COMPLETADO".getBytes(StandardCharsets.UTF_8);
					InputStream flagStream = new ByteArrayInputStream(flagDecomposed);
					success = ftpClient.storeFile("'VALT.GOFI.BAT1SBAS.GOJT0101.FLAG'", flagStream);
					log.info("Código de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.FLAG "
							+ ftpClient.getReplyCode());
					log.info("Mensaje de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.FLAG "
							+ ftpClient.getReplyString());

					if (success && ftpClient.getReplyCode() == 250) {
						log.info("Proceso ejecutado correctamente, procediendo a modificar los estados de los lotes y transacciones");
						ArrayList<LoteMainframe> listaLotesEjecutar = this.consultarListaLotesMainframe();
						List<String> idLotes = new ArrayList<>();
						List<String> idTransacciones = new ArrayList<>();

						for (LoteMainframe guardarLoteDTO : listaLotesEjecutar) {
							idLotes.add(guardarLoteDTO.getIdlote());
						}

						boolean loteOk = this.cambiarEstatusLoteMasivo("L", idLotes);
						if (loteOk) {
							boolean transaccionOk = this.cambiarEstatusTransaccionMasivo("L", idTransacciones);
							if (transaccionOk) {
								log.info("Estados de lotes y transacciones actualizados correctamente.");
							} else {
								log.error("Error al actualizar los estados de las transacciones.");
							}
						} else {
							log.error("Error al actualizar los estados de los lotes.");
						}
					} else {
						log.error("Falla al escribir flag en el mainframe");
						log.error("Código de respuesta FTP " + ftpClient.getReplyCode());
						log.error("Mensaje de respuesta FTP " + ftpClient.getReplyString());
					}
				} else {
					log.error("Falla al escribir data en el mainframe");
					log.error("Código de respuesta FTP " + ftpClient.getReplyCode());
					log.error("Mensaje de respuesta FTP " + ftpClient.getReplyString());
				}
			} catch (Exception e) {
				log.error("No fue posible ejecutar el proceso asincrónico", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					log.error("Error al cerrar el InputStream", e);
				}
			}
		} else {
			log.info("Fallo la conexión con el FTP");
		}

		try {
			ftpClient.disconnect();
			log.info("Código de respuesta desconexión FTP " + ftpClient.getReplyCode());
			log.info("Mensaje de respuesta desconexión FTP " + ftpClient.getReplyString());
		} catch (IOException ex) {
			log.error("No fue posible la desconexión con el servidor FTP", ex);
		}

		return null;
	}





	public ResponseModel ejecutarFtpAutomatico() {
		log.info("Iniciando el proceso asincrónico de mainframe automáticamente");

		//********** LOGICA PARA OBTENER EL ID DE LOS LOTES, EXTRAER TODA LA DATA DE SUS ARCHIVOS.TXT Y ENVIARLA A TRAVES DE UN ARRAY **********


		// Llamar al método obtenerLotesActivos para obtener los valores
		ResponseModel lotesActivosResponse = obtenerLotesActivos();
		String valores = (String) lotesActivosResponse.getData(); // Obtener el resultado como String

		if (valores == null || valores.isEmpty()) {
			log.error("No se obtuvieron IDs de lotes activos.");
			return null;
		}

		String[] idLotesArray = valores.split(","); // Suponiendo que los IDs están separados por comas
		StringBuilder resultadoFinal = new StringBuilder();

		for (String idLote : idLotesArray) {
			ResponseModel respuestaProcedimiento = obtenerRespuestaDelProcedimiento(idLote.trim());

			if (respuestaProcedimiento.getStatus() == 200) {
				// Obtener la lista de LoteDTO
				List<LoteDTO> resultados = (List<LoteDTO>) respuestaProcedimiento.getData();
				// Convertir la lista de LoteDTO a un String
				for (LoteDTO lote : resultados) {
					// Aquí debes definir cómo quieres convertir cada LoteDTO a String
					resultadoFinal.append(lote.toFormattedString()).append("\n"); // Asegúrate de que LoteDTO tenga un método toString adecuado
				}
			} else {
				log.error("Error al obtener respuesta para el lote: {}", idLote);
			}
		}

		String resultado = resultadoFinal.toString(); // Convertir el StringBuilder a String

		if (resultado.isEmpty()) {
			log.error("No se obtuvieron datos del procedimiento, resultado es null o vacío.");
			return null;
		}

		log.info("Resultado del procedimiento: {}", resultado);



		//********************************************************************************************************************************************


		log.info("Se inicia el proceso de consulta de base de datos para determinar la data de envío");
		log.info("Iniciando proceso");
		FTPClient ftpClient = null;
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();

		// Obtener configuración del host FTP
		datos.setDescriptor("FH");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String host = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración de la contraseña
		datos.setDescriptor("FP");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String password = parametrosPivot.get(0).getValorConfigurado();

		// Obtener configuración del usuario
		datos.setDescriptor("FU");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String usuario = parametrosPivot.get(0).getValorConfigurado();

		log.info("Iniciando consulta de documentos XML en el servidor FTP");
		log.info("Host FTP: \r\n " + host + " \r\n Contraseña de directorio remoto: " + password + " \r\n Usuario: " + usuario);
		log.info("Iniciando conexión");

		ftpClient = FtpUtil.getFTPClient(host, 21, usuario, password);

		if (ftpClient.isConnected()) {
			log.info("Conexión establecida correctamente con el FTP");
			byte[] textoDecomposed = resultado.getBytes(StandardCharsets.UTF_8);
			InputStream is = new ByteArrayInputStream(textoDecomposed);

			try {
				boolean success = ftpClient.storeFile("'VALT.GOFI.BAT1SBAS.GOJT0101.ENTRADA'", is);
				log.info("Código de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.ENTRADA "
						+ ftpClient.getReplyCode() + " El valor de ftpClient es : " + ftpClient);
				log.info("Mensaje de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.ENTRADA "
						+ ftpClient.getReplyString());

				if (success && ftpClient.getReplyCode() == 250) {
					log.info("Escritura correcta en el mainframe");
					byte[] flagDecomposed = "PROCESO COMPLETADO".getBytes(StandardCharsets.UTF_8);
					InputStream flagStream = new ByteArrayInputStream(flagDecomposed);
					success = ftpClient.storeFile("'VALT.GOFI.BAT1SBAS.GOJT0101.FLAG'", flagStream);
					log.info("Código de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.FLAG "
							+ ftpClient.getReplyCode());
					log.info("Mensaje de respuesta FTP al tratar de registrar VALT.GOFI.BAT1SBAS.GOJT0101.FLAG "
							+ ftpClient.getReplyString());

					if (success && ftpClient.getReplyCode() == 250) {
						log.info("Proceso ejecutado correctamente, procediendo a modificar los estados de los lotes y transacciones");
						ArrayList<LoteMainframe> listaLotesEjecutar = this.consultarListaLotesMainframe();
						List<String> idLotes = new ArrayList<>();
						List<String> idTransacciones = new ArrayList<>();

						for (LoteMainframe guardarLoteDTO : listaLotesEjecutar) {
							idLotes.add(guardarLoteDTO.getIdlote());
						}

						boolean loteOk = this.cambiarEstatusLoteMasivo("L", idLotes);
						if (loteOk) {
							boolean transaccionOk = this.cambiarEstatusTransaccionMasivo("L", idTransacciones);
							if (transaccionOk) {
								log.info("Estados de lotes y transacciones actualizados correctamente.");
							} else {
								log.error("Error al actualizar los estados de las transacciones.");
							}
						} else {
							log.error("Error al actualizar los estados de los lotes.");
						}
					} else {
						log.error("Falla al escribir flag en el mainframe");
						log.error("Código de respuesta FTP " + ftpClient.getReplyCode());
						log.error("Mensaje de respuesta FTP " + ftpClient.getReplyString());
					}
				} else {
					log.error("Falla al escribir data en el mainframe");
					log.error("Código de respuesta FTP " + ftpClient.getReplyCode());
					log.error("Mensaje de respuesta FTP " + ftpClient.getReplyString());
				}
			} catch (Exception e) {
				log.error("No fue posible ejecutar el proceso asincrónico", e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					log.error("Error al cerrar el InputStream", e);
				}
			}
		} else {
			log.info("Fallo la conexión con el FTP");
		}

		try {
			ftpClient.disconnect();
			log.info("Código de respuesta desconexión FTP " + ftpClient.getReplyCode());
			log.info("Mensaje de respuesta desconexión FTP " + ftpClient.getReplyString());
		} catch (IOException ex) {
			log.error("No fue posible la desconexión con el servidor FTP", ex);
		}

		return null;
	}






	private String agregarCeros(int length, int cantidadDigitos, String numeroProgramable, String CaracterAgregar) {
		String ceros = "";
		Integer recorrer = cantidadDigitos - length;
		for (int i = 0; i < recorrer; i++) {
			ceros = ceros + CaracterAgregar;
		}
		ceros = ceros + numeroProgramable.toString();
		return ceros;
	}

	public boolean almacenarArchivoEntrada(String archivoFrontEnd, String nombrearchivo) {
		log.info("iniciando proceso de registrado de archivos.");
		try {
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
			datos = new ConsultarConfiguarcionDTO();
			datos.setDescriptor("RESPINTTXT");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String absoluteFilePath = parametrosPivot.get(0).getValorConfigurado() + "/" + nombrearchivo;
			log.info("Se utilizara la ruta de: " + parametrosPivot.get(0).getValorConfigurado());
			log.info("se guardara el archivo: " + absoluteFilePath);
			File file = new File(absoluteFilePath);
			if (file.createNewFile()) {
				log.info(absoluteFilePath + " File Created");
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(archivoFrontEnd);
				bw.flush();
			} else {
				log.info("archivo " + absoluteFilePath + " ya existe, no es posible generarlo. ");
				return false;
			}
		} catch (Exception e) {
			log.error("no fue posible generar el archivo para resguardar la data.", e);
			return false;
		}
		return true;
	}

	public boolean moverArchivos() {
		log.info("Inicio de proceso de mover los documentos de directorio");
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
		datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("RESPINTTXT");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String absoluteFilePath = parametrosPivot.get(0).getValorConfigurado();
		log.info("Obtendremos los archivos de la ruta: " + absoluteFilePath);
		File ruta = new File(absoluteFilePath);
		datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("RESPDATTXT");
		parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String respaldoPath = parametrosPivot.get(0).getValorConfigurado();
		log.info("los archivos seran respaldados en la ruta: " + respaldoPath);
		String[] listado = ruta.list();
		File[] listadoFich = ruta.listFiles();
		for (final File f : ruta.listFiles()) {
			if (f.isFile()) {
				File to = new File(respaldoPath + "/" + f.getName());
				log.info("moviendo el archivo:" + absoluteFilePath + "/" + f.getName());
				log.info("a la ruta: " + respaldoPath + "/" + f.getName());
				try {
					moveFile(f, to);
					log.info("archivo movido con exito");
				} catch (IOException ex) {
					log.info("no fue posible mover el archivo propuesto", ex);
				}
			}
		}
		log.info("fin de proceso de mover los documentos de directorio");
		log.info("Inicio del proceso de respaldado de fatos");
		this.respaldarDirectorio();
		log.info("Fin del proceso de respaldado de fatos");
		return false;
	}

	public static void moveFile(File src, File dest) throws IOException {
		Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	public boolean respaldarDirectorio() {
		log.info("Inicio del proceso de compresion de archivos.");
		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
		datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("RESPDATTXT");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
		String respaldoPath = parametrosPivot.get(0).getValorConfigurado();
		log.info("ruta de donde se obtendran los arvhivos a procesar: " + respaldoPath);
		File ruta = new File(respaldoPath);
		String[] listado = ruta.list();
		log.info("arvhivos a comprimir: ");
		for (String file : listado) {
			log.info("Archivo:  " + file);
		}
		try {
			datos = new ConsultarConfiguarcionDTO();
			datos.setDescriptor("RESPZIP");
			parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String respaldoZip = parametrosPivot.get(0).getValorConfigurado();
			String dateTime = DateTimeFormatter.ofPattern("MMM_dd_yyyy_hh_mm_ss_a").format(LocalDateTime.now());
			log.info("zip de respaldo: " + respaldoZip + "/respaldo_" + dateTime + ".zip");
			String dest = respaldoZip + "/respaldo_" + dateTime + ".zip";
			zipFilesList(listado, dest);
		} catch (Exception e) {
			log.info("no fue posible generar el archivo.zip en la ruta solicitada ", e);
		}
		log.info("Fin del proceso de respaldado de data.");
		return false;
	}

	private void zipFilesList(String[] listado, String dest) {
		try {
			ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
			datos = new ConsultarConfiguarcionDTO();
			datos.setDescriptor("RESPDATTXT");
			List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();
			String respaldoPath = parametrosPivot.get(0).getValorConfigurado();
			FileOutputStream fos = new FileOutputStream(dest);
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			for (String sourceFile : listado) {
				log.info("resguardando el archivo: " + respaldoPath + "/" + sourceFile);
				File fileToZip = new File(respaldoPath + "/" + sourceFile);
				FileInputStream fis = new FileInputStream(fileToZip);
				ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
				zipOut.putNextEntry(zipEntry);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				fis.close();
				ZipInputStream zis = new ZipInputStream(new FileInputStream(dest));
				ZipEntry ze = zis.getNextEntry();
				log.info("Se procedera a eliminar el archivo: " + respaldoPath + "/" + sourceFile+ " que fue resguardado previamente. ");
				if (fileToZip.delete()) {
					log.info("Archivo destruido con exito. ");
				} else {
					log.info("No fue posible destruir el archivo solicitado. ");
				}
			}
			zipOut.close();
			fos.close();
		} catch (Exception e) {
			log.error("No fue posible resguardar los archivos solicitados", e);
		}
	}

	public boolean cifrarDecifrar() {
		try {
			log.info("inicio del proceso de cifrado de data");
			System.out.println("clave de cifrado: "+ SecretKeyData);
			CifradoData cifradoData = new CifradoData();
			String dataAEncriptar="El texto a encriptar";
			System.out.println("lo que vamos a encriptar es: "+ dataAEncriptar);
			String data =cifradoData.encript(dataAEncriptar,SecretKeyData);
			System.out.println("encriptar "+data);
			System.out.println("desencriptar "+cifradoData.decrypt(data,SecretKeyData));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


}