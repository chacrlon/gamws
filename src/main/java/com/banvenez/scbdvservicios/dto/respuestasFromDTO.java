package com.banvenez.scbdvservicios.dto;
import lombok.Data;
import java.util.List;

@Data
public class respuestasFromDTO {

	  private String estatus;  
	    private String mensaje;  
	    private List<String> data;  
    
	    @Override  
	    public String toString() {  
	        return "respuestasFromDTO{" +  
	                "estatus='" + estatus + '\'' +  
	                ", mensaje='" + mensaje + '\'' +  
	                ", data=" + data +  
	                '}';  
	    }  
	    
}


