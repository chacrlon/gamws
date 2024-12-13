/*package com.banvenez.scbdvservicios.dto;  
 

import lombok.Data;  

@Data  
public class LoteDTO {  

    private String numeroDeCuenta;   
    private String vef; // Añadir el campo vef  
    private Double montoTransaccion;   
    private String tipoMovimiento;     
    private String serialOperacion;   
    private String referencia;        
    private String codigoOperacion;      
    private String referencia2;    
    private String tipoDocumento;         
    private String cedula;                  

 // Método para obtener una representación en cadena  
    public String toFormattedString() {  
        StringBuilder sb = new StringBuilder();  
        // Concatenar los campos y ajustar el formato  
        sb.append(String.format("%s", numeroDeCuenta)); // Número de cuenta  
        sb.append(String.format("%s", vef != null ? vef.trim() : "")); // VEF  
        sb.append(String.format("%s", "0000000000")); // Referencia2  
        sb.append(String.format("%s", String.format("%.0f", montoTransaccion != null ? montoTransaccion : 0))); // Monto de transacción sin decimales  
        sb.append(String.format("%s", tipoMovimiento)); // Tipo de movimiento  
        sb.append(String.format("%s", serialOperacion)); // Serial de operación  
        sb.append(String.format("%s", referencia != null ? referencia.trim() : "")); // Referencia
        sb.append(String.format("%s", codigoOperacion)); // Código de operación  
        sb.append(String.format("%s", referencia2 != null ? referencia2.trim() : "")); // Referencia2 nuevamente  
        sb.append(String.format("%s", tipoDocumento)); // Tipo de documento   
        sb.append(String.format("%s", cedula != null ? cedula.trim() : "")); // Cédula  

        return sb.toString();  
    } 
}*/

package com.banvenez.scbdvservicios.dto;  

import lombok.Data;  

@Data  
public class LoteDTO {  

    private static int contador = 0; // Contador estático para el incremental  
    private String numeroDeCuenta;   // Dígito 71-90  
    private String vef;               // Dígito 58-58  
    private Double montoTransaccion;  // Dígito 100-116  
    private String tipoMovimiento;     // Dígito 49-49  
    private String serialOperacion;    // Dígito 91-95  
    private String referencia;         // Dígito 41-48  
    private String codigoOperacion;    // Dígito 96-99  
    private String referencia2;        // Dígito 41-48 (relleno)  
    private String tipoDocumento;      // Dígito 50-57  
    private String cedula;             // Dígito 58-58  
    private String incremental;        // Dígito 01-10  
    private String idLote;             // ID_LOTE_GIOM_FK  
    private String idRegistro;         // ID_REGISTRO_GIOM_PK  
    private String fecha;              // FECHA_CARGA  

    // Constructor  
    public LoteDTO() {  
        contador++; // Incrementar el contador  
        this.incremental = agregarCeros(String.valueOf(contador), 10); // Formatear el contador  
    }  

    // Método para obtener una representación en cadena  
    public String toFormattedString() {  
        StringBuilder sb = new StringBuilder();  

        System.out.println("Incremental: " + incremental); // Imprimir valor  

        // 2. ID_LOTE (dígitos 11-25)  
        String idLoteFormatted = agregarCeros(idLote != null ? idLote : "0", 15); // Usar idLote  
        System.out.println("ID Lote: " + idLoteFormatted); // Imprimir valor  

        // 3. ID_REGISTRO (dígitos 26-40)  
        String idRegistroFormatted = agregarCeros(idRegistro != null ? idRegistro : "0", 15); // Usar idRegistro  
        System.out.println("ID Registro: " + idRegistroFormatted); // Imprimir valor  

        // 4. REFERENCIA (dígitos 41-48)  
        String referenciaFormatted = referencia != null ? referencia.trim() : "";  
        referenciaFormatted = agregarCeros(referenciaFormatted, 8);  
        System.out.println("Referencia: " + referenciaFormatted); // Imprimir valor  

        // 5. TRANSACCION (dígito 49)  
        String transaccion = tipoMovimiento != null ? tipoMovimiento : "";  
        transaccion = agregarCeros(transaccion, 1);  
        System.out.println("Transacción: " + transaccion); // Imprimir valor  

        // 6. FECHA (dígitos 50-57)  
        String fechaFormatted = fecha != null ? fecha.substring(0, 10).replace("-", "") : "20241120"; // Usar fecha y formatear  
        System.out.println("Fecha: " + fechaFormatted); // Imprimir valor  
        
     // 7. TIPO DOCUMENTO (dígito 50-57)  
        String tipoDocumentoFormatted = (tipoDocumento != null && !tipoDocumento.trim().isEmpty()) ? tipoDocumento.trim() : " "; // Asignar espacio vacío si está vacío  
        System.out.println("Tipo Documento: " + tipoDocumentoFormatted); // Imprimir valor  

        // 8. CEDULA (dígito 58)  
        String cedulaFormatted = cedula != null ? cedula.trim() : "";  
        cedulaFormatted = agregarCeros(cedulaFormatted, 8); // Asegurarse de que tenga el formato correcto  
        System.out.println("Cédula: " + cedulaFormatted); // Imprimir valor  

        // Concatenar tipoDocumento y cedula  
        String tipoCedula = tipoDocumentoFormatted + cedulaFormatted;  
        tipoCedula = agregarCeros(tipoCedula, 9); // Asegurarse de que tenga el formato correcto (1 letra + 8 dígitos)  
        System.out.println("Tipo y Cédula: " + tipoCedula); // Imprimir valor  

        // 9. NUMERO-ORDENANTE (dígitos 60-69)  
        String numeroOrdenante = agregarCeros("0012397232", 11); // Placeholder, ajustar según sea necesario  
        System.out.println("Número Ordenante: " + numeroOrdenante); // Imprimir valor  

        // 10. DIGITO-ORDENANTE (dígito 70)  
        String digitoOrdenante = agregarCeros("0", 1); // Placeholder, ajustar según sea necesario  
        System.out.println("Dígito Ordenante: " + digitoOrdenante); // Imprimir valor  

        // 11. NUMERO-CUENTA (dígitos 71-90)  
        String numeroCuentaFormatted = agregarCeros(numeroDeCuenta != null ? numeroDeCuenta.trim() : "", 20);  
        System.out.println("Número de Cuenta: " + numeroCuentaFormatted); // Imprimir valor  

        // 12. SERIAL-BANCO (dígitos 91-95)  
        String serialBanco = agregarCeros(serialOperacion != null ? serialOperacion.trim() : "", 5);  
        System.out.println("Serial Banco: " + serialBanco); // Imprimir valor  

        // 13. CODIGO-OPER (dígitos 96-99)  
        String codigoOper = agregarCeros(codigoOperacion != null ? String.valueOf(codigoOperacion) : "0", 4);  
        System.out.println("Código Operación: " + codigoOper); // Imprimir valor  

        // 14. MONTO-TRANSACCION (dígitos 100-116)  
        String montoTransaccionFormatted = agregarCeros(String.valueOf(Math.round(montoTransaccion * 100)), 17); // Multiplicar por 100 y formatear  
        System.out.println("Monto Transacción: " + montoTransaccionFormatted); // Imprimir valor  

        // 15. LIBRE (dígito 117)  
        String libre = agregarCeros(" ", 1); // Campo libre  
        System.out.println("Libre: " + libre); // Imprimir valor  
        
     // 16. VALIDA-CEDULA (dígito 118)  
        String validaCedula = (cedula != null && cedula.trim().equals("00000000000")) ? "N" : "S";  
        validaCedula = agregarCeros(validaCedula, 1);  
        System.out.println("Valida Cédula: " + validaCedula); // Imprimir valor  

        // 17. TIPO-LOTE (dígitos 119-120)  
        String tipoLote = agregarCeros("01", 2); // Placeholder, ajustar según sea necesario  
        System.out.println("Tipo Lote: " + tipoLote); // Imprimir valor  

        // Concatenar todos los campos  
        sb.append(incremental);  
        sb.append(idLoteFormatted);  
        sb.append(idRegistroFormatted);  
        sb.append(referenciaFormatted);  
        sb.append(transaccion);  
        sb.append(fechaFormatted);  
        sb.append(tipoCedula);  // Aquí se incluye el tipo de documento y la cédula  
        sb.append(" "); // Agregar un espacio en blanco  
        //sb.append(numeroOrdenante);
        //sb.append(digitoOrdenante);
        sb.append(numeroCuentaFormatted);  
        sb.append(serialBanco);  
        sb.append(codigoOper);  
        sb.append(montoTransaccionFormatted);  
        sb.append(" "); // Este es el campo libre   
        sb.append(validaCedula);  
        sb.append(tipoLote);  
        // sb.append(filler);  

        return sb.toString();  
    }  

    // Método para agregar ceros a la izquierda  
    private String agregarCeros(String numeroProgramable, int cantidadDigitos) {  
        return String.format("%" + cantidadDigitos + "s", numeroProgramable).replace(' ', '0');  
    }  
}