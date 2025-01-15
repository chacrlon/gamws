package com.banvenez.scbdvservicios.dto;  

import lombok.Data;  

@Data  
public class LoteDTO {  

    private static int contador = 0;
    private String numeroDeCuenta;
    private String vef;
    private Double montoTransaccion;
    private String tipoMovimiento;
    private String serialOperacion;
    private String referencia;
    private String codigoOperacion;
    private String referencia2;
    private String tipoDocumento;
    private String cedula;
    private String incremental;
    private String idLote;
    private String idRegistro;
    private String fecha;
    // Nuevas variables añadidas  
    private String numeroOrdenante; 
    private String digitoOrdenante; 
    private String campoLibre;
    private String validaCedula;
    private String tipoLote;
    private String observacion;
    private String cod_err;
    private String tip_err;
    private String des_err;
    private String filler;

    // Constructor  
    public LoteDTO() {  
        contador++; // Incrementar el contador  
        this.incremental = agregarCeros(String.valueOf(contador), 10); // Formatear el contador  
    }  

    // Método para obtener una representación en cadena  
    public String toFormattedString() {  
        StringBuilder sb = new StringBuilder();  

        System.out.println("Incremental: " + incremental);  

        String idLoteFormatted = agregarCeros(idLote != null ? idLote : "0", 15);
        System.out.println("ID Lote: " + idLoteFormatted);  
  
        String idRegistroFormatted = agregarCeros(idRegistro != null ? idRegistro : "0", 15); 
        System.out.println("ID Registro: " + idRegistroFormatted);  

        String referenciaFormatted = referencia != null ? referencia.trim() : "";  
        referenciaFormatted = agregarCeros(referenciaFormatted, 8);  
        System.out.println("Referencia: " + referenciaFormatted);   
  
        String transaccion = tipoMovimiento != null ? tipoMovimiento : "";  
        transaccion = agregarCeros(transaccion, 1);  
        System.out.println("Transacción: " + transaccion);
 
        String fechaFormatted = fecha != null ? fecha.substring(0, 10).replace("-", "") : "20241120"; // Usar fecha y formatear  
        System.out.println("Fecha: " + fechaFormatted);  
        
        String tipoDocumentoFormatted = (tipoDocumento != null && !tipoDocumento.trim().isEmpty()) ? tipoDocumento.trim() : " "; // Asignar espacio vacío si está vacío  
        System.out.println("Tipo Documento: " + tipoDocumentoFormatted); 
 
        String cedulaFormatted = cedula != null ? cedula.trim() : "";  
        cedulaFormatted = agregarCeros(cedulaFormatted, 8);  
        System.out.println("Cédula: " + cedulaFormatted);   
 
        String tipoCedula = tipoDocumentoFormatted + cedulaFormatted;  
        tipoCedula = agregarCeros(tipoCedula, 9);  
        System.out.println("Tipo y Cédula: " + tipoCedula);

        String numeroOrdenante = agregarCeros("0012397232", 11);
        System.out.println("Número Ordenante: " + numeroOrdenante);

        String digitoOrdenante = agregarCeros("0", 1);
        System.out.println("Dígito Ordenante: " + digitoOrdenante);

        String numeroCuentaFormatted = agregarCeros(numeroDeCuenta != null ? numeroDeCuenta.trim() : "", 20);
        System.out.println("Número de Cuenta: " + numeroCuentaFormatted);
 
        String serialBanco = agregarCeros(serialOperacion != null ? serialOperacion.trim() : "", 5);  
        System.out.println("Serial Banco: " + serialBanco);
 
        String codigoOper = agregarCeros(codigoOperacion != null ? String.valueOf(codigoOperacion) : "0", 4);  
        System.out.println("Código Operación: " + codigoOper);
 
        String montoTransaccionFormatted = agregarCeros(String.valueOf(Math.round(montoTransaccion * 100)), 17); // Multiplicar por 100 y formatear  
        System.out.println("Monto Transacción: " + montoTransaccionFormatted);

        String libre = agregarCeros(" ", 1);
        System.out.println("Libre: " + libre);
         
        String validaCedula = (cedula != null && cedula.trim().equals("00000000000")) ? "N" : "S";  
        validaCedula = agregarCeros(validaCedula, 1);  
        System.out.println("Valida Cédula: " + validaCedula);

        String tipoLote = agregarCeros("01", 2);
        System.out.println("Tipo Lote: " + tipoLote);

        // Concatenar todos los campos  
        sb.append(incremental);  
        sb.append(idLoteFormatted);  
        sb.append(idRegistroFormatted);  
        sb.append(referenciaFormatted);  
        sb.append(transaccion);  
        sb.append(fechaFormatted);  
        sb.append(tipoCedula);  // Aquí se incluye el tipo de documento y la cédula  
        sb.append(" "); // Agregar un espacio en blanco  
        sb.append(numeroCuentaFormatted);  
        sb.append(serialBanco);  
        sb.append(codigoOper);  
        sb.append(montoTransaccionFormatted);  
        sb.append(" "); // Este es el campo libre   
        sb.append(validaCedula);  
        sb.append(tipoLote);   

        return sb.toString();  
    }  

    // Método para agregar ceros a la izquierda  
    private String agregarCeros(String numeroProgramable, int cantidadDigitos) {  
        return String.format("%" + cantidadDigitos + "s", numeroProgramable).replace(' ', '0');  
    }  
}