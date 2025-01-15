package com.banvenez.scbdvservicios.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import java.io.*;
import java.net.SocketException;
import org.apache.logging.log4j.LogManager;


public class FtpUtil {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(FtpUtil.class);
       public static FTPClient getFTPClient(String ftpHost, Integer ftpPort, String ftpUserName, String ftpPassword){
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(60000);
            if(ftpPort != null){
                                 ftpClient.connect (ftpHost, ftpPort); // Conectarse al servidor FTP
            }else {
                                 ftpClient.connect (ftpHost); // Conectarse al servidor FTP
            }
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            	System.out.println("se realizo la conexion con el ftp correctamente");
                                 if (ftpClient.login (ftpUserName, ftpPassword)) {// Inicie sesión en el servidor FTP
                    if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                                                         "OPTS UTF8", "ON"))) {// Activa el soporte del servidor para UTF-8, si el servidor lo admite, usa codificación UTF-8, de lo contrario usa codificación local (GBK).
                        ftpClient.setControlEncoding("UTF-8");
                    }else {
                        ftpClient.setControlEncoding("GBK");
                    }
                                         ftpClient.enterLocalPassiveMode (); // Establecer el modo pasivo
//                                         ASCII_FILE_TYPE BINARY_FILE_TYPE
                                         ftpClient.setFileType (FTPClient.ASCII_FILE_TYPE); // Establecer el modo de transmisión, leer en flujo binario
                    ftpClient.enterLocalPassiveMode();
                                        logger.info("¡La conexión del servicio FTP se realizó correctamente!");
                }else {
                	logger.error("codigo de respuesta ftp "+ftpClient.getReplyCode());
                	logger.error("mensaje de respuesta ftp "+ftpClient.getReplyString());
                                        logger.error("¡El nombre de usuario o la contraseña del servicio FTP son incorrectos!");
                    disConnection(ftpClient);
                }
            }else {
            	logger.error("codigo de respuesta ftp "+ftpClient.getReplyCode());
            	logger.error("mensaje de respuesta ftp "+ftpClient.getReplyString());
                                 logger.error("¡No se pudo conectar al servicio FTP!");
                disConnection(ftpClient);
            }
        } catch (SocketException e) {
        	logger.error("codigo de respuesta ftp "+ftpClient.getReplyCode());
        	logger.error("mensaje de respuesta ftp "+ftpClient.getReplyString());
            disConnection(ftpClient);
                        logger.error ("La dirección IP de FTP puede ser incorrecta, configúrela correctamente");
        } catch (IOException e) {
        	logger.error("el mensaje de error al tratar de conectar es:",e);
        	logger.info(e.getMessage()); 
        	logger.error("codigo de respuesta ftp "+ftpClient.getReplyCode());
        	logger.error("mensaje de respuesta ftp "+ftpClient.getReplyString());
            disConnection(ftpClient);
                         logger.error ("El puerto FTP es incorrecto, configúrelo correctamente");
        }
        return ftpClient;
    }
           public static void disConnection(FTPClient ftpClient){
               logger.info("desconectando del servidor ftp");
        try {
            if(ftpClient.isConnected()){
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            logger.error("error al tratar de desconectar del servidor ftp ",e);
            
        }
    }
               public static FTPFile[] getFTPDirectoryFiles(FTPClient ftpClient,String path){
                   logger.info("leyendo archivos del servidor ftp");
        FTPFile[] files = null;
        try {
            ftpClient.changeWorkingDirectory(path);
            files = ftpClient.listFiles();
        }catch (Exception e){
            e.printStackTrace();
                         logger.error("¡Excepción de datos de lectura FTP!");
        }
        return files;
    }
               
    public static InputStream getFTPFile(FTPClient ftpClient,String path,String fileName){
        logger.info("obteniendo archivo del servidor ftp");
        InputStream in = null;
        try {
            ftpClient.changeWorkingDirectory(path);
            FTPFile[] files = ftpClient.listFiles();
            if(files.length > 0){
                in  = ftpClient.retrieveFileStream(fileName);
            }
        }catch (Exception e){
                         logger.error ("¡Excepción de datos de lectura FTP!",e);
        }
        return in;
    }
    
    
    public static boolean move(String in,String to,String host,String userFtp, String passwordFtp){
        FTPClient ftpClient = getFTPClient(host,21,userFtp,passwordFtp);
        try {
            ftpClient.rename(in, to);
            
        } catch (Exception e) {
            logger.error("se produjo un error al tratar de mover el archivo ", e);
            return false;
        }
        try {
            ftpClient.disconnect();
        } catch (Exception e) {
            logger.error("se produjo un error al tratar de cerrar la sesion ",e);
        }
        return true;
    }
    
}
