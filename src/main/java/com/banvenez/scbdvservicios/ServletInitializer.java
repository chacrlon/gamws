package com.banvenez.scbdvservicios;  

import org.springframework.boot.builder.SpringApplicationBuilder;  
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;  
import org.springframework.web.WebApplicationInitializer;  
import lombok.extern.slf4j.Slf4j;  

@Slf4j  
public class ServletInitializer extends SpringBootServletInitializer implements WebApplicationInitializer {  

    @Override  
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {  
        log.info("**** Hola mundo GIOM desde ServletInitializer **** ");  
        return application.sources(IntServiciosApplication.class);  
    }  
}