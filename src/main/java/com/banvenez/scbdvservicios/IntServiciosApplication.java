package com.banvenez.scbdvservicios;  

import com.banvenez.scbdvservicios.util.GiomService;  
import lombok.extern.slf4j.Slf4j;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.boot.SpringApplication;  
import org.springframework.boot.autoconfigure.SpringBootApplication;  
import org.springframework.scheduling.annotation.EnableAsync;  
import org.springframework.scheduling.annotation.EnableScheduling;  

@SpringBootApplication  
@EnableScheduling  
@EnableAsync  
@Slf4j  
public class IntServiciosApplication {  

    @Autowired  
    GiomService gamService;  

    public static void main(String[] args) {  
        SpringApplication.run(IntServiciosApplication.class, args);  
        log.info("**** Hola mundo GIOM **** ");  
    }  

    // Si deseas que el método programado se ejecute al iniciar la aplicación, descomenta el siguiente método  
    /*  
    @Scheduled(cron="${cron.getGion}")  
    public void envioGam() {  
        log.info("**** BEGIN: envioGiom **** ");  
        try {  
            gamService.enviarLote();  
        } catch (Exception e) {  
            log.error(e.getMessage(), e);  
        }  
        log.info("**** END: envioGiom **** ");  
    }  
    */  
}