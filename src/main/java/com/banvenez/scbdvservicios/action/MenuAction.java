package com.banvenez.scbdvservicios.action;

import com.banvenez.scbdvservicios.dto.JsonEnvioMenuDto;
import com.banvenez.scbdvservicios.dto.JsonMenuDto;
import com.banvenez.scbdvservicios.dto.MenuDto;
//import com.banvenez.scbdvservicios.util.MenuPrincipal;
import com.google.gson.Gson;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class MenuAction {

//    @CrossOrigin
//    @RequestMapping(path = "obtenerMenu", produces = "application/json")
//    @Post
//    public HashMap<String, Object> obtenerMenu(@RequestBody String jsonEntrada){
//        List<MenuDto> resp = new ArrayList<MenuDto>();
//        HashMap<String, Object> respI = new HashMap<String, Object>();
//        JsonEnvioMenuDto menu = null;
//        try {
//            MenuPrincipal menuP = new MenuPrincipal();
//            Gson gson = new Gson();
//            JsonMenuDto bsqApp = gson.fromJson(jsonEntrada, JsonMenuDto.class);
//            resp = menuP.cargar(bsqApp.getCedula(), bsqApp.getApp());
//            menu = new JsonEnvioMenuDto(resp);
//            if(menu.getMenu().size() > 0){
//                respI.put("status", "success");
//                respI.put("lstMenu" , menu);
//            }else{
//                respI.put("status", "fail");
//                respI.put("mensaje", "No cumple con el perfil para ingresar en la aplicación");
//            }
//
//        }catch (Exception e){
//            System.out.println("obtenerMenu " + e.getMessage());
//            respI.put("status", "fail");
//            respI.put("mensaje", "Tuvimos un incoveniente en procesar la acción");
//        }
//        return  respI;
//    }
}
