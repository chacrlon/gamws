//package com.banvenez.scbdvservicios.action;
//
//import com.banvenez.scbdvservicios.dto.jsonSesionDto;
//import com.banvenez.scbdvservicios.util.Constantes;
//import com.google.gson.Gson;
//import oracle.jdbc.proxy.annotation.Post;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//
//@RestController
//public class SesionAction {

//    @Autowired
//   // private MsintDao daoMsint;
//
//    @CrossOrigin
//    @RequestMapping(path = "crearSesion", produces = "application/json")
//    @Post
//    public HashMap<String, Object> crearSesion(@RequestBody String jsonEntrada){
//        HashMap<String, Object> resp = new HashMap<String, Object>();
//        try {
//            Gson gson = new Gson();
//            jsonSesionDto bsqApp = gson.fromJson(jsonEntrada, jsonSesionDto.class);
//            //Integer respCre = daoMsint.crearSesion(bsqApp.getCodUsuario(), bsqApp.getCodAplicativo());
//            if(respCre > 0){
//                resp.put("status", "success");
//                resp.put("mensaje", "se ha creado la sesión exitosamente");
//            }else{
//                resp.put("status", "fail");
//                resp.put("mensaje", "tuvimos un incoveniente en crear la sesion");
//            }
//        }catch (Exception e){
//            resp.put("status", "fail");
//            resp.put("mensaje", "tuvimos un incoveniente en crear la sesion");
//            System.out.println("error " + e.getMessage());
//
//        }
//    return  resp;
//    }
//
//
//    @CrossOrigin
//    @RequestMapping(path = "cerrarSesion", produces = "application/json")
//    @Post
//    public HashMap<String, Object> cerrarSesion(@RequestBody String jsonEntrada){
//        HashMap<String, Object> resp = new HashMap<String, Object>();
//        try {
//            Gson gson = new Gson();
//            jsonSesionDto bsqApp = gson.fromJson(jsonEntrada, jsonSesionDto.class);
//            Integer respCre = daoMsint.cerrarSesion(bsqApp.getCodUsuario(), bsqApp.getCodAplicativo());
//            if(respCre == 0){
//                resp.put("status", "success");
//                resp.put("mensaje", "se ha cerrado  la sesión exitosamente");
//            }else{
//                resp.put("status", "fail");
//                resp.put("mensaje", "tuvimos un incoveniente en cerrar la sesion");
//            }
//        }catch (Exception e){
//            resp.put("status", "fail");
//            resp.put("mensaje", "tuvimos un incoveniente en cerrar la sesion");
//            System.out.println("error " + e.getMessage());
//
//        }
//        return  resp;
//    }
//
//
//    @CrossOrigin
//    @RequestMapping(path = "extenderSesion", produces = "application/json")
//    @Post
//    public HashMap<String, Object> extenderSesion(@RequestBody String jsonEntrada){
//        HashMap<String, Object> resp = new HashMap<String, Object>();
//        try {
//            Gson gson = new Gson();
//            jsonSesionDto bsqApp = gson.fromJson(jsonEntrada, jsonSesionDto.class);
//            Integer respCre = daoMsint.validarSesion(bsqApp.getCodUsuario(), bsqApp.getCodAplicativo(), Constantes.contexto);
//            if(respCre == 2){
//                resp.put("status", "success");
//                resp.put("mensaje", "se ha extendido   la sesión exitosamente");
//            }else{
//                resp.put("status", "fail");
//                resp.put("mensaje", "sesion finalizada");
//            }
//        }catch (Exception e){
//            resp.put("status", "fail");
//            resp.put("mensaje", "tuvimos un incoveniente en extendido la sesion");
//            System.out.println("error " + e.getMessage());
//
//        }
//        return  resp;
//    }

//}
