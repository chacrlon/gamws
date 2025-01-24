package com.banvenez.scbdvservicios.action;

import com.banvenez.scbdvservicios.dto.*;
import com.banvenez.scbdvservicios.util.GiomService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

@RestController
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST })
@RequestMapping(value = "/apiGiom", headers = "Accept=application/json", method = { RequestMethod.POST,
		RequestMethod.OPTIONS, RequestMethod.GET })
public class GiomAction {

	@Autowired
	GiomService giomService;


	@RequestMapping(value = "/leerArchivo-Gion", method = RequestMethod.POST)
	public ResponseModel leerArchivoGiom(@RequestBody ObjetoElquetal objetoElquetal

	) {
		log.info("leerArchivo => {}", objetoElquetal.toString());
		return giomService.leer(objetoElquetal.getFile(), objetoElquetal.getIdlote(),
				objetoElquetal.getNombrearchivo());
	}

	@PostMapping(value = "/guardar-datos-lote")
	public ResponseModel guardarlote(@RequestBody GuardarLoteDTO datos) {

		log.info("guardarlote => {}", datos.toString());

		return giomService.guardarlote(datos);

	}

	@PostMapping(value = "/lote-detalle")
	public ResponseModel lotedetalle(@RequestBody GuardarLoteDTO datos) {

		log.info("lotedetalle => {}", datos.toString());

		return giomService.lotellefecha(datos);

	}

	@PostMapping(value = "/lote-monto-recuperado")
	public ResponseModel lote_monto(@RequestBody GuardarLoteDTO datos) {

		log.info("lote_monto => {}", datos.toString());

		return giomService.lote_monto(datos);

	}

	@PostMapping(value = "/lote-monto-no-recuperado")
	public ResponseModel lote_monto2(@RequestBody GuardarLoteDTO datos) {

		log.info("lote_monto2 => {}", datos.toString());

		return giomService.lote_monto2(datos);

	}

	@PostMapping(value = "/modificar-datos-lote")
	public ResponseModel modificarlote(@RequestBody GuardarLoteDTO datos) {

		log.info("modificarlote => {}", datos.toString());

		return giomService.modificarlote(datos);

	}

	@PostMapping(value = "/modificar-estados-lote")
	public ResponseModel modificarestadoslote(@RequestBody EstadosLoteDTO datos, HttpServletRequest request) {

		datos.setIp(request.getRemoteAddr());
		log.info("modificarestadoslote => {}", datos.toString());

		return giomService.modificarestadoslote(datos);

	}


	@PostMapping(value = "/guardar-auditoria")
	public ResponseModel auditoriahora(@RequestBody AuditoriasDTO datos) {

		log.info("auditoriahora => {}", datos.toString());

		return giomService.guardarauditoria(datos);

	}

	@PostMapping(value = "/consultar-hora-ejecucion")
	public ResponseModel consultarHora() {
		log.info("consultarHora => {}");
		return giomService.consuitarlistaHora();

	}

	@PostMapping(value = "/consultar-registros-lote")
	public ResponseModel consultarregistroslote() {
		log.info("consultarregistroslote => {}");
		return giomService.consuitarlistaregistros();

	}


	@PostMapping(value = "/consultar-lote-lista")
	public ResponseModel consultarLote() {
		log.info("consultarLote => {}");
		return giomService.consultarlista();

	}

	@PostMapping(value = "/consultar-rango-fecha-lote")
	public ResponseModel rangoFechalote(@RequestBody RangoFechaDTO datos) {
		log.info("rangoFechalote => {}", datos.toString());
		return giomService.consultarrangolote(datos);

	}

	@PostMapping(value = "/consultar-rango-fecha-auditoria")
	public ResponseModel rangoFechaauditoria(@RequestBody FechasAuditoriasDTO datos) {
		log.info("rangoFechaauditoria => {}", datos.toString());
		return giomService.consultarrangoauditoria(datos);

	}

	@PostMapping(value = "/consultar-rango-fecha-transacciones")
	public ResponseModel rangoFechatransacciones(@RequestBody ConsultarRangotransaccionesDTO datos) {
		log.info("rangoFechatransacciones => {}", datos.toString());
		return giomService.consultarrangotransacciones(datos);

	}

	@RequestMapping("descargar-text")
	public void descargarLog(HttpServletResponse response,
			@RequestParam(value = "fechaInicio", required = false) String fechaInicio,
			@RequestParam(value = "fechaFinal", required = false) String fechaFinal) throws Exception {

		log.info("Iniciando en la clase y metodo " + GiomAction.class.getName() + " descargar Log");


		ConsultarRangotransaccionesDTO datos = new ConsultarRangotransaccionesDTO();

		datos.setFechai(fechaInicio);
		datos.setFechaf(fechaFinal);
		ResponseModel returnData = giomService.consultarrangotransacciones(datos);

		System.out.println("la salida es:: " + returnData);

		System.out.println("Veamos que parsear " + returnData.getData());

		ConsultaRangoFechasDTO listaDatosProcesar = (ConsultaRangoFechasDTO) returnData.getData();

		// la transformamos a un string para imprimir
		String dataTxtOut = "";

		for (CargaGiomDTO datosString : listaDatosProcesar.getData()) {
			log.info("se esta procesando el dato de: " + datosString.toString());
			dataTxtOut =

					dataTxtOut + datosString.getNumeroCuenta() + datosString.getVef()
							+ this.agregarCeros(datosString.getMontoTransaccion().toString().length(), 15,
									datosString.getMontoTransaccion().toString(), "0")

							+ datosString.getTipoMovimiento() + datosString.getSerialOperacion() + this.agregarCeros(
									datosString.getReferencia().length(), 12, datosString.getReferencia(), "0")

							+ this.agregarCeros(datosString.getCodigoOperacion().length(), 4,
									datosString.getCodigoOperacion(), "0")
							+ datosString.getReferencia2() + datosString.getTipoDocumento()

							+ datosString.getNumeroCedula() + "\r\n";

		}

		log.info("mandamos a la salida " + dataTxtOut);

		try {
			byte[] textoDecomposed = (dataTxtOut).getBytes(StandardCharsets.UTF_8);

			InputStream is = new ByteArrayInputStream(textoDecomposed);

			response.setContentType("text/plain");
			response.addHeader("Content-disposition", "attachment:fileName=" + "dataImpresa.txt");

			OutputStream os = response.getOutputStream();
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@PostMapping(value = "/consultar-transacciones-lista")
	public ResponseModel consultartransacciones() {
		log.info("consultartransacciones => {}");
		return giomService.consultartransacciones();

	}

	@PostMapping(value = "/consultar-lote-aprobacion")
	public ResponseModel consultar_aprobacion() {
		log.info("consultar_aprobacion => {}");
		return giomService.consultar_aprobacion();

	}

	@PostMapping(value = "/aprobacion-lote")
	public ResponseModel aprobacion(@RequestBody GuardarLoteDTO datos, HttpServletRequest request) {

		log.info("aprobacion => {}", datos.toString());

		return giomService.aprobacion(datos, request.getRemoteAddr());

	}

	@PostMapping(value = "/consultar-unidades")
	public ResponseModel consultarunidades() {
		log.info("consultarunidades => {}");
		return giomService.consultarunidades();

	}

	@PostMapping(value = "/eliminar-lote")
	public ResponseModel eliminarLote(@RequestBody GuardarLoteDTO datos) {

		log.info("Eliminacion => {}", datos.toString());

		return giomService.eliminacion(datos);

	}

	@PostMapping(value = "/consultar-configuracion")
	public ResponseModel consultarConfiguracion(@RequestBody ConsultarConfiguarcionDTO datos) {

		log.info("Consulta => {}", datos.toString());

		return giomService.consultarConfiguracion(datos);

	}

	@PostMapping(value = "/cambio-estado-configuracion")
	public ResponseModel cambioEstadoConfiguracion(@RequestBody ConsultarConfiguarcionDTO datos) {

		log.info("cambioEstadoConfiguracion => {}", datos.toString());

		return giomService.cambioEstadoConfiguracion(datos);

	}

	@PostMapping(value = "/consultar-seguimiento")
	public ResponseModel consultarSeguimiento(@RequestBody EstadosLoteDTO datos) {
		log.info("consultarunidades => {}");
		return giomService.consultarSeguimiento(datos);

	}

	@PostMapping(value = "/actualizarEstadoRegistro")
	public ResponseModel actualizarEstadoRegistro(@RequestBody String idLotes, @RequestParam String estadoRegistro) {
		log.info("Actualizando el estado del registro para ID(s): {}", idLotes);
		return giomService.actualizarEstadoRegistro(idLotes, estadoRegistro); // Llama al método del servicio
	}

	@PostMapping(value = "/ejecutarFtp")
	public ResponseModel ejecutarFtp() {
		log.info("consultarunidades => {}");
		return giomService.ejecutarFtp();

	}
	
	@PostMapping(value = "/ejecutarFtpAutomatico")
	public ResponseModel ejecutarFtpAutomatico() {
		log.info("consultarunidades => {}");
		return giomService.ejecutarFtpAutomatico();

	}

	@PostMapping(value = "/ejecutarFtpAutomatico2")
	public ResponseModel ejecutarFtpAutomatico2() {
		log.info("consultarunidades => {}");
		return giomService.ejecutarFtpAutomatico2();

	}


	@GetMapping(value = "/datosPorLote/{idLotes}")  
    public ResponseModel obtenerRespuestaDelProcedimiento(@PathVariable String idLotes) {  
		log.info("Ejecutando consulta de datos por lote: {}", idLotes);
        return giomService.obtenerRespuestaDelProcedimiento(idLotes);  
    }
	
	@PostMapping(value = "/ejecutarRecepcion")
	public ResponseModel ejecutarRecepcion() {
		log.info("consultarunidades => {}");
		return giomService.ejecutarRecepcion();

	}
	
	
	@PostMapping(value = "/listarArchivosEnDirectorio")  
    public ResponseModel listarArchivosEnDirectorio() {  
        log.info("Llamando a listarArchivosEnDirectorio");  
        ResponseModel response = giomService.listarArchivosEnDirectorio();  
        log.info("Respuesta de listarArchivosEnDirectorio: {}", response);  
        return response;  
    }
	
	
	@PostMapping(value = "/leerArchivoDesdeFTP")  
    public ResponseModel leerArchivoDesdeFTP() {  
        log.info("Llamando a leerArchivoDesdeFTP");  
        ResponseModel response = giomService.leerArchivoDesdeFTP();  
        log.info("Respuesta de leerArchivoDesdeFTP: {}", response);  
        return response;  
    }
	
	
	@PostMapping(value = "/comprimirArchivoLocal")  
	public ResponseModel comprimirArchivoLocal() {  
	    log.info("Llamando a comprimirArchivoLocal con la ruta del archivo: {}" );  
	    ResponseModel response = giomService.comprimirArchivoLocal();  
	    log.info("Respuesta de comprimirArchivoLocal: {}", response);  
	    return response;  
	}
	
	
	@PostMapping(value = "/moverArchivoEnFTP")  
    public ResponseModel moverArchivoEnFTP() {  
        log.info("Llamando a moverArchivoEnFTP");  
        ResponseModel response = giomService.moverArchivoEnFTP();  
        log.info("Respuesta de moverArchivoEnFTP: {}", response);  
        return response;  
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

	@RequestMapping("descargar-log")
	public void descargarLog(HttpServletResponse response) throws Exception {

		log.info("Iniciando en la clase y metodo " + GiomAction.class.getName() + " descargarLog");

		FileInputStream fis = new FileInputStream("/oracle/Middleware/oracle_home/intranet/logs/giom.log");
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment:fileName=" + "scbdv-comedor-servicios.log");

		OutputStream os = response.getOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = fis.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		fis.close();
	}

	@RequestMapping("descargar-flag")
	public void descargarFlag(HttpServletResponse response) throws Exception {

		log.info("Iniciando en la clase y metodo " + GiomAction.class.getName() + " descargar flag mainframe ");

		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
		datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("RUTF");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();

		String rutaServidor = parametrosPivot.get(0).getValorConfigurado();
		log.info("la ruta del archivo a leer es: " + rutaServidor);

		Path file = Paths.get(rutaServidor);

		FileInputStream fis = new FileInputStream(rutaServidor);
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment:fileName=" + "scbdv-comedor-servicios.log");

		OutputStream os = response.getOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = fis.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		fis.close();
	}

	@RequestMapping("descargar-data")
	public void descargarData(HttpServletResponse response) throws Exception {

		ConsultarConfiguarcionDTO datos = new ConsultarConfiguarcionDTO();
		datos = new ConsultarConfiguarcionDTO();
		datos.setDescriptor("RUT");
		List<ParametrosDTO> parametrosPivot = (List<ParametrosDTO>) this.consultarConfiguracion(datos).getData();

		String rutaServidor = parametrosPivot.get(0).getValorConfigurado();
		log.info("la ruta del archivo a leer es: " + rutaServidor);

		log.info("Iniciando en la clase y metodo " + GiomAction.class.getName() + " descargar archivos mainframe ");

		FileInputStream fis = new FileInputStream(rutaServidor);
		response.setContentType("text/plain");
		response.addHeader("Content-disposition", "attachment:fileName=" + "scbdv-comedor-servicios.log");

		OutputStream os = response.getOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = fis.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		fis.close();
	}

	@PostMapping(value = "/mover-archivos")
	public ResponseModel moverArchivos() {
		log.info("moverArchivos => {}");
		return giomService.moverArchivos();

	}

//	@PostMapping(value = "/respaldar-archivos")
//	public ResponseModel respaldarArchivos() {
//		log.info("respaldarArchivos => {}");
//		return giomService.respaldarArchivos();
//
//	}
	
	@PostMapping(value = "/cifrar")
	public ResponseModel cifrar() {
		log.info("respaldarArchivos => {}");
		return giomService.cifrar();

	}

}
