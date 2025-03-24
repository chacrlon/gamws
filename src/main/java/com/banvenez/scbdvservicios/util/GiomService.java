package com.banvenez.scbdvservicios.util;

import com.banvenez.scbdvservicios.dao.GiomDao;
import com.banvenez.scbdvservicios.dto.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.PredicateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.nio.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class GiomService {
	@Autowired
	GiomDao giomDao;
	
/* ANTERIORMENTE ESTABA ESTE METODO         LEER   
	public ResponseModel leer(String file, String id_lote, String nombrearchivo) {
		ResponseModel responseModel = new ResponseModel();

		try {

			byte[] bytes = Base64.getDecoder().decode(file.replaceAll("data:text/plain;base64,", ""));
			log.info("leerArchivo en el Service => {}", bytes);

			String base64Decodificado = new String(bytes, StandardCharsets.UTF_8);
			log.info("leerArchivo  en el Service => {}", base64Decodificado);

			String[] datos = base64Decodificado.split("\n");

			Map<String, Boolean> mapaErrores = new HashMap<String, Boolean>();
			mapaErrores.put("TipoMovimiento", false);
			List<CargaGiomDTO> lalista = new ArrayList<CargaGiomDTO>();
			String escribirTextoPlano = "";
			for (int i = 0; i < datos.length; i++) {
				CargaGiomDTO archivo = new CargaGiomDTO();

				if (this.separadorValidador(datos[i], 38, 39).equals("D")) {

					mapaErrores.put("TipoMovimiento", true);
					break;
				}
				escribirTextoPlano = escribirTextoPlano + datos[i] + "\n";
				archivo.setNumeroCuenta(this.separadorValidador(datos[i], 0, 20));
				archivo.setVef(this.separadorValidador(datos[i], 20, 23));
				archivo.setMontoTransaccion(this.separadorValidador(datos[i], 23, 38));
				archivo.setTipoMovimiento(this.separadorValidador(datos[i], 38, 39));
				archivo.setSerialOperacion(this.separadorValidador(datos[i], 39, 44));
				archivo.setReferencia(this.separadorValidador(datos[i], 44, 52));
				archivo.setCodigoOperacion(this.separadorValidador(datos[i], 52, 56));
				archivo.setReferencia2(this.separadorValidador(datos[i], 56, 76));
				archivo.setTipoDocumento(this.separadorValidador(datos[i], 76, 77));
				archivo.setNumeroCedula(this.separadorValidador(datos[i], 77, 89));
				archivo.setId_lote(id_lote);

				lalista.add(archivo);

			}

			if (!mapaErrores.get("TipoMovimiento")) {
				log.info("leerArchivo  en el Service interno => {}", lalista.size());

				responseModel = giomDao.cargarArchivo(lalista);

				responseModel = giomDao.guardarnombreArchivo(nombrearchivo, id_lote);

				if (giomDao.almacenarArchivoEntrada(escribirTextoPlano, nombrearchivo + "_" + id_lote)) {
					log.info("Fue posible registrar en disco duro el archivo enviado por el front");
				} else {
					log.info("no posible registrar en disco duro el archivo enviado por el front");
				}

				try {

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("se esta ingresando un movimiento que no pinta en la aplicacion ");
				responseModel.setCode(9999);
				responseModel.setMessage("Se ingreso una cuenta con tipo de movimiento invalido");
				responseModel.setStatus(500);
				responseModel.setData(null);
			}

		} catch (Exception e) {

			log.error(e.getMessage(), e);
			responseModel.setCode(9999);
			responseModel.setMessage("ERROR al leer el archivo  Exception");
			responseModel.setStatus(500);
			responseModel.setData(null);

		}

		return responseModel;
	}*/
	
	/*
	 SEGUNDA MODIFICACION
	public ResponseModel leer(String file, String id_lote, String nombrearchivo) {  
	    ResponseModel responseModel = new ResponseModel();  

	    try {  
	        // Decodificar el archivo Base64  
	        byte[] bytes = Base64.getDecoder().decode(file.replaceAll("data:text/plain;base64,", ""));  
	        log.info("leerArchivo en el Service => {}", bytes);  

	        String base64Decodificado = new String(bytes, StandardCharsets.UTF_8);  
	        log.info("leerArchivo  en el Service => {}", base64Decodificado);  

	        String[] datos = base64Decodificado.split("\n");  

	        Map<String, Boolean> mapaErrores = new HashMap<>();  
	        mapaErrores.put("TipoMovimiento", false);  
	        List<CargaGiomDTO> lalista = new ArrayList<>();  
	        String escribirTextoPlano = "";  

	        for (int i = 0; i < datos.length; i++) {  
	            CargaGiomDTO archivo = new CargaGiomDTO();  

	            // Obtener el tipo de movimiento  
	            String tipoMovimiento = this.separadorValidador(datos[i], 38, 39);  
	            log.info("Tipo de movimiento para la línea {}: {}", i, tipoMovimiento);  

	            // Validar el tipo de movimiento  
	            if (tipoMovimiento.equals("D")) {  
	                mapaErrores.put("TipoMovimiento", true);  
	                break; // Salir del bucle si se encuentra un tipo de movimiento inválido  
	            }  

	            // Construir el objeto CargaGiomDTO  
	            escribirTextoPlano += datos[i] + "\n";  
	            archivo.setNumeroCuenta(this.separadorValidador(datos[i], 0, 20));  
	            archivo.setVef(this.separadorValidador(datos[i], 20, 23));  
	            archivo.setMontoTransaccion(this.separadorValidador(datos[i], 23, 38));  
	            archivo.setTipoMovimiento(tipoMovimiento); // Usar el tipo de movimiento obtenido  
	            archivo.setSerialOperacion(this.separadorValidador(datos[i], 39, 44));  
	            archivo.setReferencia(this.separadorValidador(datos[i], 44, 52));  
	            archivo.setCodigoOperacion(this.separadorValidador(datos[i], 52, 56));  
	            archivo.setReferencia2(this.separadorValidador(datos[i], 56, 76));  
	            archivo.setTipoDocumento(this.separadorValidador(datos[i], 76, 77));  
	            archivo.setNumeroCedula(this.separadorValidador(datos[i], 77, 89));  
	            archivo.setId_lote(id_lote);  

	            lalista.add(archivo);  
	        }  

	        // Verificar si hay errores en el tipo de movimiento  
	        if (!mapaErrores.get("TipoMovimiento")) {  
	            log.info("leerArchivo  en el Service interno => {}", lalista.size());  

	            responseModel = giomDao.cargarArchivo(lalista);  
	            responseModel = giomDao.guardarnombreArchivo(nombrearchivo, id_lote);  

	            if (giomDao.almacenarArchivoEntrada(escribirTextoPlano, nombrearchivo + "_" + id_lote)) {  
	                log.info("Fue posible registrar en disco duro el archivo enviado por el front");  
	            } else {  
	                log.info("No fue posible registrar en disco duro el archivo enviado por el front");  
	            }  
	        } else {  
	            log.warn("Se está ingresando un movimiento que no pinta en la aplicación");  
	            responseModel.setCode(9999);  
	            responseModel.setMessage("Se ingresó una cuenta con tipo de movimiento inválido");  
	            responseModel.setStatus(500);  
	            responseModel.setData(null);  
	        }  

	    } catch (Exception e) {  
	        log.error("Error al leer el archivo: {}", e.getMessage(), e);  
	        responseModel.setCode(9999);  
	        responseModel.setMessage("ERROR al leer el archivo Exception");  
	        responseModel.setStatus(500);  
	        responseModel.setData(null);  
	    }  

	    return responseModel;  
	}

	private String separadorValidador(String dato, int i, int i1) {
		return dato.substring(i, i1);

	}
*/
	public ResponseModel leer(String file, String id_lote, String nombrearchivo) {  
	    ResponseModel responseModel = new ResponseModel();  

	    // Primera validación con procedimiento
	    ResponseModel validacion = giomDao.validarNombreArchivo(nombrearchivo);
	    if (validacion.getCode() == 1001) {
	        return validacion; // Retorna error directamente
	    }
	    
	    try {  
	        // Decodificar el archivo Base64  
	        byte[] bytes = Base64.getDecoder().decode(file.replaceAll("data:text/plain;base64,", ""));  
	        log.info("leerArchivo en el Service => {}", bytes);  

	        String base64Decodificado = new String(bytes, StandardCharsets.UTF_8);  
	        log.info("leerArchivo  en el Service => {}", base64Decodificado);  

	        String[] datos = base64Decodificado.split("\n");  

	        Map<String, Boolean> mapaErrores = new HashMap<>();  
	        mapaErrores.put("TipoMovimiento", false);  
	        List<CargaGiomDTO> lalista = new ArrayList<>();  
	        String escribirTextoPlano = "";  

	        for (int i = 0; i < datos.length; i++) {  
	            CargaGiomDTO archivo = new CargaGiomDTO();  

	            // Obtener el tipo de movimiento  
	            String tipoMovimiento = this.separadorValidador(datos[i], 38, 39);  
	            log.info("Tipo de movimiento para la línea {}: {}", i, tipoMovimiento);  

	            // Validar el tipo de movimiento  
	            if (!tipoMovimiento.equals("D")) {  // Cambiar la condición aquí  
	                mapaErrores.put("TipoMovimiento", true);  
	                break; // Salir del bucle si se encuentra un tipo de movimiento inválido  
	            }  

	            // Construir el objeto CargaGiomDTO  
	            escribirTextoPlano += datos[i] + "\n";  
	            archivo.setNumeroCuenta(this.separadorValidador(datos[i], 0, 20));  
	            archivo.setVef(this.separadorValidador(datos[i], 20, 23));  
	            archivo.setMontoTransaccion(this.separadorValidador(datos[i], 23, 38));  
	            archivo.setTipoMovimiento(tipoMovimiento); // Usar el tipo de movimiento obtenido  
	            archivo.setSerialOperacion(this.separadorValidador(datos[i], 39, 44));  
	            archivo.setReferencia(this.separadorValidador(datos[i], 44, 52));  
	            archivo.setCodigoOperacion(this.separadorValidador(datos[i], 52, 56));  
	            archivo.setReferencia2(this.separadorValidador(datos[i], 56, 76));  
	            archivo.setTipoDocumento(this.separadorValidador(datos[i], 76, 77));  
	            archivo.setNumeroCedula(this.separadorValidador(datos[i], 77, 89));  
	            archivo.setId_lote(id_lote);  

	            lalista.add(archivo);  
	        }  

	        // Verificar si hay errores en el tipo de movimiento  
	        if (!mapaErrores.get("TipoMovimiento")) {  
	            log.info("leerArchivo  en el Service interno => {}", lalista.size());  

	            responseModel = giomDao.cargarArchivo(lalista);  
	            responseModel = giomDao.guardarnombreArchivo(nombrearchivo, id_lote);  

	            if (giomDao.almacenarArchivoEntrada(escribirTextoPlano, nombrearchivo + "_" + id_lote)) {  
	                log.info("Fue posible registrar en disco duro el archivo enviado por el front");  
	            } else {  
	                log.info("No fue posible registrar en disco duro el archivo enviado por el front");  
	            }  
	        } else {  
	            log.warn("Se está ingresando un movimiento que no pinta en la aplicación");  
	            responseModel.setCode(9999);  
	            responseModel.setMessage("Se ingresó una cuenta con tipo de movimiento inválido");  
	            responseModel.setStatus(500);  
	            responseModel.setData(null);  
	        }  

	    } catch (Exception e) {  
	        log.error("Error al leer el archivo: {}", e.getMessage(), e);  
	        responseModel.setCode(9999);  
	        responseModel.setMessage("ERROR al leer el archivo Exception");  
	        responseModel.setStatus(500);  
	        responseModel.setData(null);  
	    }  

	    return responseModel;  
	}  

	private String separadorValidador(String dato, int i, int i1) {  
	    return dato.substring(i, i1);  
	}

	public ResponseModel consultarunidades() {
		log.info("Begin consultarunidades en el Service");

		String url = "http://180.183.174.37:7010/int-servicios/sgu/consultaUnidades";
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<ResponseConsultaUnidades> response = restTemplate.postForEntity(url, null, ResponseConsultaUnidades.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("Consulta exitosa: " + response.getBody());

				// Rellenar el ResponseModel
				ResponseConsultaUnidades respuestaConsulta = response.getBody();
				ResponseModel responseModel = new ResponseModel();
				// Suponiendo que 'code', 'status' y 'id' son campos que puedes obtener o establecer en función de la aplicacion
				responseModel.setCode(200); // O el código que consideres
				responseModel.setMessage(respuestaConsulta.getMensaje());
				responseModel.setData(respuestaConsulta.getData());
				responseModel.setStatus(1); // O el estado que consideres
				responseModel.setId(null); // Asigna el ID si es necesario

				return responseModel;
			} else {
				log.error("Error al consumir el servicio: " + response.getStatusCode());
				return new ResponseModel();
			}
		} catch (HttpClientErrorException e) {
			log.error("Error del cliente: " + e.getMessage());
			return new ResponseModel();
		} catch (RestClientException e) {
			log.error("Error en la solicitud: " + e.getMessage());
			return new ResponseModel();
		}
	}


	public ResponseModel guardarlote(GuardarLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin guardarlote en el Service=>{}", datos.toString());
		if ((datos.getUnidad() != "" && datos.getUnidad() != null)
				&& (datos.getFechaInicio() != "" && datos.getFechaInicio() != null)
				&& (datos.getFechaFin() != null && datos.getFechaFin() != null)) {
			responseModel = giomDao.guardarLote(datos);
		} else {
			log.info(" guardarlote Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}



	public ResponseModel guardarauditoria(AuditoriasDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin guardarauditoria en el Service =>{}", datos.toString());
		if ((datos.getAccion() != "" && datos.getAccion() != null)
				&& (datos.getIdregistroauditoria() != 0 && datos.getIdregistroauditoria() != null)
				&& (datos.getUsuario() != "" && datos.getUsuario() != null)

		) {
			boolean resp = giomDao.guardarauditoriaDinamico(datos);

			responseModel.setCode(1000);
			responseModel.setStatus(200);
			responseModel.setCode(1000);
			responseModel.setMessage("auditoria guardada con exito");
		} else {
			log.info(" guardarauditoria Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel consultarlista() {
		log.info("Begin consultarlista en el Service");

		return giomDao.consultarlista();
	}

	public ResponseModel consultarrangolote(RangoFechaDTO datos) {
		log.info("Begin consultarrangolote en el Service=>{}", datos);

		return giomDao.consultarrangolote(datos);
	}

	public ResponseModel consultarrangoauditoria(FechasAuditoriasDTO datos) {
		log.info("Begin consultarrangoauditoria en el Service=>{}", datos);

		return giomDao.consultarrangoauditoria(datos);
	}

	public ResponseModel consultarrangotransacciones(ConsultarRangotransaccionesDTO datos) {
		log.info("Begin consultarrangotransacciones en el Service =>{}", datos);

		return giomDao.consultarrangotransacciones(datos);
	}

	public ResponseModel consultartransacciones() {
		log.info("Begin consultartransacciones en el Service");

		return giomDao.consultartransacciones();
	}

	public ResponseModel consultar_aprobacion() {
		log.info("Begin consultar_aprobacion en el Service");

		return giomDao.consultar_aprobacion();
	}

	public ResponseModel consuitarlistaregistros() {
		log.info("Begin consuitarlistaregistros en el Service");

		return giomDao.consultarlistaregistros();
	}

	public ResponseModel consuitarlistaHora() {
		log.info("Begin consuitarlistaHora en el Service");

		return giomDao.consultarlistaHora();
	}


	public ResponseModel lotellefecha(GuardarLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin detalle en el Service =>{}", datos.toString());
		if ((datos.getIdlote() != "" && datos.getIdlote() != null)) {
			responseModel = giomDao.detalleslote(datos);
		} else {
			log.info(" lotellefecha Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel lote_monto(GuardarLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin lote_monto en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != "" && datos.getIdlote() != null)) {
			responseModel = giomDao.lote_monto(datos);
		} else {
			log.info(" lote_monto Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel lote_monto2(GuardarLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin lote_monto2 en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != "" && datos.getIdlote() != null)) {
			responseModel = giomDao.lote_monto2(datos);
		} else {
			log.info(" lote_monto2 Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}


	public ResponseModel aprobacion(GuardarLoteDTO datos, String ip) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin aprobacion en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != "" && datos.getIdlote() != null)) {
			responseModel = giomDao.aprobacion(datos, ip);
		} else {
			log.info(" aprobacion Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel modificarlote(GuardarLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin modificarlote en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != "" && datos.getIdlote() != null)
				&& (datos.getFechaInicio() != "" && datos.getFechaInicio() != null)
				&& (datos.getFechaFin() != "" && datos.getFechaFin() != null)) {
			responseModel = giomDao.modificardatoslote(datos);
		} else {
			log.info(" modificarlote Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel modificarestadoslote(EstadosLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin modificarestadoslote en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != null) && (datos.getNumero() != "" && datos.getNumero() != null)
		// (datos.getEstado()!="" && datos.getEstado()!= null )
		) {
			responseModel = giomDao.modificardatosestado(datos);
		} else {
			log.info(" modificarestadoslote Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}
	
	
	public ResponseModel modificarestadosloteReprocesado(EstadosLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin modificarestadoslote en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != null) && (datos.getNumero() != "" && datos.getNumero() != null)
		// (datos.getEstado()!="" && datos.getEstado()!= null )
		) {
			responseModel = giomDao.modificardatosestadoReprocesado(datos);
		} else {
			log.info(" modificarestadoslote Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel eliminacion(GuardarLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		log.info("Begin aprobacion en el Service=>{}", datos.toString());
		if ((datos.getIdlote() != "" && datos.getIdlote() != null)) {
			responseModel = giomDao.eliminacion(datos);
		} else {
			log.info(" aprobacion Datos no validos o en null");
			responseModel.setCode(9999);
			responseModel.setStatus(204);
			responseModel.setMessage("Datos no validos o en null");
		}
		return responseModel;
	}

	public ResponseModel consultarConfiguracion(ConsultarConfiguarcionDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.consultarConfiguracion(datos);
		return responseModel;
	}

	public ResponseModel cambioEstadoConfiguracion(ConsultarConfiguarcionDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.cambioEstadoConfiguracion(datos);
		return responseModel;
	}

	public ResponseModel consultarSeguimiento(EstadosLoteDTO datos) {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.consultarSeguimiento(datos);
		return responseModel;
	}

	public ResponseModel ejecutarRecepcion() {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.ejecutarRecepcion();
		return responseModel;
	}

	public ResponseModel actualizarEstadoRegistro(String idLotes, String estadoRegistro) {
		log.info("Actualizando estado del registro para ID(s): {}", idLotes);
		return giomDao.actualizarEstadoRegistro(idLotes, estadoRegistro); // Llama al método del DAO
	}

	public ResponseModel ejecutarFtp() {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.ejecutarFtp();
		return responseModel;
	}
	
	public ResponseModel ejecutarFtpAutomatico() {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.ejecutarFtpAutomatico();
		return responseModel;
	}

	public ResponseModel ejecutarFtpAutomatico2() {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.ejecutarFtpAutomatico2();
		return responseModel;
	}
	
	
	public ResponseModel listarArchivosEnDirectorio() {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.listarArchivosEnDirectorio();
		return responseModel;
	}
	
	
	public ResponseModel leerArchivoDesdeFTP() {
		ResponseModel responseModel = new ResponseModel();
		responseModel = giomDao.leerArchivoDesdeFTP();
		return responseModel;
	}
	
	public ResponseModel comprimirArchivoLocal() {  
	    ResponseModel responseModel = new ResponseModel(); 
	    responseModel = giomDao.comprimirArchivoLocal(); 
	    return responseModel; 
	}
	
	public ResponseModel moverArchivoEnFTP() {  
        ResponseModel responseModel = new ResponseModel();  
        // Llama al método del DAO que mueve el archivo en FTP  
        responseModel = giomDao.moverArchivoEnFTP();   
        return responseModel; // Devuelve la respuesta del DAO  
    } 

	
	
	 public ResponseModel obtenerRespuestaDelProcedimiento(String idLotes) {  
	        log.info("Begin consultar en el Service");  
	        return giomDao.obtenerRespuestaDelProcedimiento(idLotes);  
	    }
	
	public ResponseModel moverArchivos() {
		log.info("Inicio del proceso de ejecucion de movimiento de archivos ");

		boolean moverArchivos = giomDao.moverArchivos();

		log.info("fin del proceso de ejecucion de movimiento de archivos ");
		return null;
	}

//	public ResponseModel respaldarArchivos() {
//
//		boolean respaldarArchivos = giomDao.respaldarDirectorio();
//
//		log.info("fin del proceso de ejecucion de respaldado de archivos ");
//		return null;
//	}

	public ResponseModel cifrar() {


		boolean respaldarArchivos = giomDao.cifrarDecifrar();

		log.info("fin del proceso de cifrado de data ");
		return null;
	}

}
