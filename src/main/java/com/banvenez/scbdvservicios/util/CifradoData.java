package com.banvenez.scbdvservicios.util;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.banvenez.scbdvservicios.action.GiomAction;

import lombok.extern.slf4j.Slf4j;


@Slf4j

public class CifradoData {
	

	
	public  String encript(String text, String contrasena) throws Exception {
		log.info("Inicio del proceso de cifrado de data");
		Key aesKey = new SecretKeySpec(contrasena.getBytes(), "AES");

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey);

		byte[] encrypted = cipher.doFinal(text.getBytes());
		
		
		log.info("fin del proceso de cifrado de data");
		return new String(Base64.getEncoder().encode(encrypted)) ;
		
		}
	
	public  String decrypt(String encrypted, String contrasena) throws Exception {
		
		log.info("inicio del proceso de decifrado de data");
		byte[] encryptedBytes=Base64.getDecoder().decode( encrypted.replace("\n", "") );
			
		Key aesKey = new SecretKeySpec(contrasena.getBytes(), "AES");

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, aesKey);

		String decrypted = new String(cipher.doFinal(encryptedBytes));
		log.info("fin del proceso de decifrado de data");
		return decrypted;
		}

}
